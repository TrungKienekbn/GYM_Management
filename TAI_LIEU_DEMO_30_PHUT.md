# KỊCH BẢN DEMO GYMPRO TRONG 30 PHÚT

## 1. Chuẩn bị trước khi trình bày

- Chạy backend tại `http://localhost:8080` và frontend tại `http://localhost:5173`.
- Chạy file `gym-management/src/main/resources/h2-test-data.sql` trong H2 Console.
- Mở sẵn hai cửa sổ trình duyệt: một cửa sổ user và một cửa sổ admin.
- Tài khoản user: `fulltest@gym.com` / `password`.
- Tài khoản admin: `admin@gym.com` / `admin123`.
- Kiểm tra tài khoản Full Test đã có VIP, 2 giáo án hoàn thành và 16 buổi checkout.
- Đặt mức zoom trình duyệt khoảng 80–90% để giao diện hiển thị đầy đủ.
- Không dành thời gian gõ dữ liệu dài trong lúc demo; chuẩn bị sẵn nội dung cần nhập.

## 2. Mục tiêu bài trình bày

> GymPro giải quyết việc quản lý luyện tập rời rạc bằng một hệ thống duy nhất: quản lý người dùng, tạo giáo án cá nhân hóa, theo dõi từng buổi tập, ghi nhận tiến độ, hỗ trợ dinh dưỡng, phân biệt gói thường/VIP và cung cấp công cụ quản trị.

## 3. Timeline chính xác 30 phút

### 00:00–02:00 — Giới thiệu đề tài

Thao tác:

1. Mở trang Home.
2. Giới thiệu ngắn đối tượng sử dụng: người tập và quản trị viên.
3. Chuyển sang trang đăng nhập.

Lời nói gợi ý:

> Nhóm em xây dựng GymPro, một hệ thống quản lý và hỗ trợ luyện tập cá nhân. Người dùng có thể quản lý hồ sơ thể trạng, nhận giáo án, checkout buổi tập và theo dõi tiến độ. Admin quản lý dữ liệu nền, người dùng, giao dịch và hỗ trợ. Điểm trọng tâm của hệ thống là dữ liệu của người dùng được liên kết xuyên suốt, thay vì mỗi chức năng hoạt động riêng lẻ.

### 02:00–04:00 — Đăng nhập và phân quyền

Thao tác:

1. Chỉ vào hai tài khoản demo trên màn hình.
2. Đăng nhập bằng `fulltest@gym.com` / `password`.
3. Giới thiệu menu user và biểu tượng gói VIP.
4. Nhắc nhanh chức năng đăng nhập bằng email và 4 số cuối điện thoại.

Điểm cần nhấn mạnh:

- Mật khẩu được mã hóa BCrypt.
- Backend xác thực bằng Spring Security và JWT.
- Route user/admin được bảo vệ theo vai trò.
- Trạng thái chưa có gói/giáo án là trạng thái bình thường, không hiện lỗi kỹ thuật.

### 04:00–06:30 — Dashboard và hồ sơ thể trạng

Thao tác:

1. Mở Dashboard, giới thiệu số liệu tổng quan.
2. Mở Hồ sơ cá nhân.
3. Chỉ vào chiều cao, cân nặng, BMI, phần trăm mỡ, mục tiêu và số ngày có thể tập.

Lời nói gợi ý:

> Hồ sơ là dữ liệu đầu vào cho các đề xuất của hệ thống. Khi người dùng thay đổi cân nặng hoặc mục tiêu, dữ liệu được lưu về backend và được dùng trong giáo án, tiến độ và dinh dưỡng. Hệ thống không chỉ lưu thông tin tài khoản mà còn quản lý trạng thái luyện tập theo thời gian.

### 06:30–11:00 — Giáo án và sự khác biệt VIP

Thao tác:

1. Mở Giáo án tập.
2. Giới thiệu cấu trúc giáo án: tuần → buổi/ngày → bài tập → sets/reps/thời gian nghỉ.
3. Chỉ vào ghi chú giáo án.
4. Trình bày việc lựa chọn bài từ thư viện để thêm vào buổi tập phụ.
5. Giải thích gói thường giữ nguyên giáo án; VIP được tự động điều chỉnh theo kết quả tuần.
6. Nếu có luồng hoàn thành tuần, chỉ vào hộp xác nhận trước khi chuyển tuần.

Lời nói gợi ý:

> Giáo án được tạo dựa trên mục tiêu, trình độ và lịch rảnh. Mỗi bài có số hiệp, số lần lặp, thời gian nghỉ và mức tạ đề xuất. Người dùng có thể chọn thêm bài yêu thích vào buổi phụ. Khi hoàn thành tuần, hệ thống hỏi xác nhận thay vì tự chuyển ngay. Với VIP, giáo án có thể được điều chỉnh theo dữ liệu checkout; gói thường vẫn sử dụng đầy đủ chức năng tập cơ bản nhưng không có tự động điều chỉnh nâng cao.

### 11:00–15:00 — Luồng bắt đầu tập và checkout

Thao tác:

1. Mở Buổi tập.
2. Chọn một buổi chưa thực hiện hoặc trình bày trên dữ liệu mẫu.
3. Giải thích trạng thái SCHEDULED → COMPLETED/SKIPPED.
4. Mở form checkout, chỉ vào tỷ lệ hoàn thành, cân nặng và phần trăm mỡ.
5. Nhấn mạnh hệ thống không cho bắt đầu hai buổi cùng lúc.
6. Giải thích nếu thoát trước checkout, người dùng có thể quay lại hoàn thành hoặc xử lý buổi đó, không tạo trùng.

Lời nói gợi ý:

> Checkout là điểm liên kết dữ liệu quan trọng. Sau buổi tập, hệ thống lưu mức hoàn thành từng bài, calories, thời lượng và thể trạng cuối tuần. Backend kiểm tra thứ tự và buổi đang mở để tránh một người bắt đầu đồng thời hai buổi hoặc ghi nhận trùng dữ liệu.

### 15:00–18:00 — Lịch sử 2 giáo án và tiến độ

Thao tác:

1. Hiển thị hai giáo án đã hoàn thành của tài khoản Full Test.
2. Nêu giáo án thứ nhất cách khoảng 3 tháng, giáo án thứ hai cách khoảng 2 tháng.
3. Mở Theo dõi tiến độ và chỉ vào biểu đồ/lịch sử.
4. Dùng filter hoặc phân trang nếu danh sách đủ dài.

Điểm dữ liệu mẫu:

- 2 giáo án × 4 tuần.
- 2 buổi/tuần, tổng 16 buổi hoàn thành.
- Có thời gian check-in/out, calories, tỷ lệ hoàn thành, cân nặng và body fat.

Lời nói gợi ý:

> Dữ liệu lịch sử không phải số hiển thị tĩnh. Mỗi điểm được tổng hợp từ các bản ghi tiến độ và checkout. Tài khoản Full Test có hai giáo án trong 2–3 tháng để minh họa khả năng xem lại quá trình thay đổi, lọc dữ liệu và đánh giá mức tuân thủ.

### 18:00–20:00 — Bài tập và buổi tập phụ

Thao tác:

1. Mở Thư viện bài tập.
2. Lọc theo tên/nhóm cơ/độ khó.
3. Mở chi tiết một bài.
4. Chọn thêm bài đó vào buổi tập phụ.
5. Chỉ vào phân trang.

Điểm cần nói:

- Bài tập bị ẩn không xuất hiện với user nhưng không bị xóa vật lý.
- Lịch sử cũ vẫn giữ được liên kết đến bài tập.

### 20:00–22:00 — Dinh dưỡng

Thao tác:

1. Mở Thực đơn món ăn.
2. Tìm/lọc một món ăn.
3. Thay đổi khẩu phần và chỉ ra calories, protein, carb, fat được quy đổi.

Lời nói gợi ý:

> Dữ liệu dinh dưỡng được chuẩn hóa theo 100 gram. Khi người dùng đổi khối lượng, hệ thống nhân theo tỷ lệ `khối lượng / 100`, nhờ đó các chất dinh dưỡng thay đổi đồng nhất và dễ kiểm tra.

### 22:00–24:30 — Gói VIP, thanh toán và trang phục

Thao tác:

1. Mở Gói tập, so sánh gói thường và VIP.
2. Mở lịch sử hóa đơn đã thanh toán của Full Test.
3. Mở cửa hàng trang phục thú cưng, chỉ vào món miễn phí và món trả phí đã mở khóa.

Điểm cần nhấn mạnh:

- Thanh toán tạo hóa đơn PENDING trước.
- Webhook ngân hàng xác nhận đúng mã và số tiền rồi mới chuyển PAID.
- Chỉ sau PAID mới kích hoạt VIP hoặc cấp quyền sở hữu trang phục.
- Không dựa vào thông báo frontend để quyết định đã thanh toán.

### 24:30–28:30 — Phần quản trị

Thao tác:

1. Đăng xuất hoặc chuyển sang cửa sổ admin đã chuẩn bị sẵn.
2. Mở Dashboard admin.
3. Trình bày nhanh theo thứ tự:
   - Quản lý/filter user.
   - Quản lý bài tập: filter, phân trang, ẩn và khôi phục.
   - Quản lý món ăn và dữ liệu theo 100 g.
   - Tạo giáo án mẫu và ghi chú.
   - Quản lý hóa đơn/gói tập.
   - Gửi thông báo có bước xác nhận.
   - Đánh giá và hỗ trợ/chat.
4. Nhấn mạnh ID tự tăng vẫn đúng sau khi xóa hoặc ẩn bản ghi.

Lời nói gợi ý:

> Admin quản lý dữ liệu nền và các nghiệp vụ cần kiểm soát. Với bài tập, hệ thống dùng ẩn/khôi phục thay vì xóa cứng để bảo toàn lịch sử. ID là khóa kỹ thuật tự tăng, không dùng làm số thứ tự hiển thị nên việc thiếu một ID sau khi xóa là bình thường và an toàn.

### 28:30–30:00 — Kiến trúc và kết luận

Lời nói gợi ý:

> Frontend sử dụng Vue 3, Pinia, Vue Router, Axios và Element Plus. Backend sử dụng Java 17, Spring Boot, Spring Security, JWT và Spring Data JPA; dữ liệu demo lưu bằng H2. Kiến trúc tách frontend, API, service và repository giúp từng phần có trách nhiệm rõ ràng. GymPro hiện đã bao phủ luồng từ tạo hồ sơ, tập luyện, checkout, theo dõi tiến độ đến thanh toán và quản trị. Hướng phát triển tiếp theo là chuyển sang cơ sở dữ liệu production, bổ sung kiểm thử tự động và triển khai dịch vụ thanh toán/email thực tế.

## 4. Phương án dự phòng khi demo lỗi

- Nếu API chưa chạy: trình bày giao diện và chuyển sang mô tả dữ liệu Full Test; không sửa lỗi trực tiếp trước lớp.
- Nếu thanh toán webhook không hoạt động: mở hóa đơn PAID có sẵn và giải thích luồng trạng thái.
- Nếu không thể tạo giáo án mới: dùng hai giáo án lịch sử trong Full Test.
- Nếu checkout không thực hiện được: mở buổi COMPLETED có sẵn, trình bày các trường dữ liệu đã ghi nhận.
- Nếu mất mạng: hệ thống local vẫn demo được; các dịch vụ bên ngoài được thay bằng dữ liệu đã chuẩn bị.
- Luôn giữ H2 Console ở tab riêng để có thể chứng minh dữ liệu thật khi giảng viên yêu cầu.

## 5. Những điều không nên làm trong 30 phút

- Không đọc toàn bộ nội dung trên màn hình.
- Không demo mọi nút; ưu tiên luồng nghiệp vụ chính.
- Không nhập một giáo án dài từ đầu.
- Không nói “AI” nếu chức năng thực tế là thuật toán/rule-based mà không dùng mô hình AI bên ngoài.
- Không khẳng định hệ thống đã production-ready nếu đang dùng H2 và tài khoản demo.
- Không tranh luận khi gặp lỗi; ghi nhận giới hạn và chuyển sang dữ liệu dự phòng.
