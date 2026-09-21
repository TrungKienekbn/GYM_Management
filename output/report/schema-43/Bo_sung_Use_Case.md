# Bổ sung Use Case cửa hàng và nhân viên

Tài liệu bổ sung cho SD-41_DATNSP26 (1).pdf, đối chiếu mã hiện tại ngày 17/09/2026. Bản đặc tả 43 bảng trước đó mới nêu danh sách chức năng cần cập nhật, chưa thay thế phần Use Case. Tài liệu này bổ sung danh mục, tác nhân, đặc tả và nguồn sơ đồ cho phạm vi cửa hàng/ca làm.

## 1. Cách ghép vào báo cáo

Giữ mã UC-01 đến UC-32 ở trang 18–20 để tránh làm hỏng tham chiếu cũ. Thêm 23 ca sử dụng dưới đây từ UC-33 đến UC-55 vào mục 2.2.2 và đưa đặc tả vào Phụ lục A. Số 55 là danh mục cũ cộng phần bổ sung cửa hàng/ca làm; không phải tuyên bố đã kiểm toán đầy đủ mọi chức năng khác như nhân vật đồng hành hay chatbot.

Cập nhật mục 2.2.1 với khách vãng lai, hội viên USER, nhân viên STAFF, quản trị ADMIN và cổng thanh toán. Các mã cũ UC-01/UC-28 cần ghi nhận đăng nhập/đăng xuất cho STAFF; quyền quản trị không có nghĩa được tự đăng ký tài khoản ADMIN. UC-14 giữ phạm vi thanh toán gói tập, UC-42 dành cho đơn cửa hàng. UC-15/UC-25 là hỗ trợ trực tiếp; không dùng hai mã này để mô tả chatbot USER/BOT. UC-17 và UC-27 đang có phạm vi chồng lấn: cần thống nhất nội dung trước khi gộp/đánh lại mã.

## 2. Tác nhân

| Tác nhân | Vai trò trong phần bổ sung |
|---|---|
| Khách vãng lai | Xem hàng, chọn phân loại, giỏ cục bộ, yêu thích/đã xem/so sánh và tra cứu bằng mã đơn + điện thoại. Cần đăng nhập để đặt hàng trực tuyến. |
| USER | Thực hiện các thao tác mua hàng; quản lý địa chỉ, đặt/thanh toán, theo dõi/hủy đơn và đánh giá hợp lệ. |
| STAFF | Bán POS, xử lý đơn và vào/kết ca được phép thao tác. |
| ADMIN | Quản lý danh mục, biến thể, voucher, báo cáo/kho và phân ca; API cũng cho phép bán POS, xử lý đơn và thao tác ca. |
| Cổng thanh toán | Tác nhân hệ thống bên ngoài của luồng MoMo/ZaloPay khi đã được cấu hình. Không áp dụng cho thu tiền COD. |

## 3. Danh mục bổ sung

| Mã | Use Case | Tác nhân |
|---|---|---|
| UC-33 | Xem và tìm kiếm sản phẩm | Khách vãng lai, USER |
| UC-34 | Xem chi tiết và chọn phân loại | Khách vãng lai, USER |
| UC-35 | Quản lý giỏ hàng | Khách vãng lai, USER |
| UC-36 | Quản lý sản phẩm yêu thích | Khách vãng lai, USER |
| UC-37 | Xem sản phẩm đã xem gần đây | Khách vãng lai, USER |
| UC-38 | So sánh sản phẩm | Khách vãng lai, USER |
| UC-39 | Quản lý địa chỉ nhận hàng | USER |
| UC-40 | Áp dụng mã giảm giá | Khách vãng lai, USER, STAFF, ADMIN |
| UC-41 | Đặt hàng trực tuyến | USER |
| UC-42 | Thanh toán đơn trực tuyến | USER; cổng thanh toán (phụ) |
| UC-43 | Theo dõi và tra cứu đơn hàng | Khách vãng lai, USER |
| UC-44 | Hủy đơn hàng | USER |
| UC-45 | Đánh giá sản phẩm sau mua | USER |
| UC-46 | Quản lý sản phẩm cửa hàng | ADMIN |
| UC-47 | Quản lý thuộc tính và biến thể | ADMIN |
| UC-48 | Quản lý mã giảm giá | ADMIN |
| UC-49 | Xử lý đơn hàng trực tuyến | STAFF, ADMIN |
| UC-50 | Bán hàng tại quầy | STAFF, ADMIN |
| UC-51 | Xem báo cáo cửa hàng | ADMIN |
| UC-52 | Theo dõi tồn thấp và nhật ký kho | ADMIN |
| UC-53 | Phân công và xem lịch làm việc | ADMIN |
| UC-54 | Vào ca làm việc | STAFF, ADMIN |
| UC-55 | Kết ca và đối soát tiền | STAFF, ADMIN |

## 4. Đặc tả chi tiết

### UC-33. Xem và tìm kiếm sản phẩm

Tác nhân: Khách vãng lai, USER.
Mục tiêu: Người mua xem được danh mục theo điều kiện đã chọn.
Tiền điều kiện: Có sản phẩm đang bán.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở cửa hàng.
2. Nhập từ khóa, danh mục hoặc khoảng giá.
3. Chọn cách sắp xếp và trang.
4. Hệ thống hiển thị sản phẩm phù hợp.
Luồng thay thế/ngoại lệ: Không có kết quả: hiển thị danh sách trống, cho phép đổi bộ lọc.
Hậu điều kiện thành công: Người mua xem được danh mục theo điều kiện đã chọn.
Dữ liệu liên quan: shop_products, product_variants, product_reviews.
Đối chiếu mã/giao diện: ShopExperienceController.java; /shop, /app/shop.

### UC-34. Xem chi tiết và chọn phân loại

Tác nhân: Khách vãng lai, USER.
Mục tiêu: Xác định đúng sản phẩm/biến thể trước khi mua.
Tiền điều kiện: Sản phẩm đang hoạt động.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở chi tiết sản phẩm.
2. Xem ảnh, mô tả và đánh giá.
3. Chọn tổ hợp phân loại.
4. Hệ thống hiển thị giá và tồn tương ứng.
Luồng thay thế/ngoại lệ: Biến thể ngừng bán/hết tồn: không thêm hợp lệ vào giỏ. Sản phẩm không có biến thể: dùng giá và tồn của sản phẩm.
Hậu điều kiện thành công: Xác định đúng sản phẩm/biến thể trước khi mua.
Dữ liệu liên quan: shop_products, product_variants, product_attributes, product_attribute_values, variant_attribute_values, product_reviews.
Đối chiếu mã/giao diện: ProductDetailView.vue; ProductVariantService.java; /shop/product/:id.

### UC-35. Quản lý giỏ hàng

Tác nhân: Khách vãng lai, USER.
Mục tiêu: Giỏ phản ánh lựa chọn hiện tại; chưa tạo đơn.
Tiền điều kiện: Sản phẩm/biến thể hợp lệ; khách có thể chưa đăng nhập.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Chọn sản phẩm, phân loại và số lượng.
2. Thêm vào giỏ.
3. Mở giỏ, sửa số lượng hoặc xóa dòng.
4. Hệ thống cập nhật tổng tiền tạm tính.
Luồng thay thế/ngoại lệ: Khách vãng lai lưu giỏ cục bộ và phải đăng nhập để thanh toán. Máy chủ kiểm tra lại giá, quyền sở hữu dòng giỏ và tồn kho; dữ liệu hiển thị cũ không đảm bảo còn hàng.
Hậu điều kiện thành công: Giỏ phản ánh lựa chọn hiện tại; chưa tạo đơn.
Dữ liệu liên quan: shop_cart_items, shop_products, product_variants; giỏ khách trong trình duyệt.
Đối chiếu mã/giao diện: ShopController.java; ShopService.java; PublicShopView.vue.

### UC-36. Quản lý sản phẩm yêu thích

Tác nhân: Khách vãng lai, USER.
Mục tiêu: Cập nhật cờ wishlisted, dùng chung bảng với lần xem gần nhất.
Tiền điều kiện: Sản phẩm tồn tại.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Chọn biểu tượng yêu thích.
2. Hệ thống lưu lựa chọn.
3. Mở danh sách yêu thích.
4. Bỏ đánh dấu khi không còn cần.
Luồng thay thế/ngoại lệ: Khách lưu cục bộ; sau đăng nhập/đăng ký, dữ liệu được đồng bộ theo xử lý của ứng dụng.
Hậu điều kiện thành công: Cập nhật cờ wishlisted, dùng chung bảng với lần xem gần nhất.
Dữ liệu liên quan: customer_product_states, shop_products.
Đối chiếu mã/giao diện: ShopCustomerService.java; useShopCustomer.js.

### UC-37. Xem sản phẩm đã xem gần đây

Tác nhân: Khách vãng lai, USER.
Mục tiêu: Lưu lần xem gần nhất, không tạo bảng lịch sử riêng cho từng lượt.
Tiền điều kiện: Có thao tác xem sản phẩm.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở chi tiết sản phẩm.
2. Hệ thống ghi nhận lần xem.
3. Mở khu vực đã xem.
4. Chọn lại sản phẩm cần tham khảo.
Luồng thay thế/ngoại lệ: Chưa xem sản phẩm: không có lịch sử. Dữ liệu khách nằm ở trình duyệt; tài khoản dùng trạng thái theo người dùng.
Hậu điều kiện thành công: Lưu lần xem gần nhất, không tạo bảng lịch sử riêng cho từng lượt.
Dữ liệu liên quan: customer_product_states, shop_products.
Đối chiếu mã/giao diện: ShopCustomerService.java; useShopCustomer.js.

### UC-38. So sánh sản phẩm

Tác nhân: Khách vãng lai, USER.
Mục tiêu: Người mua có thông tin để chọn sản phẩm.
Tiền điều kiện: Có sản phẩm trong danh mục.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Chọn sản phẩm để so sánh.
2. Mở giao diện so sánh.
3. Đối chiếu các thông tin hiển thị.
4. Bỏ sản phẩm khỏi danh sách khi cần.
Luồng thay thế/ngoại lệ: Chưa đủ lựa chọn hoặc vượt giới hạn giao diện: xử lý theo thông báo của giao diện; không ghi thêm bảng so sánh.
Hậu điều kiện thành công: Người mua có thông tin để chọn sản phẩm.
Dữ liệu liên quan: shop_products; trạng thái so sánh ở giao diện.
Đối chiếu mã/giao diện: gym-frontend/src/components/shop; useShopCustomer.js.

### UC-39. Quản lý địa chỉ nhận hàng

Tác nhân: USER.
Mục tiêu: Sổ địa chỉ và địa chỉ mặc định được cập nhật.
Tiền điều kiện: Đã đăng nhập.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở sổ địa chỉ.
2. Nhập người nhận, điện thoại và địa chỉ.
3. Chọn mặc định nếu cần.
4. Lưu, sửa hoặc xóa địa chỉ thuộc tài khoản.
Luồng thay thế/ngoại lệ: Thiếu trường bắt buộc, quá giới hạn 20 địa chỉ hoặc địa chỉ của người khác: từ chối.
Hậu điều kiện thành công: Sổ địa chỉ và địa chỉ mặc định được cập nhật.
Dữ liệu liên quan: customer_addresses.
Đối chiếu mã/giao diện: ShopCustomerService.java; ShopExperienceController.java.

### UC-40. Áp dụng mã giảm giá

Tác nhân: Khách vãng lai, USER, STAFF, ADMIN.
Mục tiêu: Mã hợp lệ được đưa vào yêu cầu đặt hàng/bán tại quầy.
Tiền điều kiện: Có hàng được chọn và mã cần kiểm tra.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Xem hoặc nhập mã.
2. Hệ thống kiểm tra thời hạn, trạng thái, lượt dùng và phạm vi.
3. Tính phần giảm trên hàng đủ điều kiện.
4. Hiển thị tổng tiền dự kiến.
Luồng thay thế/ngoại lệ: Mã hết hạn/hết lượt, không đủ giá trị hoặc sai phạm vi: thông báo không áp dụng. Việc xem trước chưa thay thế kiểm tra lúc tạo đơn.
Hậu điều kiện thành công: Mã hợp lệ được đưa vào yêu cầu đặt hàng/bán tại quầy.
Dữ liệu liên quan: vouchers, voucher_scope_products, shop_products.
Đối chiếu mã/giao diện: VoucherService.java; ShopController.java; PosService.java.

### UC-41. Đặt hàng trực tuyến

Tác nhân: USER.
Mục tiêu: Đơn và thông tin hàng tại thời điểm mua được lưu; chưa mặc nhiên là đã thu tiền.
Tiền điều kiện: Đã đăng nhập; giỏ có hàng; có thông tin nhận.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở thanh toán.
2. Chọn/nhập địa chỉ, mã giảm giá và phương thức.
3. Gửi yêu cầu đặt hàng.
4. Máy chủ kiểm tra hàng, khóa dữ liệu tồn và tính lại tiền.
5. Tạo đơn, chi tiết và nhật ký, trừ tồn.
6. Xóa giỏ sau khi lưu thành công và trả thông tin đơn.
Luồng thay thế/ngoại lệ: Giỏ trống, hàng ngừng bán, thiếu tồn/địa chỉ, voucher sai hoặc phương thức chưa bật: từ chối và không chốt đơn. COD tạo CONFIRMED; các phương thức trả trước tạo PENDING_PAYMENT.
Hậu điều kiện thành công: Đơn và thông tin hàng tại thời điểm mua được lưu; chưa mặc nhiên là đã thu tiền.
Dữ liệu liên quan: shop_orders, shop_order_items, shop_cart_items, shop_products, product_variants, vouchers, shop_order_events, shop_inventory_movements.
Đối chiếu mã/giao diện: ShopService.checkout; POST /api/shop/orders.

### UC-42. Thanh toán đơn trực tuyến

Tác nhân: USER; cổng thanh toán (phụ).
Mục tiêu: Đơn chỉ được ghi đã thanh toán khi kết quả được xác nhận hợp lệ. COD thu tiền ở luồng giao hàng, không gọi cổng thanh toán.
Tiền điều kiện: Có đơn thuộc người dùng, đang chờ thanh toán; phương thức được cấu hình.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở thông tin thanh toán của đơn.
2. Với chuyển khoản, xem QR/nội dung.
3.  với ví, tạo phiên và mở URL thanh toán.
4. Người mua thực hiện thanh toán.
5. Hệ thống nhận/xác nhận kết quả qua luồng phù hợp.
6. Cập nhật trạng thái và lịch sử khi xác nhận hợp lệ.
Luồng thay thế/ngoại lệ: Ví chưa cấu hình: không khả dụng. Callback ví sai chữ ký/merchant/số tiền: từ chối. Callback lặp: không ghi thu tiền lần hai. Tiền đến sau hủy/hết hạn: ghi sự kiện đối soát. QR hết hạn sau 15 phút; trang chuyển hướng không chứng minh đã trả tiền.
Hậu điều kiện thành công: Đơn chỉ được ghi đã thanh toán khi kết quả được xác nhận hợp lệ. COD thu tiền ở luồng giao hàng, không gọi cổng thanh toán.
Dữ liệu liên quan: shop_orders, shop_order_events; hoàn tồn khi hết hạn có shop_inventory_movements.
Đối chiếu mã/giao diện: ShopPaymentService.java; ShopPaymentController.java; ShopExpiryScheduler.java.

### UC-43. Theo dõi và tra cứu đơn hàng

Tác nhân: Khách vãng lai, USER.
Mục tiêu: Người mua biết trạng thái đơn, không thay đổi đơn.
Tiền điều kiện: Có đơn; tài khoản cần quyền sở hữu hoặc người tra cứu có mã đơn và số điện thoại khớp.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở lịch sử đơn của tài khoản hoặc trang tra cứu.
2. Chọn đơn hoặc nhập mã đơn và số điện thoại.
3. Hệ thống kiểm tra quyền/thông tin.
4. Hiển thị hàng, phân loại, tổng tiền, trạng thái và lịch sử.
Luồng thay thế/ngoại lệ: Sai mã/điện thoại hoặc không có quyền: từ chối. Khách được tra cứu không đồng nghĩa được đặt đơn trực tuyến không đăng nhập.
Hậu điều kiện thành công: Người mua biết trạng thái đơn, không thay đổi đơn.
Dữ liệu liên quan: shop_orders, shop_order_items, shop_order_events.
Đối chiếu mã/giao diện: ShopService.lookupOrder/myOrders/order; /tra-cuu-don.

### UC-44. Hủy đơn hàng

Tác nhân: USER.
Mục tiêu: Đơn hủy và lượng tồn được trả lại đúng một lần.
Tiền điều kiện: Là chủ đơn; đơn PENDING_PAYMENT hoặc COD đang CONFIRMED.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở đơn đủ điều kiện.
2. Yêu cầu hủy.
3. Hệ thống khóa đơn và kiểm tra lại điều kiện.
4. Hoàn tồn, chuyển CANCELLED và ghi nhật ký.
5. Hiển thị trạng thái mới.
Luồng thay thế/ngoại lệ: Đơn đã xử lý/không thuộc tài khoản: từ chối. Yêu cầu lặp không được hoàn tồn thêm lần nữa.
Hậu điều kiện thành công: Đơn hủy và lượng tồn được trả lại đúng một lần.
Dữ liệu liên quan: shop_orders, shop_order_items, shop_products, product_variants, shop_order_events, shop_inventory_movements.
Đối chiếu mã/giao diện: ShopService.cancel.

### UC-45. Đánh giá sản phẩm sau mua

Tác nhân: USER.
Mục tiêu: Lưu đánh giá và danh sách URL ảnh.
Tiền điều kiện: Đơn thuộc tài khoản và chứa sản phẩm; online DELIVERED/COMPLETED hoặc POS PAID/COMPLETED.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Chọn sản phẩm trong đơn đủ điều kiện.
2. Nhập 1–5 sao, nhận xét và ảnh tùy chọn.
3. Gửi đánh giá.
4. Hệ thống kiểm tra và lưu.
5. Đánh giá xuất hiện trong danh sách sản phẩm.
Luồng thay thế/ngoại lệ: Đã đánh giá cùng người/đơn/sản phẩm, sai chủ đơn, sai trạng thái hoặc số sao: từ chối. Tối đa 8 ảnh; ảnh tải lên JPG/PNG/WebP, tối đa 5 MB mỗi ảnh.
Hậu điều kiện thành công: Lưu đánh giá và danh sách URL ảnh.
Dữ liệu liên quan: product_reviews, shop_orders, shop_order_items, users, shop_products.
Đối chiếu mã/giao diện: ProductReviewService.java; ShopExperienceController.image.

### UC-46. Quản lý sản phẩm cửa hàng

Tác nhân: ADMIN.
Mục tiêu: Danh mục được cập nhật; giữ được thông tin đơn đã chốt.
Tiền điều kiện: Đăng nhập có quyền quản trị.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở danh sách quản lý.
2. Tạo hoặc chọn sản phẩm.
3. Cập nhật thông tin, giá, ảnh, tồn và trạng thái.
4. Lưu.
5. Hệ thống cập nhật danh mục và nhật ký tồn khi có thay đổi.
Luồng thay thế/ngoại lệ: Dữ liệu không hợp lệ: từ chối. Thao tác ngừng bán dùng cờ trạng thái; không mô tả là xóa lịch sử đơn.
Hậu điều kiện thành công: Danh mục được cập nhật; giữ được thông tin đơn đã chốt.
Dữ liệu liên quan: shop_products, shop_inventory_movements.
Đối chiếu mã/giao diện: ShopService.saveProduct/hideProduct; /admin/shop.

### UC-47. Quản lý thuộc tính và biến thể

Tác nhân: ADMIN.
Mục tiêu: Thuộc tính, tổ hợp giá trị, giá và tồn của biến thể được lưu.
Tiền điều kiện: Đăng nhập quản trị; có sản phẩm cần phân loại.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Tạo/chọn thuộc tính và giá trị.
2. Tạo tổ hợp biến thể cho sản phẩm.
3. Nhập SKU, giá riêng và tồn.
4. Lưu hoặc cập nhật trạng thái biến thể.
5. Hệ thống dùng biến thể cho giỏ, online và POS.
Luồng thay thế/ngoại lệ: Biến thể không hợp lệ hoặc không thuộc sản phẩm: từ chối theo dịch vụ. Biến thể ngừng bán không bị xóa khỏi lịch sử mua.
Hậu điều kiện thành công: Thuộc tính, tổ hợp giá trị, giá và tồn của biến thể được lưu.
Dữ liệu liên quan: product_attributes, product_attribute_values, product_variants, variant_attribute_values, shop_inventory_movements.
Đối chiếu mã/giao diện: ProductAttributeService.java; ProductVariantService.java; /admin/product-attributes.

### UC-48. Quản lý mã giảm giá

Tác nhân: ADMIN.
Mục tiêu: Voucher và phạm vi áp dụng được cập nhật.
Tiền điều kiện: Đăng nhập quản trị.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở quản lý voucher.
2. Nhập mã, loại và mức giảm.
3. Đặt thời gian, giới hạn lượt, điều kiện và phạm vi.
4. Lưu hoặc chỉnh trạng thái.
5. Hệ thống dùng cấu hình khi kiểm tra mã mua hàng.
Luồng thay thế/ngoại lệ: Mã/điều kiện không hợp lệ: trả lỗi theo dịch vụ. Phạm vi sản phẩm được lưu ở bảng collection; không có thêm bảng khuyến mãi riêng.
Hậu điều kiện thành công: Voucher và phạm vi áp dụng được cập nhật.
Dữ liệu liên quan: vouchers, voucher_scope_products.
Đối chiếu mã/giao diện: VoucherService.java; VoucherAdmin.vue.

### UC-49. Xử lý đơn hàng trực tuyến

Tác nhân: STAFF, ADMIN.
Mục tiêu: Trạng thái và lịch sử đơn phản ánh xử lý vận hành.
Tiền điều kiện: Đăng nhập có quyền; đơn tồn tại.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở danh sách đơn.
2. Xem hàng và lịch sử.
3. Chọn trạng thái tiếp theo hợp lệ.
4. Hệ thống kiểm tra chuyển trạng thái.
5. Lưu đơn và sự kiện.
Luồng thay thế/ngoại lệ: Chuyển trạng thái sai: từ chối. COD đi CONFIRMED → PREPARING → SHIPPING → DELIVERED → COMPLETED; ghi paid_at khi xác nhận giao/thu tiền. Không tự coi mọi đơn CONFIRMED là đã trả tiền.
Hậu điều kiện thành công: Trạng thái và lịch sử đơn phản ánh xử lý vận hành.
Dữ liệu liên quan: shop_orders, shop_order_events; shop_inventory_movements nếu hoàn tồn.
Đối chiếu mã/giao diện: ShopService.updateStatus; PUT /api/shop/admin/orders/{id}/status.

### UC-50. Bán hàng tại quầy

Tác nhân: STAFF, ADMIN.
Mục tiêu: Lưu đơn POS, nhân viên tạo, chi tiết hàng và nhật ký tồn.
Tiền điều kiện: Đăng nhập có quyền bán; có ít nhất một dòng hàng.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Chọn tài khoản khách hoặc nhập tên khách vãng lai.
2. Chọn sản phẩm, phân loại và số lượng.
3. Nhập phương thức thanh toán và mã giảm giá nếu có.
4. Xác nhận bán.
5. Hệ thống kiểm tra tồn, tính tiền, tạo đơn POS và trừ tồn.
Luồng thay thế/ngoại lệ: Thiếu thông tin khách, số lượng không dương, dòng trùng, thiếu/sai phân loại hoặc thiếu tồn: từ chối. Không thêm tiền điều kiện “bắt buộc đang vào ca” vì PosService hiện không kiểm tra điều này.
Hậu điều kiện thành công: Lưu đơn POS, nhân viên tạo, chi tiết hàng và nhật ký tồn.
Dữ liệu liên quan: shop_orders, shop_order_items, shop_products, product_variants, vouchers, shop_order_events, shop_inventory_movements.
Đối chiếu mã/giao diện: PosService.java; PosController.java.

### UC-51. Xem báo cáo cửa hàng

Tác nhân: ADMIN.
Mục tiêu: Xem báo cáo; không tạo bảng tổng hợp. Doanh thu gồm phí vận chuyển, trừ giảm giá; chưa phải lợi nhuận.
Tiền điều kiện: Đăng nhập quản trị.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở báo cáo.
2. Chọn khoảng ngày.
3. Hệ thống tổng hợp đơn theo ngày tạo.
4. Hiển thị doanh thu online/POS, theo ngày và hàng bán chạy.
Luồng thay thế/ngoại lệ: Ngày kết thúc trước ngày bắt đầu: từ chối. Không có dữ liệu: hiển thị số liệu trống/0. Chỉ cộng đơn có paid_at và không CANCELLED/EXPIRED.
Hậu điều kiện thành công: Xem báo cáo; không tạo bảng tổng hợp. Doanh thu gồm phí vận chuyển, trừ giảm giá; chưa phải lợi nhuận.
Dữ liệu liên quan: shop_orders, shop_order_items.
Đối chiếu mã/giao diện: ShopExperienceController.statistics; /admin/shop-reports.

### UC-52. Theo dõi tồn thấp và nhật ký kho

Tác nhân: ADMIN.
Mục tiêu: Quản trị biết hàng cần bổ sung và nguồn thay đổi tồn.
Tiền điều kiện: Đăng nhập quản trị.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở báo cáo/kho.
2. Xem sản phẩm hoặc biến thể hoạt động có tồn ≤ 5.
3. Lọc nhật ký theo sản phẩm và trang.
4. Xem tồn trước/sau, lý do và đơn liên quan.
Luồng thay thế/ngoại lệ: Chưa có lịch sử: danh sách trống. Nhật ký chỉ có từ khi triển khai, không dựng lại dữ liệu cũ.
Hậu điều kiện thành công: Quản trị biết hàng cần bổ sung và nguồn thay đổi tồn.
Dữ liệu liên quan: shop_inventory_movements, shop_products, product_variants.
Đối chiếu mã/giao diện: ShopExperienceController.inventory; ShopAuditService.java.

### UC-53. Phân công và xem lịch làm việc

Tác nhân: ADMIN.
Mục tiêu: Tạo work_shifts để nhân viên theo dõi và vào/kết ca.
Tiền điều kiện: Đăng nhập quản trị; có tài khoản được phân công.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở lịch làm việc.
2. Chọn nhân viên, ngày, giờ bắt đầu/kết thúc.
3. Lưu ca.
4. Hệ thống trả ca được phân công.
5. Quản trị xem danh sách lịch.
Luồng thay thế/ngoại lệ: Không tìm thấy tài khoản hoặc giờ kết thúc trước bắt đầu: từ chối. Không mô tả đã chống trùng ca khi code chưa có kiểm tra tương ứng.
Hậu điều kiện thành công: Tạo work_shifts để nhân viên theo dõi và vào/kết ca.
Dữ liệu liên quan: work_shifts, users.
Đối chiếu mã/giao diện: WorkShiftService.assign; WorkShiftController.java.

### UC-54. Vào ca làm việc

Tác nhân: STAFF, ADMIN.
Mục tiêu: Ghi check_in_at, cash_at_start và trạng thái CHECKED_IN.
Tiền điều kiện: Có ca được phép thao tác và chưa check-in.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở lịch ca.
2. Chọn ca.
3. Nhập tiền mặt đầu ca.
4. Gửi check-in.
5. Hệ thống ghi thời điểm và trạng thái vào ca.
Luồng thay thế/ngoại lệ: Không sở hữu ca và không phải quản trị, hoặc đã check-in: từ chối. Không nhập tiền đầu ca: mã hiện tại dùng 0.
Hậu điều kiện thành công: Ghi check_in_at, cash_at_start và trạng thái CHECKED_IN.
Dữ liệu liên quan: work_shifts.
Đối chiếu mã/giao diện: WorkShiftService.checkIn/owned.

### UC-55. Kết ca và đối soát tiền

Tác nhân: STAFF, ADMIN.
Mục tiêu: Lưu doanh thu tiền mặt/chuyển khoản, tiền dự kiến, kiểm đếm, chênh lệch và trạng thái COMPLETED.
Tiền điều kiện: Ca được phép thao tác, đã vào ca và chưa kết ca.
Kích hoạt: tác nhân chọn chức năng tương ứng hoặc thực hiện thao tác nêu ở bước 1.
Luồng chính:
1. Mở ca đang làm.
2. Nhập tiền mặt kiểm đếm và ghi chú.
3. Gửi kết ca.
4. Hệ thống tổng hợp doanh thu POS theo nhân viên/khoảng thời gian của ca.
5. Tính tiền mặt dự kiến và chênh lệch.
6. Lưu kết ca.
Luồng thay thế/ngoại lệ: Chưa vào ca, đã kết ca hoặc không có quyền: từ chối. Chênh lệch được lưu để đối soát, không tự điều chỉnh đơn.
Hậu điều kiện thành công: Lưu doanh thu tiền mặt/chuyển khoản, tiền dự kiến, kiểm đếm, chênh lệch và trạng thái COMPLETED.
Dữ liệu liên quan: work_shifts, shop_orders.
Đối chiếu mã/giao diện: WorkShiftService.checkOut.

## 5. Sơ đồ và quan hệ UML

Ba tệp PlantUML đi kèm chia theo mua hàng, quản trị/vận hành và ca làm. Các sơ đồ dùng cùng mã UC-33…UC-55. Nhãn tác nhân biểu thị vai trò nghiệp vụ; quyền truy cập chi tiết theo mục 2 và từng đặc tả.

UC-40 Áp dụng mã giảm giá mở rộng UC-41 Đặt hàng trực tuyến và UC-50 Bán tại quầy khi người mua/nhân viên nhập mã. Trong PlantUML, mũi tên <<extend>> đi từ UC-40 đến UC-41 hoặc UC-50. Đăng nhập là tiền điều kiện cho các chức năng cần tài khoản, không vẽ mọi use case <<include>> Đăng nhập. Thanh toán trực tuyến là bước có thể thực hiện sau khi tạo đơn; không vẽ UC-41 luôn include UC-42 vì COD không thanh toán ngay. Hủy/đánh giá là mục tiêu độc lập có điều kiện trạng thái; không phải bước bắt buộc của mọi đơn.

Scheduler hết hạn và việc trừ/hoàn tồn là xử lý nội bộ, không phải người dùng hay hệ thống bên ngoài để vẽ làm tác nhân. Database cũng không phải tác nhân. Không thêm use case hoàn tiền tự động hoặc mua online không đăng nhập vì chưa có chức năng tương ứng.

## 6. Tiêu chí nghiệm thu để đưa vào phần kiểm thử

Các tiêu chí dưới đây là trường hợp cần kiểm tra, không phải tuyên bố tất cả đã chạy đạt trong lần viết tài liệu. Các kết quả kiểm thử có sẵn giữ theo báo cáo nâng cấp trước đó.

| Nhóm | Tình huống cần kiểm tra | Kết quả mong đợi |
|---|---|---|
| UC-34/35/41/50 | Hai biến thể cùng sản phẩm có giá/tồn khác nhau | Giỏ, đơn online và POS lưu đúng variant_id, tên phân loại, đơn giá và tồn của biến thể. |
| UC-41 | Hai người đặt phần tồn cuối hoặc dữ liệu giá cũ | Máy chủ kiểm tra lại, không bán vượt tồn và không lấy tổng tiền từ khách làm nguồn tin cậy. |
| UC-41/49 | Đơn COD | Không có paid_at khi mới CONFIRMED; chỉ ghi khi xác nhận giao/thu tiền. |
| UC-42 | Callback hợp lệ, sai chữ ký, sai số tiền, lặp hoặc đến muộn | Chỉ chấp nhận hợp lệ; không thu hai lần; tiền đến muộn được ghi đối soát. |
| UC-44 | Hủy đúng chủ/khác chủ, hủy lặp, hủy sau xử lý | Chỉ hủy trạng thái cho phép; tồn được hoàn đúng một lần. |
| UC-39 | Sửa/xóa địa chỉ khác người | Từ chối và không thay đổi địa chỉ người khác. |
| UC-45 | Đơn chưa giao, sản phẩm không thuộc đơn, đánh giá lặp | Từ chối; đánh giá hợp lệ có 1–5 sao và ảnh đúng giới hạn. |
| UC-51/52 | Đơn hủy/hết hạn, COD chưa thu tiền, biến thể tồn 5 | Không cộng sai doanh thu; tồn 5 được cảnh báo. |
| UC-54/55 | Kết ca chưa vào, kết ca lặp, tiền kiểm đếm khác dự kiến | Từ chối trạng thái sai; lưu đúng số chênh lệch khi kết ca hợp lệ. |

## 7. Nguồn đối chiếu

Báo cáo PDF: trang 18–20 chứa UC-01…UC-32. Mã nguồn chính: gym-management/src/main/java/com/example/gymmanagement/shop; gym-management/src/main/java/com/example/gymmanagement/shift; gym-frontend/src/views; gym-frontend/src/components/shop; gym-frontend/src/composables/useShopCustomer.js. Mã UC-33…UC-55 là số hiệu đề xuất để ghép báo cáo, không phải ID tồn tại trong code.