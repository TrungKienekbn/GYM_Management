# CÂU HỎI GIẢNG VIÊN CÓ THỂ HỎI VÀ CÁCH TRẢ LỜI

## A. Câu hỏi về bài toán và nghiệp vụ

### 1. Đề tài giải quyết vấn đề gì?

Trả lời:

> GymPro tập trung dữ liệu hồ sơ, giáo án, buổi tập, tiến độ, dinh dưỡng và thanh toán vào một hệ thống. Giá trị chính là dữ liệu được liên kết: kết quả checkout cập nhật lịch sử, tiến độ và có thể ảnh hưởng đến giáo án tuần sau.

### 2. Đối tượng sử dụng hệ thống là ai?

> Có hai vai trò chính: user sử dụng chức năng luyện tập và admin quản lý dữ liệu, người dùng, hóa đơn, thông báo, đánh giá và hỗ trợ.

### 3. Điểm khác biệt giữa gói thường và VIP là gì?

> Gói thường dùng các chức năng luyện tập cơ bản nhưng bị giới hạn một số dữ liệu lịch sử và không tự điều chỉnh giáo án nâng cao. VIP được xem lịch sử đầy đủ và tự động điều chỉnh giáo án theo kết quả tuần. Quyền VIP được kiểm tra tại backend dựa trên loại gói, trạng thái PAID, trạng thái active và hạn sử dụng.

### 4. Vì sao không khóa toàn bộ ứng dụng với người dùng thường?

> Mục tiêu là mô hình freemium: người dùng thường vẫn trải nghiệm được giá trị cốt lõi. VIP mở rộng khả năng phân tích và cá nhân hóa, tạo lý do nâng cấp mà không làm ứng dụng mất khả năng sử dụng.

### 5. Giáo án được tạo dựa trên những dữ liệu nào?

> Các đầu vào chính gồm mục tiêu, trình độ, chiều cao/cân nặng, BMI, số ngày có thể tập, thời lượng buổi và dữ liệu thể lực. Hệ thống dùng các quy tắc và trọng số để chọn bài, sets, reps, lịch và mức độ phù hợp.

### 6. Đây có thực sự là AI không?

> Phiên bản hiện tại chủ yếu là hệ thống đề xuất theo luật, công thức và trọng số, chưa gọi mô hình học máy bên ngoài. Trong báo cáo nên gọi chính xác là “tạo giáo án thông minh/rule-based”. Hướng phát triển là huấn luyện hoặc tích hợp mô hình khi có đủ dữ liệu thực tế.

### 7. Vì sao phải hỏi trước khi chuyển tuần?

> Hoàn thành buổi cuối không đồng nghĩa người dùng luôn muốn chuyển ngay. Bước xác nhận giúp người dùng xem lại kết quả, tránh thay đổi trạng thái ngoài ý muốn và làm luồng nghiệp vụ rõ ràng hơn.

### 8. Làm sao tránh bắt đầu hai buổi tập cùng lúc?

> Backend kiểm tra buổi SCHEDULED/đang mở của cùng user và giáo án trước khi tạo buổi mới. Kiểm tra phải nằm ở backend vì chỉ chặn nút frontend thì vẫn có thể gọi API trực tiếp hoặc bấm nhanh nhiều lần.

### 9. Vì sao bài tập bị xóa lại chuyển thành “ẩn”?

> Bài tập có thể đã được tham chiếu bởi giáo án và lịch sử checkout. Xóa cứng dễ làm mất liên kết hoặc vi phạm khóa ngoại. Soft delete bằng `is_active` bảo toàn lịch sử, đồng thời admin vẫn có thể khôi phục.

### 10. ID bị thiếu sau khi xóa có phải lỗi không?

> Không. ID là khóa định danh, không phải số thứ tự. Cơ chế identity không đảm bảo liên tục vì giao dịch có thể rollback hoặc bản ghi có thể bị xóa. Giao diện cần tự tính STT theo trang, không sửa ID của dữ liệu cũ.

### 11. Quy đổi dinh dưỡng hoạt động thế nào?

> Dữ liệu gốc được lưu theo 100 g. Giá trị hiển thị bằng `giá trị trên 100 g × khối lượng / 100`. Ví dụ 200 kcal/100 g thì khẩu phần 150 g là 300 kcal. Calories và các macro đều dùng cùng một tỷ lệ.

### 12. Buổi tập phụ liên kết với bài tập thế nào?

> User chọn bài từ thư viện; hệ thống lưu liên kết tới ID bài tập trong buổi phụ thay vì sao chép toàn bộ thông tin. Nhờ vậy có thể dùng lại hướng dẫn bài tập và bảo đảm dữ liệu nhất quán.

## B. Câu hỏi về cơ sở dữ liệu

### 13. Tại sao dùng H2?

> H2 phù hợp giai đoạn phát triển và demo vì gọn, chạy local và có console kiểm tra dữ liệu. Nếu triển khai thực tế, nhóm sẽ chuyển sang PostgreSQL hoặc MySQL, thêm migration và cấu hình backup.

### 14. Các quan hệ dữ liệu quan trọng là gì?

> User có profile, membership, progress và nhiều workout plan/session. Workout plan có nhiều ngày; mỗi ngày có nhiều bài. Workout session liên kết user, plan, plan day và các exercise log. Invoice liên kết user và có thể liên kết membership.

### 15. Vì sao tách WorkoutPlan, WorkoutPlanDay và WorkoutPlanExercise?

> Đây là cấu trúc phân cấp tự nhiên và tránh lặp dữ liệu. Một giáo án có nhiều ngày, một ngày có nhiều bài; mỗi liên kết bài trong giáo án có sets, reps, nghỉ và mức tạ riêng.

### 16. File Full Test có tác dụng gì?

> File tạo bộ dữ liệu demo lặp lại được: user VIP, hồ sơ, tiến độ, hóa đơn, thú cưng, bài tập và hai giáo án đã hoàn thành với 16 buổi. Nhờ đó demo không phụ thuộc vào việc nhập dữ liệu trực tiếp trước lớp.

### 17. File SQL chạy lại có bị trùng không?

> Các khối insert đều có điều kiện `NOT EXISTS`, nên cùng bản ghi test không được tạo thêm. Tuy nhiên đây là script demo; môi trường production nên dùng migration có phiên bản như Flyway hoặc Liquibase.

### 18. Tại sao không đánh lại ID sau khi xóa?

> Thay đổi ID có thể phá khóa ngoại và lịch sử. Hệ thống chỉ bảo đảm ID mới không trùng; số thứ tự hiển thị được tính riêng ở frontend.

### 19. Làm sao bảo đảm toàn vẹn dữ liệu?

> Dùng khóa ngoại qua quan hệ JPA, unique constraint cho dữ liệu như email và quyền sở hữu trang phục, validation request và transaction ở service đối với thao tác nhiều bước.

## C. Câu hỏi kỹ thuật backend

### 20. Backend sử dụng công nghệ gì?

> Java 17, Spring Boot, Spring Web, Spring Data JPA, Spring Security, JWT, Bean Validation, Lombok và H2.

### 21. Kiến trúc backend được chia như thế nào?

> Controller nhận HTTP request; service xử lý nghiệp vụ; repository truy cập dữ liệu; entity ánh xạ bảng; DTO tách dữ liệu API khỏi entity. Security xử lý xác thực và phân quyền.

### 22. Vì sao không trả entity trực tiếp?

> DTO giúp kiểm soát trường trả về, tránh lộ mật khẩu hoặc quan hệ nội bộ, tránh vòng lặp JSON và cho phép API thay đổi độc lập hơn với cấu trúc database.

### 23. JWT hoạt động thế nào?

> Sau khi đăng nhập đúng, backend cấp token có thông tin người dùng/vai trò và thời hạn. Frontend gửi token trong header Authorization. Spring Security xác minh token trước khi cho phép truy cập endpoint bảo vệ.

### 24. Mật khẩu được bảo vệ thế nào?

> Mật khẩu được băm bằng BCrypt, không lưu dạng rõ. Khi đăng nhập, hệ thống dùng password encoder để so khớp. HTTPS vẫn cần thiết khi triển khai thật để bảo vệ dữ liệu trên đường truyền.

### 25. Tại sao kiểm tra VIP ở backend?

> Ẩn nút ở frontend chỉ là UX, không phải bảo mật. Người dùng có thể tự gọi API. Backend phải là nguồn quyết định quyền truy cập cuối cùng.

### 26. Xử lý lỗi API như thế nào?

> Lỗi nghiệp vụ được trả qua response chuẩn. Frontend có interceptor hiển thị lỗi phù hợp. Các API kiểm tra trạng thái tùy chọn như “chưa có gói/giáo án/tiến độ” trả null hoặc chạy im lặng vì đó không phải lỗi người dùng.

### 27. Làm sao tránh thanh toán giả?

> Không kích hoạt quyền dựa trên việc user bấm nút “đã thanh toán”. Invoice ở trạng thái PENDING; backend chỉ chuyển PAID khi webhook hợp lệ, đúng mã tham chiếu và đúng số tiền. Sau đó mới kích hoạt membership hoặc cấp trang phục.

### 28. Nếu webhook được gửi hai lần thì sao?

> Xử lý cần có tính idempotent: nếu invoice đã PAID thì không kích hoạt hoặc cấp quyền lần nữa. Mã giao dịch/mã chuyển khoản phải duy nhất và được kiểm tra trước khi cập nhật.

### 29. Transaction dùng ở đâu?

> Các luồng như checkout, kích hoạt thanh toán hoặc tạo cấu trúc giáo án có nhiều thay đổi liên quan. `@Transactional` giúp hoặc toàn bộ thành công, hoặc rollback khi có lỗi, tránh dữ liệu dở dang.

## D. Câu hỏi kỹ thuật frontend

### 30. Frontend sử dụng công nghệ gì?

> Vue 3 với Composition API, Vue Router, Pinia, Axios, Element Plus, Chart.js, Day.js và thư viện tạo QR.

### 31. Pinia dùng để làm gì?

> Pinia lưu trạng thái dùng chung như thông tin đăng nhập và token-related state, giúp các component không phải truyền dữ liệu qua quá nhiều tầng.

### 32. Vue Router bảo vệ trang admin thế nào?

> Route có metadata về yêu cầu đăng nhập/vai trò và navigation guard kiểm tra trạng thái trước khi điều hướng. Tuy vậy backend vẫn tiếp tục kiểm tra quyền vì route guard không thay thế bảo mật server.

### 33. Filter và phân trang xử lý ở đâu?

> Với dữ liệu hiện tại, một số màn hình có thể filter/phân trang trên tập dữ liệu đã tải. Khi dữ liệu lớn, nên chuyển sang phân trang server-side với `page`, `size`, `keyword` để giảm dung lượng và thời gian phản hồi.

### 34. Vì sao dùng Axios interceptor?

> Interceptor tự gắn JWT, chuẩn hóa lấy dữ liệu response và xử lý lỗi xác thực tập trung. Nó cũng hỗ trợ chế độ không hiện popup cho các request thăm dò trạng thái bình thường.

## E. Câu hỏi kiểm thử, giới hạn và phát triển

### 35. Nhóm đã kiểm thử như thế nào?

> Có kiểm tra build frontend/backend, kiểm thử các service quan trọng và chạy luồng tích hợp bằng dữ liệu Full Test. Nếu phát triển tiếp, nhóm sẽ tăng unit test, integration test với database riêng và end-to-end test cho đăng nhập, checkout và thanh toán.

### 36. Điểm yếu hiện tại của hệ thống là gì?

> H2 phù hợp demo nhưng chưa phải lựa chọn production; thuật toán đề xuất còn rule-based; tích hợp thanh toán/email cần cấu hình môi trường thật; cần thêm logging tập trung, migration, rate limiting, backup và bộ test tự động đầy đủ.

### 37. Nếu có nhiều người dùng thì hệ thống chịu tải được không?

> Kiến trúc hiện tại có thể mở rộng bước đầu nhưng chưa đo tải chính thức. Cần chuyển sang database production, thêm index, phân trang server-side, cache dữ liệu đọc nhiều, connection pool, giám sát và load test trước khi kết luận khả năng chịu tải.

### 38. Vì sao chọn Vue + Spring Boot?

> Vue phù hợp xây SPA theo component và có hệ sinh thái UI tốt. Spring Boot có cấu trúc rõ cho REST API, security, validation, transaction và JPA. Hai phần tách biệt giúp frontend/backend phát triển và triển khai độc lập.

### 39. Chức năng nào nhóm tự đánh giá là nổi bật nhất?

> Luồng xuyên suốt giáo án → buổi tập → checkout → tiến độ → điều chỉnh tuần. Nó thể hiện nhiều bảng dữ liệu và quy tắc nghiệp vụ phối hợp, thay vì chỉ CRUD từng bảng độc lập.

### 40. Nếu có thêm thời gian, nhóm sẽ làm gì trước?

> Ưu tiên chuyển database sang PostgreSQL, thêm Flyway migration, hoàn thiện test cho checkout/thanh toán, phân trang server-side, triển khai HTTPS và giám sát. Sau khi có dữ liệu đủ lớn mới đánh giá việc dùng mô hình học máy.

## F. Công thức trả lời khi gặp câu hỏi ngoài dự kiến

Dùng cấu trúc bốn câu:

1. **Xác nhận phạm vi:** “Trong phiên bản hiện tại, nhóm em xử lý ở mức…”
2. **Nêu cách đang làm:** “Hệ thống hiện dùng…”
3. **Thừa nhận giới hạn chính xác:** “Điểm chưa có là…”
4. **Đưa hướng phát triển khả thi:** “Nếu triển khai thực tế, nhóm em sẽ…”

Ví dụ:

> Trong phiên bản hiện tại, nhóm em mới demo thanh toán qua trạng thái và webhook. Backend chỉ cấp quyền khi hóa đơn được xác nhận PAID, không dựa vào nút bấm phía người dùng. Phần chưa hoàn thiện là môi trường ngân hàng production và cơ chế đối soát định kỳ. Nếu triển khai thật, nhóm em sẽ xác thực chữ ký webhook, lưu audit log và chạy job đối soát.

## G. Các câu không nên trả lời

- Không nói “em không biết” rồi dừng; hãy nêu phạm vi hiện tại và hướng tìm hiểu.
- Không gọi thuật toán rule-based là machine learning nếu không có mô hình/dữ liệu huấn luyện.
- Không khẳng định hệ thống “bảo mật tuyệt đối”.
- Không nói frontend chặn được API; quyền phải được kiểm tra ở backend.
- Không nói ID bắt buộc liên tục.
- Không nói H2 phù hợp production nhiều người dùng.
- Không che giấu lỗi; mô tả tác động, nguyên nhân đã biết và hướng khắc phục.
