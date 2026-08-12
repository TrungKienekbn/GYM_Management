# ĐẶC TẢ CHỨC NĂNG HỆ THỐNG GYMPRO

## 1. Phạm vi hệ thống

GymPro là hệ thống hỗ trợ tập luyện cá nhân hóa, quản lý giáo án, buổi tập, tiến độ, dinh dưỡng, gói thành viên, thanh toán chuyển khoản, bán sản phẩm tập luyện và hỗ trợ người dùng.

## 2. Tác nhân

| Tác nhân | Quyền chính |
|---|---|
| Khách | Xem trang giới thiệu, đăng ký, đăng nhập, xem đánh giá công khai |
| Người dùng thường | Quản lý hồ sơ, nhận giáo án cơ bản, tập luyện, ghi tiến độ, mua hàng |
| Người dùng VIP | Toàn bộ quyền thường và các quyền VIP theo cấu hình hệ thống |
| Quản trị viên | Quản lý người dùng, bài tập, món ăn, giáo án mẫu, cấu hình, thanh toán, đơn hàng và hỗ trợ |
| SePay | Gửi webhook giao dịch ngân hàng để hệ thống tự xác nhận thanh toán |
| Bộ lập lịch | Nhắc lịch tập và xử lý giao dịch/hóa đơn hết hạn |

## 3. Quy tắc nghiệp vụ chung

- API riêng tư yêu cầu JWT hợp lệ; API quản trị yêu cầu `ROLE_ADMIN`.
- Dữ liệu của mỗi người dùng phải được lọc theo tài khoản đang đăng nhập.
- Hồ sơ phải hợp lệ về tuổi, chiều cao, cân nặng, mục tiêu, thể lực, lịch tập và hạn chế chấn thương.
- Một người dùng chỉ được ghi nhận tiến độ một lần trong một ngày.
- Giáo án được tạo theo hồ sơ, mục tiêu, cấp thể lực, số buổi, nhóm cơ, chấn thương và điểm phù hợp của bài tập; không chọn ngẫu nhiên thuần túy.
- Khi các bài có cùng điểm, hệ thống ưu tiên cân bằng nhóm cơ, tránh lặp bài, phù hợp thiết bị và phân bổ tải giữa các buổi.
- Một tuần chỉ chuyển tiếp sau khi đủ số buổi đã hoàn thành hoặc bỏ qua và người dùng xác nhận đánh giá tuần.
- Thanh toán VIP chỉ dùng chuyển khoản; SePay xác nhận tự động theo nội dung và số tiền giao dịch.
- Admin không được xóa đánh giá của người dùng, chỉ được phản hồi.
- Xóa bài tập/sản phẩm đang được tham chiếu sử dụng cơ chế ẩn hoặc ngừng hoạt động để giữ lịch sử.

## 4. Đặc tả chức năng phía người dùng

### UC01 — Đăng ký tài khoản

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Khách |
| Tiền điều kiện | Email chưa tồn tại; dữ liệu đúng định dạng |
| Luồng chính | Nhập thông tin → hệ thống kiểm tra → mã hóa mật khẩu → tạo tài khoản thường → gửi kết quả |
| Ngoại lệ | Email trùng, mật khẩu yếu, dữ liệu thiếu hoặc sai giới hạn |
| Hậu điều kiện | Tài khoản được tạo nhưng không phát thông báo nghiệp vụ không liên quan như “chưa có gói” |

### UC02 — Đăng nhập và đăng xuất

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng, admin |
| Tiền điều kiện | Tài khoản tồn tại và đang hoạt động |
| Luồng chính | Nhập email/mật khẩu → xác thực → phát JWT → tải đúng dữ liệu tài khoản → chuyển trang theo vai trò |
| Ngoại lệ | Sai thông tin, tài khoản khóa, token hết hạn |
| Hậu điều kiện | Khi đăng xuất/đổi tài khoản phải xóa store, cache và dữ liệu tài khoản trước |

### UC03 — Đặt lại mật khẩu bằng 4 số cuối điện thoại

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Tài khoản có số điện thoại đã lưu |
| Luồng chính | Nhập email và 4 số cuối → kiểm tra giới hạn số lần thử → nhập mật khẩu mới → cập nhật mật khẩu |
| Ngoại lệ | Sai quá số lần cho phép, thông tin không khớp, mật khẩu mới không đạt yêu cầu |
| Hậu điều kiện | Chỉ đặt mật khẩu mới, không đăng nhập trực tiếp |

### UC04 — Khai báo và cập nhật hồ sơ thể chất

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Đã đăng nhập |
| Luồng chính | Nhập tuổi, giới tính, chiều cao, cân nặng, mục tiêu, kinh nghiệm, số buổi, thời lượng, thiết bị, lịch rảnh, chấn thương và sức khỏe → kiểm tra → tính chỉ số thể lực → lưu |
| Ngoại lệ | Tuổi/cân nặng/chiều cao ngoài giới hạn hoặc dữ liệu không hợp lệ |
| Hậu điều kiện | Hồ sơ trở thành đầu vào cho đề xuất mục tiêu và tạo giáo án |

### UC05 — Làm bài kiểm tra sức bền

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Có hồ sơ cơ bản |
| Luồng chính | Nhập kết quả bài kiểm tra → chuẩn hóa theo hồ sơ → tính mức thể lực → lưu lịch sử |
| Ngoại lệ | Thiếu dữ liệu hoặc kết quả vượt miền hợp lệ |
| Hậu điều kiện | Mức thể lực được dùng để chọn độ khó, số hiệp, số lần và thời gian nghỉ |

### UC06 — Đề xuất mục tiêu an toàn

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Hồ sơ đã nhập đầy đủ |
| Luồng chính | Hệ thống đánh giá BMI, thể lực, tuổi và hạn chế → nếu hồ sơ quá yếu thì đề xuất giáo án cải thiện nền tảng thay vì mục tiêu cường độ cao |
| Ngoại lệ | Không đủ dữ liệu để đánh giá |
| Hậu điều kiện | Người dùng được giải thích lý do và có thể chọn mẫu phù hợp |

### UC07 — Tạo giáo án cá nhân hóa

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Hồ sơ hợp lệ; đã chọn mục tiêu và số buổi |
| Luồng chính | Xác định nhóm cơ theo mục tiêu → chia nhóm cơ theo số buổi → lọc bài chống chỉ định → tính điểm phù hợp → xếp hạng → cân bằng nhóm cơ → sinh hiệp/lần/tạ/nghỉ → lưu giáo án |
| Ngoại lệ | Không đủ bài phù hợp, hồ sơ thiếu, đang có giáo án không thể thay thế |
| Hậu điều kiện | Một giáo án có ngày tập, bài tập, lịch khuyến nghị, ghi chú và thời gian dự kiến |

### UC08 — Chọn giáo án mẫu

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Mẫu đang hoạt động và phù hợp điều kiện người dùng |
| Luồng chính | Xem danh sách mẫu → xem chi tiết → chọn mẫu → hệ thống sao chép thành giáo án cá nhân |
| Ngoại lệ | Mẫu bị ẩn hoặc không phù hợp thể lực/chấn thương |
| Hậu điều kiện | Người dùng có giáo án riêng, không sửa trực tiếp bản mẫu của admin |

### UC09 — Quản lý và thực hiện buổi tập

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Có giáo án đang hoạt động |
| Luồng chính | Chọn tháng/tuần → chọn buổi → bắt đầu → ghi từng bài → checkout → lưu thời gian, mức hoàn thành và calo |
| Ngoại lệ | Bắt đầu trùng buổi, sai thứ tự, phiên đã hoàn thành/bỏ qua, lỗi dữ liệu phiên |
| Hậu điều kiện | Buổi có trạng thái rõ ràng; khóa chống bấm bắt đầu hai lần và ID do CSDL tự sinh |

### UC10 — Thoát buổi chưa checkout

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Buổi đã bắt đầu nhưng chưa checkout |
| Luồng chính | Người dùng thoát → hệ thống giữ trạng thái có thể tiếp tục hoặc hủy phiên chưa hoàn tất theo quy tắc |
| Ngoại lệ | Phiên đã hoàn tất hoặc không thuộc người dùng |
| Hậu điều kiện | Không khóa chức năng checkout của các buổi hợp lệ khác |

### UC11 — Bỏ qua buổi tập

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Buổi chưa hoàn thành và chưa bỏ qua |
| Luồng chính | Bấm Bỏ qua → xác nhận → đổi trạng thái `SKIPPED` → cập nhật tiến độ tuần |
| Ngoại lệ | Buổi đã checkout hoặc không thuộc tuần hiện tại |
| Hậu điều kiện | Buổi bỏ qua được tính vào số buổi đã xử lý nhưng không tính là hoàn thành |

### UC12 — Hoàn tất tuần và chuyển tuần

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Tất cả buổi yêu cầu đã hoàn thành hoặc bỏ qua |
| Luồng chính | Hệ thống hiển thị hộp thoại → yêu cầu nhập cân nặng/đánh giá tuần → người dùng xác nhận → lưu review → chuyển sang tuần kế tiếp |
| Ngoại lệ | Người dùng đóng hộp thoại, dữ liệu không hợp lệ hoặc review đã tồn tại |
| Hậu điều kiện | Không tự động nhảy tuần khi chưa có ý kiến người dùng |

### UC13 — Thêm bài tập phụ

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Có buổi tập; bài tập đang hoạt động |
| Luồng chính | Mở danh sách bài → lọc/tìm kiếm → xem kỹ thuật và video → chọn bài → thêm vào buổi phụ |
| Ngoại lệ | Bài trùng, bài bị ẩn hoặc chống chỉ định theo chấn thương |
| Hậu điều kiện | Bài phụ liên kết với dữ liệu bài tập gốc và hiển thị đầy đủ hướng dẫn |

### UC14 — Ghi nhận và xem tiến độ

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Đã đăng nhập |
| Luồng chính | Nhập cân nặng và ghi chú → kiểm tra một lần/ngày → lưu → chọn tháng để xem bảng và biểu đồ thay đổi |
| Ngoại lệ | Đã ghi trong ngày, cân nặng ngoài giới hạn |
| Hậu điều kiện | Biểu đồ phản ánh đúng chênh lệch từng bản ghi; không yêu cầu % mỡ hoặc số đo cơ thể khó tự đo |

### UC15 — Cảnh báo mức hoàn thành thấp

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng thường |
| Tiền điều kiện | Tỷ lệ hoàn thành dưới 40% trong hai tuần liên tiếp |
| Luồng chính | Hệ thống cảnh báo → đề nghị cập nhật hồ sơ → dùng thông tin mới và lịch sử hai tuần để điều chỉnh/tạo giáo án |
| Ngoại lệ | Người dùng chọn bỏ qua cảnh báo |
| Hậu điều kiện | Cảnh báo không chặn người dùng tiếp tục tập |

### UC16 — Xem thống kê

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Có dữ liệu tập luyện hoặc tiến độ |
| Luồng chính | Chọn tháng → hệ thống tổng hợp buổi hoàn thành, khối lượng, calo, thời gian, cân nặng và phân bố nhóm cơ |
| Ngoại lệ | Tháng không có dữ liệu thì hiển thị trạng thái trống rõ ràng |
| Hậu điều kiện | Dữ liệu chỉ thuộc tài khoản hiện tại và thống nhất giữa thẻ số liệu, bảng và biểu đồ |

### UC17 — Xem bài tập và món ăn

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Dữ liệu đang hoạt động |
| Luồng chính | Tìm kiếm/lọc/phân trang → mở chi tiết → xem hướng dẫn bài tập hoặc nguyên liệu, định lượng và cách chế biến món ăn |
| Ngoại lệ | Bản ghi bị ẩn hoặc không tồn tại |
| Hậu điều kiện | Công thức và hướng dẫn không mất khi tải lại hoặc chuyển trang |

### UC18 — Nâng cấp VIP và thanh toán

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng, SePay |
| Tiền điều kiện | Người dùng chưa có gói tương ứng đang hiệu lực |
| Luồng chính | Chọn gói 99.000đ → tạo giao dịch → hiển thị VietQR → chuyển khoản đúng nội dung → SePay gửi webhook → đối chiếu số tiền/nội dung → kích hoạt gói |
| Ngoại lệ | Sai số tiền, sai nội dung, giao dịch trùng, hết hạn hoặc webhook không hợp lệ |
| Hậu điều kiện | Lịch sử giao dịch hiển thị đúng giá; không cần admin xác nhận thủ công |

### UC19 — Mua sản phẩm

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Sản phẩm đang bán và còn đủ tồn kho |
| Luồng chính | Lọc sản phẩm → thêm giỏ → sửa số lượng → đặt hàng → theo dõi trạng thái đơn |
| Ngoại lệ | Hết hàng, số lượng không hợp lệ, sản phẩm ngừng bán |
| Hậu điều kiện | Tạo đơn và chi tiết đơn; giỏ được cập nhật phù hợp |

### UC20 — Trò chuyện hỗ trợ và đánh giá phiên chat

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng, admin |
| Tiền điều kiện | Đã đăng nhập |
| Luồng chính | User gửi yêu cầu → admin nhận/trao đổi → một bên kết thúc → user chấm điểm và nhận xét → admin xem đánh giá |
| Ngoại lệ | Phiên đã đóng, tệp không hợp lệ, người dùng không sở hữu phiên |
| Hậu điều kiện | Tin nhắn, tệp và đánh giá gắn đúng phiên; modal hiển thị theo viewport |

### UC21 — Quản lý thú cưng và trang phục

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Có hồ sơ thú cưng |
| Luồng chính | Xem cấp/điểm → xem trang phục đã sở hữu → thanh toán nếu cần → trang bị vật phẩm |
| Ngoại lệ | Chưa sở hữu, thiếu điều kiện hoặc thanh toán chưa thành công |
| Hậu điều kiện | Chỉ vật phẩm đã sở hữu mới được trang bị; quyền sở hữu không phụ thuộc cache trình duyệt |

### UC22 — Viết đánh giá dịch vụ

| Thuộc tính | Đặc tả |
|---|---|
| Tác nhân | Người dùng |
| Tiền điều kiện | Đã đăng nhập và đủ điều kiện đánh giá |
| Luồng chính | Viết/sửa đánh giá về giáo án và trải nghiệm web → gửi → hiển thị trong danh sách |
| Ngoại lệ | Điểm hoặc nội dung không hợp lệ |
| Hậu điều kiện | User được sửa/xóa đánh giá của chính mình; admin chỉ xem và phản hồi |

## 5. Đặc tả chức năng phía quản trị viên

### UC23 — Quản lý người dùng

- Tìm kiếm, lọc và phân trang người dùng.
- Xem hồ sơ, gói thành viên và lịch sử liên quan.
- Khóa/mở tài khoản và đặt lại mật khẩu theo quyền.
- Không để lộ mật khẩu hoặc dữ liệu nhạy cảm.

### UC24 — Quản lý bài tập

- Thêm, sửa, lọc và phân trang bài tập.
- Quản lý nhóm cơ, mục tiêu, độ khó, thiết bị, chống chỉ định, kỹ thuật và liên kết hướng dẫn.
- Khi xóa, ưu tiên ẩn; có chức năng khôi phục để không làm mất giáo án/lịch sử.

### UC25 — Quản lý món ăn

- Thêm, sửa, lọc và phân trang món ăn.
- Quản lý calo, protein, carbohydrate, chất béo, khẩu phần, nguyên liệu và bước chế biến.
- ID do cơ sở dữ liệu tự sinh; không tự đánh lại ID sau khi xóa.

### UC26 — Quản lý giáo án mẫu và cấu hình tạo giáo án

- Tạo/sửa/ẩn giáo án mẫu, ngày tập, bài tập và ghi chú.
- Thiết lập lịch khuyến nghị theo số buổi mỗi tuần.
- Thiết lập cách chia nhóm cơ theo mục tiêu và số buổi.
- Xem giáo án người dùng và xóa theo điều kiện nghiệp vụ.

### UC27 — Quản lý thành viên và giao dịch

- Xem lịch sử giao dịch, trạng thái, số tiền và người thanh toán.
- Xử lý hoàn tiền theo quyền và lưu dấu vết.
- Không duy trì màn hình hóa đơn trùng lặp nếu lịch sử giao dịch đã bao phủ nghiệp vụ.

### UC28 — Quản lý sản phẩm và đơn hàng

- CRUD mềm sản phẩm, danh mục, giá, tồn kho và trạng thái bán.
- Xem danh sách đơn, chi tiết đơn và cập nhật trạng thái hợp lệ.
- Không cho cập nhật trạng thái ngược quy trình hoặc bán quá tồn kho.

### UC29 — Quản lý hỗ trợ

- Xem hàng đợi phiên chat; tiếp nhận, từ chối, trả lời, gửi tệp và đóng phiên.
- Xem đánh giá phiên chat sau khi kết thúc.
- Tin nhắn và số chưa đọc cập nhật đúng tài khoản admin.

### UC30 — Quản lý đánh giá

- Xem, lọc và phản hồi đánh giá.
- Admin không có quyền xóa nội dung đánh giá của user.
- Phản hồi phải gắn đúng đánh giá và hiển thị công khai theo trạng thái.

### UC31 — Gửi thông báo

- Gửi thông báo cho một người dùng hoặc toàn hệ thống.
- Có bước xem lại/xác nhận trước khi gửi.
- Chỉ gửi các thông báo liên quan đúng ngữ cảnh nghiệp vụ.

### UC32 — Xem dashboard quản trị

- Tổng hợp người dùng, gói thành viên, doanh thu, hoạt động tập luyện, đánh giá và đơn hàng.
- Cho phép lọc theo thời gian và đảm bảo số liệu khớp dữ liệu nguồn.

### UC33 — Quản lý cấu hình hệ thống

- Xem và cập nhật các tham số cho giá gói, giới hạn, lịch tập, cảnh báo và tính năng.
- Kiểm tra kiểu dữ liệu, miền giá trị và quyền admin trước khi lưu.
- Thay đổi quan trọng phải có xác nhận và thông báo kết quả.

## 6. Yêu cầu phi chức năng

| Nhóm | Yêu cầu |
|---|---|
| Bảo mật | Mật khẩu mã hóa; JWT hết hạn; phân quyền API; xác thực webhook; không ghi bí mật vào giao diện/log |
| Hiệu năng | API danh sách có lọc/phân trang; dashboard tổng hợp có truy vấn hợp lý; tránh gọi API lặp |
| Toàn vẹn | Khóa chính tự sinh; ràng buộc duy nhất hợp lý; transaction cho checkout/thanh toán/đặt hàng |
| Dễ dùng | Toàn bộ nhãn tiếng Việt; modal theo viewport; thông báo rõ nguyên nhân và cách xử lý |
| Khả dụng | Trạng thái tải, rỗng và lỗi; frontend không giữ dữ liệu tài khoản trước; lỗi backend không làm hỏng toàn trang |
| Bảo trì | Tách Controller–Service–Repository–DTO; cấu hình ngoài mã nguồn; có kiểm thử cho nghiệp vụ lõi |
| Tương thích | Giao diện đáp ứng desktop/mobile; API JSON thống nhất; H2 cho demo và MySQL cho triển khai |

## 7. Ma trận chức năng theo vai trò

| Nhóm chức năng | Khách | User | VIP | Admin |
|---|:---:|:---:|:---:|:---:|
| Đăng ký/đăng nhập | ✓ | ✓ | ✓ | ✓ |
| Hồ sơ và thể lực |  | ✓ | ✓ | Xem |
| Giáo án và buổi tập |  | ✓ | ✓ | Quản lý |
| Tiến độ/thống kê |  | ✓ | ✓ | Tổng hợp |
| Món ăn/bài tập | Xem công khai nếu cho phép | ✓ | ✓ | CRUD |
| Nâng cấp gói |  | ✓ |  | Quản lý |
| Mua hàng |  | ✓ | ✓ | Quản lý |
| Hỗ trợ |  | ✓ | ✓ | Tiếp nhận |
| Đánh giá | Xem | Viết/sửa | Viết/sửa | Phản hồi |
| Cấu hình hệ thống |  |  |  | ✓ |

## 8. Công nghệ và vị trí mã nguồn liên quan

| Lớp | Vị trí |
|---|---|
| Giao diện | `gym-frontend/src/views`, `gym-frontend/src/components` |
| Điều hướng/trạng thái/API | `gym-frontend/src/router`, `stores`, `api` |
| REST Controller | `gym-management/src/main/java/com/example/gymmanagement/controller` |
| Nghiệp vụ | `gym-management/src/main/java/com/example/gymmanagement/service` |
| Dữ liệu | `entity`, `repository`, `shop`, `pet` |
| Bảo mật | `config/SecurityConfig`, `JwtAuthenticationFilter`, `security/JwtService` |
| Cấu hình và SQL demo | `gym-management/src/main/resources` |

