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
    private final ShopAuditService audit; private final ProductRepository products;
    private final StoreOrderRepository orders;
    private final UserRepository users;
    private final ShopService shop;
    private final EmailService emailService;
    private final VoucherService voucherService;
    private final ProductVariantRepository variants;

    private record Line(Product product, ProductVariant variant, int qty, double unit) {}

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
        if (linkedCustomer == null && !customerEmail.isBlank())
            linkedCustomer = users.findByEmailIgnoreCase(customerEmail).orElse(null);
        if (linkedCustomer == null && customerEmail.isBlank() && !customerPhone.isBlank()) {
            List<User> matches = users.findByPhone(customerPhone);
            if (matches.size() == 1) linkedCustomer = matches.get(0);
        }
        if (linkedCustomer != null && !customerEmail.isBlank() && !linkedCustomer.getEmail().equalsIgnoreCase(customerEmail))
            throw new RuntimeException("Email không khớp tài khoản khách đã chọn");
        if (linkedCustomer != null && !customerPhone.isBlank() && linkedCustomer.getPhone() != null
                && !linkedCustomer.getPhone().isBlank() && !linkedCustomer.getPhone().equals(customerPhone))
            throw new RuntimeException("Số điện thoại không khớp tài khoản khách đã chọn");
        String emailTo = linkedCustomer != null ? linkedCustomer.getEmail() : customerEmail;
        if (linkedCustomer != null && !"ROLE_USER".equals(linkedCustomer.getRole().getRoleName()))
            throw new RuntimeException("Tài khoản khách hàng không hợp lệ");
        if (linkedCustomer == null && customerName.isBlank())
            throw new RuntimeException("Vui lòng nhập tên khách (khách vãng lai) hoặc chọn khách đã có tài khoản");

        Set<String> seen=new HashSet<>();
        List<Line> lines = new ArrayList<>();
        List<VoucherService.CartLine> voucherLines = new ArrayList<>();
        double subtotal = 0;
        for (Map<String, Object> it : rawItems) {
            Long pid = Long.valueOf(String.valueOf(it.get("productId")));
            Long vid = it.get("variantId") == null ? null : Long.valueOf(String.valueOf(it.get("variantId")));
            int qty = Integer.parseInt(String.valueOf(it.getOrDefault("quantity", 1)));
            if (qty <= 0) throw new RuntimeException("Số lượng không hợp lệ");
            Product p = products.findLocked(pid).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            if(!Boolean.TRUE.equals(p.getActive()))throw new RuntimeException("Sản phẩm đã ngừng bán");if(!seen.add(pid+":"+vid))throw new RuntimeException("Dòng sản phẩm trùng lặp");if(vid==null&&!variants.findByProductIdOrderByIdAsc(pid).isEmpty())throw new RuntimeException("Vui lòng chọn phân loại");ProductVariant v = null; double unit; int stock;
            if (vid != null) {
                v = variants.findLocked(vid).orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm"));
                if(!v.getProduct().getId().equals(pid)||!Boolean.TRUE.equals(v.getActive()))throw new RuntimeException("Phân loại không hợp lệ");unit = v.getPriceOverride() != null && v.getPriceOverride() > 0 ? v.getPriceOverride() : shop.price(p);
                stock = v.getStock();
            } else {
                unit = shop.price(p);
                stock = p.getStock();
            }
            if (qty > stock) throw new RuntimeException(p.getName() + " chỉ còn " + stock + " sản phẩm");
            lines.add(new Line(p, v, qty, unit));
            double lineTotal = unit * qty;
            subtotal += lineTotal;
            voucherLines.add(new VoucherService.CartLine(p.getId(), lineTotal));
        }

        String voucherCode = str(req.get("voucherCode"));
        Voucher voucher = null; double voucherDiscount = 0;
        if (!voucherCode.isBlank()) {
            var r = voucherService.validate(voucherCode, voucherLines);
            voucher = r.getKey(); voucherDiscount = r.getValue();
        }

        StoreOrder o = StoreOrder.builder().user(linkedCustomer).channel(SalesChannel.POS).status(OrderStatus.PAID)
                .subtotal(subtotal).discount(0d).voucherCode(voucher != null ? voucher.getCode() : null).voucherDiscount(voucherDiscount)
                .shippingFee(0d).total(subtotal - voucherDiscount)
                .receiverName(linkedCustomer != null ? linkedCustomer.getFullName() : customerName).receiverEmail(emailTo)
                .phone(linkedCustomer != null ? linkedCustomer.getPhone() : customerPhone)
                .shippingAddress("Mua tại quầy").note(str(req.get("note")))
                .createdByStaffId(staff.getId()).paymentMethod(paymentMethod).paidAt(LocalDateTime.now()).build();
        orders.save(o);

        audit.event(o,null,"Thanh toán tại quầy");
        for (Line l : lines) {
            int before=l.variant()!=null?l.variant().getStock():l.product().getStock();audit.stock(l.product().getId(),l.variant()!=null?l.variant().getId():null,before,before-l.qty(),o.getId(),"Bán tại quầy");
            if (l.variant() != null) { l.variant().setStock(l.variant().getStock() - l.qty()); variants.save(l.variant()); }
            else { l.product().setStock(l.product().getStock() - l.qty()); products.save(l.product()); }
            String variantLabel = l.variant() == null ? null : l.variant().getAttributeValues().stream()
                    .sorted(Comparator.comparing(av -> av.getAttribute().getName()))
                    .map(av -> av.getAttribute().getName() + ": " + av.getValue())
                    .reduce((a, b) -> a + ", " + b).orElse("");
            o.getItems().add(OrderItem.builder().order(o).productId(l.product().getId()).productName(l.product().getName())
                    .imageUrl(l.product().getImageUrl()).variantId(l.variant() == null ? null : l.variant().getId()).variantLabel(variantLabel)
                    .unitPrice(l.unit()).quantity(l.qty()).lineTotal(l.unit() * l.qty()).build());
        }
        if (voucher != null) voucherService.markUsed(voucher);
        orders.save(o);

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
