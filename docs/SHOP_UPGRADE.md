# Nâng cấp cửa hàng

Phạm vi: các mục 1, 2, 3, 4, 5, 7, 8, 9, 12, 13 theo yêu cầu. Tái sử dụng các bảng cửa hàng hiện có.

## Cơ sở dữ liệu

Tổng cấu trúc JPA: **43 bảng** (trước 39), gồm đúng 4 bảng bổ sung:
- customer_addresses: sổ địa chỉ theo tài khoản.
- customer_product_states: một dòng theo khách/sản phẩm, dùng chung yêu thích và lần xem cuối.
- shop_order_events: lịch sử xử lý và sự kiện thanh toán.
- shop_inventory_movements: số tồn trước/sau và nguồn thay đổi.

Danh sách ảnh được lưu JSON trong cột images của shop_products và product_reviews bằng JPA converter, không có bảng ảnh riêng. shop_orders bổ sung gateway_reference/payment_url và trạng thái CONFIRMED cho COD. Hibernate ddl-auto=update thực hiện bổ sung khi khởi động. URL H2 cần NON_KEYWORDS=VALUE để hỗ trợ cột value có sẵn ở voucher/thuộc tính (đã thêm vào application.properties; nếu ghi đè SPRING_DATASOURCE_URL cũng cần giữ tham số này).

Lịch sử chỉ được ghi từ thời điểm nâng cấp, không dựng lại lịch sử cũ. Thống kê không tạo bảng tổng hợp mới. Ngưỡng cảnh báo sắp hết là <=5.

## Giao diện

- /shop: tìm kiếm, danh mục, khoảng giá, sắp xếp, phân trang; yêu thích, đã xem, so sánh; giỏ khách vãng lai chỉnh sửa số lượng/xóa.
- /shop/product/:id: chi tiết, bộ ảnh, phân loại, đánh giá có ảnh/lọc sao/phân trang, sản phẩm liên quan.
- /app/shop: dùng cùng danh mục; checkout chọn địa chỉ lưu, QR/COD/ví; xem lịch sử đơn, gửi đánh giá và ảnh.
- /admin/shop: quản lý bộ ảnh, xử lý đơn COD, xem nhật ký.
- /admin/product-attributes: chỉnh SKU, giá, tồn và trạng thái biến thể; ngừng bán thay cho xóa biến thể.
- /admin/shop-reports: doanh thu online/POS, doanh thu theo ngày, sản phẩm bán chạy, cảnh báo tồn và nhật ký kho.
- /staff/orders, /tra-cuu-don: xem lịch sử và phân loại trong đơn.

Doanh thu báo cáo lọc theo ngày tạo đơn và chỉ cộng đơn có paidAt, loại đơn hủy/hết hạn. Tổng doanh thu gồm phí vận chuyển, đã trừ giảm giá. Tiền hàng trong bảng bán chạy là trước giảm giá/phí vận chuyển. COD chỉ ghi paidAt khi nhân viên xác nhận giao hàng/thu tiền.

## MoMo và ZaloPay

Mặc định hai ví không khả dụng. QR chuyển khoản hiện có và COD hoạt động độc lập. Chỉ bật ví khi có tài khoản merchant và callback công khai HTTPS. Không có khóa merchant được ghi trong repository.

Các thuộc tính cấu hình Spring (có thể truyền qua biến môi trường theo quy tắc Spring):

shop.payment.backend-url=https://your-api.example
shop.payment.return-url=https://your-web.example/app/shop?tab=orders
shop.payment.momo.enabled=true
shop.payment.momo.partner-code=...
shop.payment.momo.access-key=...
shop.payment.momo.secret-key=...
shop.payment.momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
shop.payment.zalopay.enabled=true
shop.payment.zalopay.app-id=...
shop.payment.zalopay.key1=...
shop.payment.zalopay.key2=...
shop.payment.zalopay.endpoint=https://sb-openapi.zalopay.vn/v2/create

Callback:
- POST /api/shop/payments/momo/ipn
- POST /api/shop/payments/zalopay/callback

Tạo phiên chỉ dành cho chủ đơn, số tiền lấy từ server. Redirect phía khách không xác nhận đã thanh toán. Callback phải qua kiểm tra HMAC, merchant và số tiền; ghi nhận thành công có tính idempotent. Nếu đã hủy/hết hạn rồi mới nhận tiền, giữ nguyên đơn và ghi sự kiện cần đối soát; hoàn tiền tự động nằm ngoài phạm vi lần này. Không tự thay đổi endpoint sandbox thành production.

Chưa kiểm thử giao dịch thật vì không có merchant credentials. Cần kiểm thử sandbox với callback HTTPS trước khi bật sử dụng thực tế; khi tạo phiên gặp lỗi kết nối không rõ kết quả cần đối soát nhà cung cấp.

Tài liệu giao thức đối chiếu:
- https://developers.momo.vn/v3/vi/docs/payment/api/wallet/onetime/
- https://docs.zalopay.vn/docs/specs/callback-api/

## Kiểm thử

- ShopUpgradeTest: 9 trường hợp H2 tạm, gồm số bảng, COD, hoàn tồn, địa chỉ, yêu thích/đã xem, ảnh đánh giá, tồn biến thể và phân trang.
- ShopPaymentTest: 4 trường hợp chữ ký, số tiền, callback lặp và thanh toán muộn.
- Lệnh: mvnw.cmd -Dtest=ShopUpgradeTest,ShopPaymentTest test (trong gym-management).
- Frontend: npm run build (trong gym-frontend).

Ảnh giới hạn JPG/PNG/WebP, 5 MB/ảnh, tối đa 8 ảnh. Database thử nghiệm giao diện dùng H2 memory, không thêm dữ liệu thử vào file gymdb hiện tại.

### Kết quả xác minh

- 13/13 kiểm thử cửa hàng đạt. Build frontend đạt.
- Luồng HTTP trên H2 tạm: gộp giỏ biến thể → địa chỉ → COD → giao hàng/thu tiền → đánh giá ảnh → doanh thu và lịch sử kho đạt.
- Kiểm tra trình duyệt: danh mục, chọn biến thể, giỏ khách tăng số lượng, chi tiết và báo cáo doanh thu đạt.
- Hồi quy toàn backend: 32/33 đạt. Test cũ MuscleGroupSplitPlannerTest.endurance_beginner_sessions4_cardioNgayCuoiBangKhong_doBaseQuotaThapHonTanSuat thất bại (mong CHEST/SHOULDERS/CARDIO, nhận FULL_BODY/CARDIO). Các tệp bộ lập lịch tập này không bị sửa trong nâng cấp cửa hàng.
