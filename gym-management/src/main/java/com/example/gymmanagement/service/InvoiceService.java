package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.request.CreateInvoiceRequest;
import com.example.gymmanagement.dto.response.InvoiceResponse;
import com.example.gymmanagement.entity.Invoice;
import com.example.gymmanagement.entity.Membership;
import com.example.gymmanagement.entity.User;
import com.example.gymmanagement.enums.PaymentStatus;
import com.example.gymmanagement.repository.InvoiceRepository;
import com.example.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private static final int EXPIRE_MINUTES = 5;

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final MembershipService membershipService;
    private final BankQrService bankQrService;
    private final NotificationService notificationService;
    private final com.example.gymmanagement.pet.UserCosmeticOwnershipRepository userCosmeticOwnershipRepository;

    @Transactional
    public InvoiceResponse createInvoice(String email, CreateInvoiceRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        invoiceRepository.findByUserIdAndStatus(user.getId(), PaymentStatus.PENDING)
                .ifPresent(existing -> {
                    throw new RuntimeException("Bạn đang có 1 hóa đơn chờ thanh toán (#" + existing.getId() +
                            "). Vui lòng thanh toán hoặc hủy hóa đơn đó trước khi tạo hóa đơn mới.");
                });

        Invoice invoice;
        if (request.getCosmeticItemCode() != null) {
            com.example.gymmanagement.pet.CosmeticItem item =
                    com.example.gymmanagement.pet.CosmeticItem.fromCode(request.getCosmeticItemCode());
            if (item.isFree()) {
                throw new RuntimeException("Trang phục này miễn phí, không cần thanh toán");
            }
            if (userCosmeticOwnershipRepository.existsByUserIdAndCosmeticCode(user.getId(), item.name())) {
                throw new RuntimeException("Bạn đã sở hữu trang phục này");
            }
            invoice = Invoice.builder()
                    .user(user)
                    .invoiceType(com.example.gymmanagement.pet.InvoiceType.COSMETIC)
                    .cosmeticItemCode(item.name())
                    .price((double) com.example.gymmanagement.pet.CosmeticItem.PRICE)
                    .status(PaymentStatus.PENDING)
                    .build();
        } else {
            double price = membershipService.getPrice(request.getMembershipType());
            invoice = Invoice.builder()
                    .user(user)
                    .invoiceType(com.example.gymmanagement.pet.InvoiceType.MEMBERSHIP)
                    .membershipType(request.getMembershipType())
                    .price(price)
                    .status(PaymentStatus.PENDING)
                    .build();
        }
        invoice = invoiceRepository.save(invoice);
        return generateQr(invoice);
    }
    private InvoiceResponse generateQr(Invoice invoice) {
        long amount = Math.round(invoice.getPrice());

        BankQrService.BankQrResult qr = bankQrService.generate(invoice.getId(), amount);

        invoice.setTransferCode(qr.getTransferCode());
        invoice.setQrRawPayload(qr.getQrRawPayload());
        invoice.setQrCodeUrl(qr.getQrImageUrl());
        invoice.setStatus(PaymentStatus.PENDING);
        invoice.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));

        invoiceRepository.save(invoice);
        return buildResponse(invoice);
    }

    @Transactional
    public InvoiceResponse regenerateQr(Long invoiceId, String email) {
        Invoice invoice = getOwnedInvoice(invoiceId, email);

        if (invoice.getStatus() != PaymentStatus.PENDING && invoice.getStatus() != PaymentStatus.EXPIRED
                && invoice.getStatus() != PaymentStatus.FAILED) {
            throw new RuntimeException("Hóa đơn này không thể tạo lại mã QR (trạng thái hiện tại: " + invoice.getStatus() + ")");
        }

        invoice.setRegenerateCount(invoice.getRegenerateCount() == null ? 1 : invoice.getRegenerateCount() + 1);
        if (invoice.getRegenerateCount() > 20) {
            throw new RuntimeException("Hóa đơn đã hết hạn quá nhiều lần. Vui lòng tạo đơn hàng mới.");
        }

        if (invoice.getInvoiceType() == com.example.gymmanagement.pet.InvoiceType.MEMBERSHIP
                && invoice.getMembershipType() != null) {
            invoice.setPrice(membershipService.getPrice(invoice.getMembershipType()));
        }
        return generateQr(invoice);
    }

    @Transactional
    public InvoiceResponse cancelInvoice(Long invoiceId, String email) {
        Invoice invoice = getOwnedInvoice(invoiceId, email);

        if (invoice.getStatus() != PaymentStatus.PENDING && invoice.getStatus() != PaymentStatus.EXPIRED) {
            throw new RuntimeException("Chỉ có thể hủy hóa đơn đang ở trạng thái chờ thanh toán hoặc đã hết hạn.");
        }

        invoice.setStatus(PaymentStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
        return buildResponse(invoice);
    }

    @Transactional
    public void handleBankWebhook(Map<String, Object> payload) {
        String content = String.valueOf(payload.getOrDefault("content", ""));
        Object amountObj = payload.get("transferAmount");
        String transferType = String.valueOf(payload.getOrDefault("transferType", "in"));
        String referenceCode = String.valueOf(payload.getOrDefault("referenceCode", payload.get("id")));

        if (!"in".equalsIgnoreCase(transferType)) {
            return;
        }
        if (amountObj == null) {
            log.warn("[Bank Webhook] Payload thiếu transferAmount: {}", payload);
            return;
        }

        long transferAmount = Long.parseLong(String.valueOf(amountObj));
        Invoice invoice = findInvoiceByContent(content);

        if (invoice == null) {
            log.warn("[Bank Webhook] Không tìm thấy hóa đơn khớp với nội dung chuyển khoản: '{}'", content);
            return;
        }

        if (invoice.getStatus() == PaymentStatus.PAID) {
            return;
        }

        if (transferAmount != Math.round(invoice.getPrice())) {
            log.warn("[Bank Webhook] Số tiền không khớp cho hóa đơn #{}: nhận {} nhưng cần {}",
                    invoice.getId(), transferAmount, invoice.getPrice());
            notificationService.sendToUser(invoice.getUser().getId(),
                    "Số tiền chuyển khoản không khớp",
                    "Hóa đơn #" + invoice.getId() + " cần đúng " + Math.round(invoice.getPrice()) +
                            "đ nhưng hệ thống nhận được " + transferAmount + "đ. Vui lòng liên hệ admin để được hỗ trợ.",
                    "SYSTEM");
            return;
        }

        invoice.setStatus(PaymentStatus.PAID);
        invoice.setTransactionId(referenceCode);
        invoice.setResultMessage(content);
        invoice.setPaidAt(LocalDateTime.now());

        if (invoice.getInvoiceType() == com.example.gymmanagement.pet.InvoiceType.COSMETIC) {
            if (!userCosmeticOwnershipRepository.existsByUserIdAndCosmeticCode(
                    invoice.getUser().getId(), invoice.getCosmeticItemCode())) {
                userCosmeticOwnershipRepository.save(
                        com.example.gymmanagement.pet.UserCosmeticOwnership.builder()
                                .userId(invoice.getUser().getId())
                                .cosmeticCode(invoice.getCosmeticItemCode())
                                .build());
            }
            invoiceRepository.save(invoice);
            notificationService.sendToUser(invoice.getUser().getId(),
                    "Thanh toán thành công",
                    "Hóa đơn #" + invoice.getId() + " đã thanh toán thành công. Trang phục đã được mở khóa.",
                    "SYSTEM");
        } else {
            Membership membership = membershipService.activatePaidMembership(
                    invoice.getUser(), invoice.getMembershipType(), referenceCode, "BANK_TRANSFER");
            invoice.setMembership(membership);
            invoiceRepository.save(invoice);
            notificationService.sendToUser(invoice.getUser().getId(),
                    "Thanh toán thành công",
                    "Hóa đơn #" + invoice.getId() + " đã thanh toán thành công. Gói " +
                            invoice.getMembershipType() + " đã được kích hoạt.",
                    "SYSTEM");
        }
    }

    private Invoice findInvoiceByContent(String content) {
        String normalized = content.toUpperCase().replaceAll("[^A-Z0-9]", " ");
        List<String> tokens = Arrays.asList(normalized.trim().split("\\s+"));

        for (Invoice invoice : invoiceRepository.findByStatus(PaymentStatus.PENDING)) {
            if (invoice.getTransferCode() != null && tokens.contains(invoice.getTransferCode().toUpperCase())) {
                return invoice;
            }
        }
        for (Invoice invoice : invoiceRepository.findByStatus(PaymentStatus.EXPIRED)) {
            if (invoice.getTransferCode() != null && tokens.contains(invoice.getTransferCode().toUpperCase())) {
                return invoice;
            }
        }
        return null;
    }

    @Transactional
    public int expireOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findByStatusAndExpiresAtBefore(
                PaymentStatus.PENDING, LocalDateTime.now());
        for (Invoice invoice : overdue) {
            invoice.setStatus(PaymentStatus.EXPIRED);
            invoiceRepository.save(invoice);
            String productName = invoice.getInvoiceType() == com.example.gymmanagement.pet.InvoiceType.COSMETIC
                    ? com.example.gymmanagement.pet.CosmeticItem.fromCode(invoice.getCosmeticItemCode()).getDisplayName()
                    : "Gói " + invoice.getMembershipType();
            notificationService.sendToUser(invoice.getUser().getId(),
                    "Hóa đơn hết hạn thanh toán",
                    "Hóa đơn #" + invoice.getId() + " (" + productName +
                            ") đã quá 5 phút chưa thanh toán. Bấm vào hóa đơn để tạo lại mã QR.",
                    "SYSTEM");
        }
        return overdue.size();
    }

    @Transactional
    public List<InvoiceResponse> getMyInvoices(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return invoiceRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::syncUnpaidMembershipPrice)
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceResponse getInvoice(Long id, String email) {
        Invoice invoice = syncUnpaidMembershipPrice(getOwnedInvoice(id, email));
        return buildResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::buildResponse).collect(Collectors.toList());
    }
    public List<InvoiceResponse> getInvoiceByUserId(Long userId){
        return invoiceRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::buildResponse).collect(Collectors.toList());
    }

    private Invoice getOwnedInvoice(Long id, String email) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        if (!invoice.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Bạn không có quyền truy cập hóa đơn này");
        }
        return invoice;
    }

    /**
     * Hóa đơn đã thanh toán là lịch sử bất biến. Riêng hóa đơn gói tập chưa thanh toán
     * phải luôn theo bảng giá hiện hành; nếu giá đã đổi thì QR cũ cũng phải được tạo lại
     * để số tiền trên màn hình và số tiền ngân hàng khớp nhau.
     */
    private Invoice syncUnpaidMembershipPrice(Invoice invoice) {
        if (invoice.getInvoiceType() != com.example.gymmanagement.pet.InvoiceType.MEMBERSHIP
                || invoice.getMembershipType() == null
                || invoice.getStatus() == PaymentStatus.PAID
                || invoice.getStatus() == PaymentStatus.CANCELLED) {
            return invoice;
        }

        double currentPrice = membershipService.getPrice(invoice.getMembershipType());
        if (invoice.getPrice() != null && Double.compare(invoice.getPrice(), currentPrice) == 0) {
            return invoice;
        }

        invoice.setPrice(currentPrice);
        if (invoice.getStatus() == PaymentStatus.PENDING) {
            generateQr(invoice);
        } else {
            invoiceRepository.save(invoice);
        }
        return invoice;
    }

    private InvoiceResponse buildResponse(Invoice invoice) {
        long secondsRemaining = 0;
        if (invoice.getStatus() == PaymentStatus.PENDING && invoice.getExpiresAt() != null) {
            secondsRemaining = Math.max(0, Duration.between(LocalDateTime.now(), invoice.getExpiresAt()).getSeconds());
        }
        String cosmeticName = null;
        if (invoice.getInvoiceType() == com.example.gymmanagement.pet.InvoiceType.COSMETIC
                && invoice.getCosmeticItemCode() != null) {
            cosmeticName = com.example.gymmanagement.pet.CosmeticItem
                    .fromCode(invoice.getCosmeticItemCode()).getDisplayName();
        }
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .userId(invoice.getUser().getId())
                .userName(invoice.getUser().getFullName())
                .userEmail(invoice.getUser().getEmail())
                .membershipType(invoice.getMembershipType())
                .invoiceType(invoice.getInvoiceType())
                .cosmeticItemCode(invoice.getCosmeticItemCode())
                .cosmeticItemName(cosmeticName)
                .price(invoice.getPrice())
                .membershipId(invoice.getMembership() != null ? invoice.getMembership().getId() : null)
                .status(invoice.getStatus())
                .transferCode(invoice.getTransferCode())
                .qrRawPayload(invoice.getQrRawPayload())
                .qrCodeUrl(invoice.getQrCodeUrl())
                .payUrl(invoice.getPayUrl())
                .transactionId(invoice.getTransactionId())
                .resultMessage(invoice.getResultMessage())
                .createdAt(invoice.getCreatedAt())
                .expiresAt(invoice.getExpiresAt())
                .paidAt(invoice.getPaidAt())
                .secondsRemaining(secondsRemaining)
                .build();
    }
}
