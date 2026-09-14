package com.example.gymmanagement.shop;

import com.example.gymmanagement.entity.User;
import com.example.gymmanagement.repository.UserRepository;
import com.example.gymmanagement.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PosService {
    private final ProductRepository products;
    private final StoreOrderRepository orders;
    private final UserRepository users;
    private final ShopService shop;
    private final EmailService emailService;
    private final VoucherService voucherService;

    @Transactional
    public Map<String, Object> createOrder(String staffEmail, Map<String, Object> req) {
        User staff = users.findByEmail(staffEmail).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) req.get("items");
        if (rawItems == null || rawItems.isEmpty()) throw new RuntimeException("Vui lòng chọn ít nhất một sản phẩm");

        String customerName = str(req.get("customerName")), customerPhone = str(req.get("customerPhone")), customerEmail = str(req.get("customerEmail"));
        String paymentMethod = str(req.get("paymentMethod")); if (paymentMethod.isBlank()) paymentMethod = "CASH";
        Long customerUserId = req.get("customerUserId") == null ? null : Long.valueOf(String.valueOf(req.get("customerUserId")));
        User linkedCustomer = customerUserId == null ? null : users.findById(customerUserId).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        if (linkedCustomer == null && customerName.isBlank())
            throw new RuntimeException("Vui lòng nhập tên khách (khách vãng lai) hoặc chọn khách đã có tài khoản");

        Map<Long, Integer> qtyByProduct = new LinkedHashMap<>();
        for (Map<String, Object> it : rawItems) {
            Long pid = Long.valueOf(String.valueOf(it.get("productId")));
            int qty = Integer.parseInt(String.valueOf(it.getOrDefault("quantity", 1)));
            if (qty <= 0) throw new RuntimeException("Số lượng không hợp lệ");
            qtyByProduct.merge(pid, qty, Integer::sum);
        }

        Map<Long, Product> locked = new LinkedHashMap<>();
        double subtotal = 0;
        for (Map.Entry<Long, Integer> e : qtyByProduct.entrySet()) {
            Product p = products.findLocked(e.getKey()).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            if (e.getValue() > p.getStock()) throw new RuntimeException(p.getName() + " chỉ còn " + p.getStock() + " sản phẩm");
            locked.put(p.getId(), p);
            subtotal += shop.price(p) * e.getValue();
        }

        String voucherCode = str(req.get("voucherCode"));
        Voucher voucher = null; double voucherDiscount = 0;
        if (!voucherCode.isBlank()) {
            var r = voucherService.validate(voucherCode, subtotal);
            voucher = r.getKey(); voucherDiscount = r.getValue();
        }

        StoreOrder o = StoreOrder.builder().user(linkedCustomer).channel(SalesChannel.POS).status(OrderStatus.PAID)
                .subtotal(subtotal).discount(0d).voucherCode(voucher != null ? voucher.getCode() : null).voucherDiscount(voucherDiscount)
                .shippingFee(0d).total(subtotal - voucherDiscount)
                .receiverName(linkedCustomer != null ? linkedCustomer.getFullName() : customerName)
                .phone(linkedCustomer != null ? linkedCustomer.getPhone() : customerPhone)
                .shippingAddress("Mua tại quầy").note(str(req.get("note")))
                .createdByStaffId(staff.getId()).paymentMethod(paymentMethod).paidAt(LocalDateTime.now()).build();
        orders.save(o);

        for (Map.Entry<Long, Integer> e : qtyByProduct.entrySet()) {
            Product p = locked.get(e.getKey()); int qty = e.getValue(); double unit = shop.price(p);
            p.setStock(p.getStock() - qty); products.save(p);
            o.getItems().add(OrderItem.builder().order(o).productId(p.getId()).productName(p.getName())
                    .imageUrl(p.getImageUrl()).unitPrice(unit).quantity(qty).lineTotal(unit * qty).build());
        }
        if (voucher != null) voucherService.markUsed(voucher);
        orders.save(o);

        String emailTo = linkedCustomer != null ? linkedCustomer.getEmail() : customerEmail;
        if (!emailTo.isBlank()) emailService.sendPosOrderConfirmation(emailTo, o.getReceiverName(), o.getId(), o.getTotal());

        return shop.orderMap(o);
    }

    public List<Map<String, Object>> posOrders() {
        return orders.findByChannelOrderByCreatedAtDesc(SalesChannel.POS).stream().map(shop::orderMap).toList();
    }

    public Map<String, Object> posOrder(Long id) {
        StoreOrder o = orders.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        if (o.getChannel() != SalesChannel.POS) throw new RuntimeException("Đây không phải hóa đơn bán tại quầy");
        return shop.orderMap(o);
    }

    private String str(Object o) { return o == null ? "" : String.valueOf(o).trim(); }
}