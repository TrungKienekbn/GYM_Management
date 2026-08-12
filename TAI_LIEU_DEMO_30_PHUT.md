# KỊCH BẢN DEMO GYMPRO TRONG 30 PHÚT

## 1. Mục tiêu buổi demo

Thông điệp chính:

> GymPro không phát ngẫu nhiên một danh sách bài tập. Hệ thống lấy dữ liệu hồ sơ làm ràng buộc, loại bài không phù hợp, chia lịch theo mục tiêu rồi chấm điểm để chọn bài và tải tập. Hai người có dữ liệu khác nhau sẽ nhận hai giáo án khác nhau.

Không gọi đây là machine learning. Cách gọi chính xác là **hệ thống tạo giáo án cá nhân hóa theo luật và điểm số**.

## 2. Hai tài khoản đối chứng

| Thuộc tính | Demo 1 | Demo 2 |
|---|---|---|
| Tài khoản | `fulltest@gym.com` | `fulltest2@gym.com` |
| Mật khẩu | `password` | `password` |
| Gói | VIP | Thường |
| Mục tiêu | Tăng cơ | Giảm cân |
| Giới tính | Nam | Nữ |
| Chiều cao/cân nặng | 175 cm / 72 kg | 160 cm / 68 kg |
| BMI | 23,51 – cân đối | 26,56 – thừa cân |
| Kinh nghiệm | Trung bình, 18 tháng | Mới tập, 2 tháng |
| Nơi tập | Phòng gym | Tại nhà |
| Thiết bị | Đầy đủ máy, cáp, tạ | Thảm, dây, tạ đơn, trọng lượng cơ thể |
| Lịch rảnh | Thứ 2, 3, 5, 7; 4 buổi | Thứ 3, 5, Chủ nhật; 3 buổi |
| Thời lượng | 60 phút | 45 phút |
| Giới hạn | Không | Đau đầu gối, không thích Burpee |
| Dạng giáo án | Chia nhóm cơ, nhiều bài dùng tạ | Circuit thân trên/cơ lõi, nghỉ ngắn, tác động thấp |

## 3. Chuẩn bị trước khi trình bày

1. Khởi động backend ở cổng 8080 và frontend ở cổng 5173.
2. Khởi động backend ít nhất một lần để Hibernate tạo cột/bảng mới.
3. Chạy `h2-test-data.sql` sau khi schema đã được tạo.
4. Mở hai cửa sổ ẩn danh hoặc hai profile trình duyệt:
   - Cửa sổ A đăng nhập Demo 1.
   - Cửa sổ B đăng nhập Demo 2.
5. Mở sẵn tab Hồ sơ và Giáo án ở cả hai cửa sổ.
6. Mở sẵn Admin trong cửa sổ thứ ba nếu còn thời gian.
7. Không tạo lại giáo án ngay từ đầu nếu thời gian demo ngắn; dùng giáo án đã seed để so sánh ổn định.

## 3.1. Phân chia cho 4 người thuyết trình

| Người | Thời gian | Phần phụ trách | Tài khoản/cửa sổ |
|---|---:|---|---|
| Người 1 | 00:00–05:00 | Mở bài, bài toán, kiến trúc tổng quan và giải thích lõi thuật toán | Trang chủ + slide/tài liệu |
| Người 2 | 05:00–13:00 | Hồ sơ và giáo án Full Test 1; hồ sơ và giáo án Full Test 2 | Hai cửa sổ user đã đăng nhập |
| Người 3 | 13:00–21:00 | So sánh vì sao bài tập khác nhau; buổi tập, checkout và tiến độ | Hai tài khoản user |
| Người 4 | 21:00–30:00 | Dinh dưỡng, cửa hàng, VIP/SePay, admin, kiến trúc và kết luận | User + Admin |

### Người 1 — Giới thiệu và lõi thuật toán (5 phút)

Nhiệm vụ:

1. Giới thiệu đề tài và vấn đề cần giải quyết.
2. Giới thiệu hai tài khoản đối chứng trên trang chủ.
3. Trình bày chuỗi nghiệp vụ chính:

```text
Hồ sơ → Tạo giáo án → Buổi tập → Checkout
→ Tiến độ → Điều chỉnh → Dinh dưỡng/Cửa hàng
```

4. Giải thích 8 bước tạo giáo án ở phần `02:00–05:00`.
5. Kết thúc bằng câu chuyển cho Người 2.

Lời nói mở đầu gợi ý:

> Nhóm em xây dựng GymPro để quản lý xuyên suốt hành trình luyện tập, không chỉ lưu một lịch tập cố định. Điểm nhóm em muốn chứng minh là cùng một kho bài tập nhưng hai hồ sơ khác nhau sẽ tạo ra lịch, bài và tải tập khác nhau theo các quy tắc có thể giải thích được.

Lời chuyển:

> Sau đây bạn thứ hai sẽ mở trực tiếp hai hồ sơ để chứng minh các đầu vào này tạo ra hai giáo án khác nhau như thế nào.

### Người 2 — Demo hai hồ sơ và hai giáo án (8 phút)

Nhiệm vụ:

1. Mở `fulltest@gym.com`.
2. Trình bày hồ sơ tăng cơ, 18 tháng kinh nghiệm, phòng gym, 4 buổi, 60 phút.
3. Mở giáo án tăng cơ và chỉ các bài dùng tạ/máy.
4. Chuyển sang `fulltest2@gym.com`.
5. Trình bày hồ sơ giảm cân, mới tập, tại nhà, 3 buổi, 45 phút, đau đầu gối.
6. Mở giáo án circuit tại nhà.
7. Chỉ trình bày dữ liệu nhìn thấy; chưa giải thích sâu thuật toán để dành cho Người 3.

Lời nói trọng tâm:

> Tài khoản thứ nhất có điều kiện tập tại phòng gym nên giáo án gồm bốn buổi chia nhóm cơ và có bài dùng tạ, máy, cáp. Tài khoản thứ hai chỉ có ba ngày, tập tại nhà và có vấn đề đầu gối nên kết quả chỉ còn các bài thân trên và cơ lõi tác động thấp.

Lời chuyển:

> Hai kết quả đã khác nhau trên màn hình. Bạn thứ ba sẽ phân tích chính xác mỗi khác biệt đến từ quy tắc nào và dữ liệu checkout tiếp tục được sử dụng ra sao.

### Người 3 — Phân tích khác biệt, buổi tập và tiến độ (8 phút)

Nhiệm vụ:

1. Trình bày bảng so sánh tại phần `13:00–15:30`.
2. Giải thích:
   - Vì sao tăng cơ có Deadlift, Leg Press, Tricep Pushdown.
   - Vì sao giảm cân tại nhà có Push Up, Crunch, Plank, Lateral Raise.
   - Vì sao Demo 2 không có Squat, Lunge, Burpee và bài cần máy.
3. Nêu thứ tự lọc an toàn → thiết bị → sở thích → điểm mục tiêu.
4. Demo bắt đầu buổi tập và form checkout.
5. Mở tiến độ hai tài khoản và chỉ sự khác nhau của biểu đồ.

Lời nói trọng tâm:

> Điểm mục tiêu chỉ được xét sau điều kiện an toàn và thiết bị. Vì vậy một bài dù có điểm giảm cân cao vẫn bị loại nếu xung đột chấn thương đầu gối. Checkout tiếp tục lưu kết quả thực tế để thống kê và điều chỉnh các tuần sau, tạo thành vòng phản hồi chứ không dừng ở bước sinh giáo án.

Lời chuyển:

> Sau phần luyện tập và tiến độ, bạn thứ tư sẽ trình bày các nghiệp vụ bổ trợ và quản trị giúp hệ thống vận hành hoàn chỉnh.

### Người 4 — Dinh dưỡng, bán hàng, thanh toán, admin và kết luận (9 phút)

Nhiệm vụ:

1. Demo quy đổi món ăn theo khẩu phần.
2. Demo sản phẩm cửa hàng được gợi ý theo mục tiêu.
3. Thêm sản phẩm vào giỏ và mở bước đặt hàng.
4. Giải thích VIP 99.000đ và thanh toán SePay.
5. Chuyển sang admin:
   - User.
   - Bài tập và điểm mục tiêu.
   - Cửa hàng/tồn kho/trạng thái đơn.
   - Giáo án mẫu và thông báo.
6. Kết luận kiến trúc và giới hạn hiện tại.

Lời kết gợi ý:

> GymPro kết nối dữ liệu hồ sơ, giáo án, kết quả tập, tiến độ, dinh dưỡng, bán hàng và thanh toán trong cùng một hệ thống. Phiên bản hiện tại dùng thuật toán rule-based để mọi quyết định đều có thể truy ngược và giải thích. Nếu phát triển thực tế, nhóm sẽ chuyển từ H2 sang PostgreSQL, dùng migration, mở rộng kiểm thử và đối soát thanh toán.

### Quy tắc phối hợp giữa 4 người

- Mỗi người đứng sẵn ở đúng cửa sổ hoặc tab của mình trước khi bắt đầu.
- Không đăng nhập lại trong lúc chuyển người nếu đã chuẩn bị hai cửa sổ user.
- Người đang nói tự điều khiển chuột; không để một người nói, một người bấm trừ khi đã tập trước.
- Người sau bắt đầu ngay từ câu chuyển của người trước, không giới thiệu lại đề tài.
- Người 2 chỉ mô tả đầu vào và kết quả; Người 3 chịu trách nhiệm giải thích “tại sao”.
- Người 4 theo dõi thời gian. Nếu đến phút 27 mới vào admin, chỉ demo cửa hàng và bài tập rồi kết luận.
- Khi giảng viên hỏi giữa chừng, người phụ trách đúng chủ đề trả lời; người khác không chen thêm nếu chưa được mời.

### Phân công trả lời câu hỏi

| Nhóm câu hỏi | Người trả lời chính |
|---|---|
| Bài toán, kiến trúc tổng quan, công nghệ | Người 1 |
| Hồ sơ, dữ liệu hai tài khoản, lịch tập | Người 2 |
| Thuật toán, điểm bài tập, checkout, tiến độ | Người 3 |
| Dinh dưỡng, cửa hàng, VIP, SePay, admin | Người 4 |
| Database/JPA/transaction | Người hiểu backend nhất trả lời, Người 1 bổ sung |
| Vue/Pinia/router/UI | Người hiểu frontend nhất trả lời, Người 4 thao tác minh họa |

## 4. Timeline 30 phút

### 00:00–02:00 — Mở bài

Thao tác:

1. Mở trang chủ.
2. Chỉ vào hai tài khoản demo hiển thị trên trang.
3. Nêu bài toán.

Lời nói:

> Nhiều ứng dụng chỉ lưu lịch tập. GymPro giải quyết chuỗi nghiệp vụ từ hồ sơ, tạo giáo án, bắt đầu và checkout buổi tập, theo dõi tiến độ, điều chỉnh theo tuần, cho đến thanh toán và quản trị. Phần em tập trung chứng minh hôm nay là: dữ liệu hồ sơ khác nhau thực sự làm giáo án khác nhau.

### 02:00–05:00 — Giải thích lõi tạo giáo án trước khi bấm demo

Nói theo đúng thứ tự xử lý:

1. Nhận đầu vào: mục tiêu, trình độ, BMI/thể lực, lịch rảnh, thời lượng, thiết bị, chấn thương và bài không thích.
2. Xác định số buổi và cách chia nhóm cơ.
3. Lọc cứng bài không an toàn hoặc không đủ thiết bị.
4. Trong tập còn lại, ưu tiên độ khó theo trình độ.
5. Chấm điểm bài theo mục tiêu.
6. Nếu bằng điểm, dùng ID tăng dần để kết quả ổn định, không random.
7. Tính sets, reps, nghỉ và tạ theo mục tiêu/thể lực.
8. Giới hạn số bài theo thời lượng buổi.

Lời nói trọng tâm:

> “Cá nhân hóa” ở đây không phải đổi tên giáo án. Mỗi trường hồ sơ tham gia vào một quyết định cụ thể: lịch rảnh quyết định ngày tập, thiết bị và chấn thương loại bài, mục tiêu quyết định điểm ưu tiên và sets/reps, trình độ và thể lực quyết định độ khó và tải.

### 05:00–09:00 — Demo hồ sơ 1: tăng cơ tại phòng gym

Thao tác:

1. Đăng nhập `fulltest@gym.com`.
2. Mở Hồ sơ.
3. Chỉ các trường: tăng cơ, 18 tháng, phòng gym, đủ thiết bị, 4 ngày, 60 phút.
4. Mở Giáo án.
5. Chỉ 4 buổi và các bài như Bench Press, Deadlift, Lat Pulldown, Leg Press, Tricep Pushdown.

Lời nói:

> Hồ sơ này có kinh nghiệm trung bình và tập tại phòng gym nên bài dùng tạ, máy và cáp đều hợp lệ. Mục tiêu tăng cơ ưu tiên điểm muscleGainScore, số hiệp và tải cao hơn, thời gian nghỉ dài hơn. Bốn ngày cho phép chia nhóm cơ rõ để có khối lượng tập cao nhưng vẫn phục hồi.

### 09:00–13:00 — Demo hồ sơ 2: giảm cân tại nhà

Thao tác:

1. Chuyển cửa sổ, đăng nhập `fulltest2@gym.com`.
2. Mở Hồ sơ.
3. Chỉ: giảm cân, mới tập, ít vận động, tại nhà, 3 ngày, 45 phút, đau đầu gối, không thích Burpee.
4. Mở Giáo án.
5. Chỉ lịch Thứ Ba–Thứ Năm–Chủ Nhật.
6. Chỉ các bài Push Up, Crunch, Plank, Lateral Raise; số hiệp thấp hơn và nghỉ ngắn.

Lời nói:

> Đây không phải giáo án 1 đổi tên. Vì tập tại nhà nên bài cần máy cáp, máy chạy hoặc máy chân không được chọn. Vì có vấn đề đầu gối nên Squat, Lunge, Leg Press và bài bật nhảy bị loại. Burpee bị loại theo sở thích. Giáo án còn lại dùng bài thân trên và cơ lõi tác động thấp, theo circuit nghỉ ngắn để phù hợp mục tiêu giảm cân.

### 13:00–15:30 — Đặt hai kết quả cạnh nhau

Trình bày bảng đối chiếu:

| Quyết định | Demo 1 | Demo 2 | Nguồn dữ liệu |
|---|---|---|---|
| Số buổi | 4 | 3 | Số ngày rảnh |
| Thời lượng | 60 phút | 45 phút | Hồ sơ |
| Bài máy/cáp | Có | Không | Nơi tập + thiết bị |
| Squat/Lunge | Có thể có | Bị loại | Chấn thương đầu gối |
| Dạng tải | Tăng cơ, tải cao | Circuit, nghỉ ngắn | Mục tiêu |
| Độ khó | Trung bình | Dễ | Kinh nghiệm/thể lực |
| Điều chỉnh tuần | Tự động VIP | Cảnh báo và tạo lại thủ công | Gói thành viên |

Kết luận phần này:

> Cùng một bảng bài tập, bộ lọc và hàm điểm tạo ra hai tập ứng viên khác nhau. Vì vậy đầu ra khác nhau có thể truy ngược về dữ liệu đầu vào và quy tắc, không phụ thuộc random.

#### Đoạn trình bày cụ thể: hai mục tiêu làm bài tập khác nhau thế nào và tại sao?

Bạn có thể nói nguyên đoạn sau:

> Với tài khoản thứ nhất, mục tiêu là tăng cơ nên thuật toán ưu tiên những bài có `muscleGainScore` cao, đặc biệt là các bài kháng lực và bài đa khớp. Vì người này đã có 18 tháng kinh nghiệm, tập tại phòng gym và có đầy đủ máy, cáp, thanh đòn nên tập ứng viên có Dumbbell Bench Press, Deadlift, Squat, Leg Press và Tricep Pushdown. Các bài này cho phép tăng dần mức tạ, tạo khối lượng tập lớn và tác động rõ vào từng nhóm cơ. Giáo án được chia 4 buổi để mỗi nhóm cơ có đủ khối lượng tập nhưng vẫn có thời gian phục hồi.
>
> Tài khoản thứ hai có mục tiêu giảm cân nên thuật toán ưu tiên `weightLossScore`, mật độ vận động và thời gian nghỉ ngắn hơn. Tuy nhiên điểm mục tiêu không phải điều kiện duy nhất. Người này mới tập, chỉ có 45 phút, tập tại nhà và đau đầu gối nên hệ thống loại trước các bài cần máy và các bài gây tải lớn lên đầu gối như Squat, Lunge, Leg Press, bài bật nhảy; Burpee cũng bị loại vì user đã đánh dấu không thích. Sau bước lọc, hệ thống chọn Push Up, Crunch, Plank và Lateral Raise, sắp theo dạng circuit thân trên–cơ lõi để duy trì vận động mà không bắt người dùng thực hiện bài không an toàn hoặc không đủ thiết bị.
>
> Vì vậy sự khác nhau không đơn giản là “tăng cơ dùng tạ, giảm cân dùng cardio”. Thuật toán xử lý theo thứ tự: an toàn và thiết bị là điều kiện bắt buộc; sau đó mục tiêu mới quyết định bài nào được ưu tiên, còn kinh nghiệm, thể lực và thời lượng quyết định số hiệp, số lần lặp, thời gian nghỉ và tổng số bài trong buổi.

Các khác biệt cụ thể để chỉ trực tiếp trên màn hình:

| Thành phần | Mục tiêu tăng cơ – Full Test 1 | Mục tiêu giảm cân – Full Test 2 | Lý do nghiệp vụ |
|---|---|---|---|
| Ngực | Dumbbell Bench Press, Push Up | Push Up | Tăng cơ ưu tiên bài có thể tăng tải; giảm cân tại nhà dùng trọng lượng cơ thể |
| Lưng/toàn thân | Deadlift | Không chọn Deadlift | Full Test 2 mới tập, không có thanh đòn và cần giáo án đơn giản hơn |
| Chân | Squat, Leg Press, Lunge | Không có các bài này | Full Test 2 khai báo đau đầu gối nên bộ lọc an toàn loại trước khi chấm điểm |
| Vai | Lateral Raise với khối lượng tăng cơ | Lateral Raise nhẹ | Cùng tên bài nhưng sets/reps/tải có thể khác theo mục tiêu và trình độ |
| Cơ lõi | Plank, Crunch là bài hỗ trợ | Crunch, Plank là thành phần chính của circuit | Giảm cân tại nhà cần bài dễ tổ chức, nghỉ ngắn và không cần máy |
| Tay sau | Tricep Pushdown | Không chọn | Full Test 2 không có máy cáp |
| Hình thức buổi | Chia nhóm cơ, tải cao, nghỉ dài hơn | Circuit thân trên–cơ lõi, nghỉ 45–60 giây | Tăng cơ cần tải và phục hồi; giảm cân ưu tiên mật độ vận động |
| Số buổi | 4 buổi/tuần | 3 buổi/tuần | Lấy từ lịch rảnh thực tế, không ép user tập vượt khả năng |
| Số bài | Nhiều hơn trong 60 phút | Ít hơn trong 45 phút | Thuật toán giới hạn số bài theo thời lượng buổi |

Nếu giảng viên hỏi “cùng một bài Push Up tại sao vẫn xuất hiện ở cả hai giáo án?”, trả lời:

> Một bài có thể phù hợp với nhiều mục tiêu nhưng cách sử dụng khác nhau. Trong giáo án tăng cơ, Push Up là bài hỗ trợ cho ngực và tay sau, có thể tăng độ khó hoặc khối lượng. Trong giáo án giảm cân, bài được đặt trong circuit, số lần cao hơn hoặc nghỉ ngắn hơn để duy trì vận động. Mục tiêu không nhất thiết tạo hai danh sách bài hoàn toàn không giao nhau; nó thay đổi điểm ưu tiên và thông số thực hiện của bài.

Nếu giảng viên hỏi “mục tiêu nào quyết định trước?”, trả lời:

> Hệ thống không chọn mục tiêu trước điều kiện an toàn. Thứ tự là: loại bài không an toàn → loại bài thiếu thiết bị → loại bài user không thích → ưu tiên độ khó theo trình độ → sắp điểm theo mục tiêu → tính sets, reps, nghỉ và mức tạ. Vì thế một bài có điểm giảm cân cao vẫn không được chọn nếu gây xung đột với chấn thương đầu gối.

### 15:30–18:30 — Demo buổi tập và checkout

Thao tác:

1. Ở Demo 1, bấm bắt đầu một buổi chưa tập.
2. Chỉ cơ chế không cho mở hai buổi đồng thời.
3. Mở form checkout: reps, thời lượng, mức tạ thực tế.
4. Không cần gửi nếu muốn giữ dữ liệu demo.
5. Mở lịch sử buổi tập theo tháng.

Lời nói:

> Khi bắt đầu, backend tạo WorkoutSession và kiểm tra user chưa có buổi đang mở. Khi checkout, hệ thống lưu kết quả từng bài chứ không chỉ lưu nút “đã hoàn thành”. Các bản ghi đó là nguồn cho thống kê và điều chỉnh tuần.

### 18:30–21:00 — Tiến độ và điều chỉnh

Thao tác:

1. Mở Tiến độ Demo 1 và Demo 2.
2. So sánh lịch sử cân nặng khác ngày và khác xu hướng.
3. Chỉ Demo 2: 70 → 69,2 → 69,5 → 68 kg.
4. Nêu trường hợp hai tuần dưới 40%.

Lời nói:

> Dữ liệu tiến độ là chuỗi thời gian, không phải một số tĩnh. Demo 2 còn có tuần tăng nhẹ để biểu đồ phản ánh biến động thực tế. Nếu gói thường dưới 40% hai tuần liên tiếp, hệ thống cảnh báo cập nhật hồ sơ và tạo lại giáo án nhẹ hơn nhưng không khóa người dùng.

### 21:00–23:30 — Dinh dưỡng và cửa hàng

Thao tác:

1. Mở Món ăn, đổi khẩu phần và chỉ phép quy đổi theo 100 g.
2. Mở Cửa hàng.
3. Chỉ sản phẩm được gợi ý khác theo mục tiêu.
4. Thêm một sản phẩm vào giỏ và mở checkout.

Lời nói:

> Cửa hàng liên kết hồ sơ: tăng cơ ưu tiên whey/creatine, giảm cân ưu tiên món kiểm soát calo. Khi đặt hàng, hệ thống giữ tồn kho, lưu bản chụp tên và giá vào OrderItem. Hủy hoặc hết hạn thì hoàn tồn kho; thanh toán thành công mới chuyển trạng thái bằng webhook SePay.

### 23:30–26:30 — Thanh toán và VIP

Thao tác:

1. Mở gói VIP 99.000đ.
2. Mở một giao dịch PAID có sẵn.
3. Nêu QR và mã chuyển khoản.

Lời nói:

> SePay là dịch vụ nhận biến động giao dịch ngân hàng và gọi webhook về backend. Frontend không được tự xác nhận. Backend tìm đúng mã chuyển khoản, đối chiếu đúng số tiền, chống xử lý lặp rồi mới chuyển PAID và kích hoạt quyền. Đơn cửa hàng dùng mã SHOP, gói tập dùng mã GYMPRO nên hai nghiệp vụ không lẫn nhau.

### 26:30–28:30 — Admin

Thao tác nhanh:

1. Mở quản lý user.
2. Mở bài tập: điểm mục tiêu, ẩn/khôi phục.
3. Mở cửa hàng: giá, tồn kho, trạng thái đơn.
4. Mở giáo án mẫu và thông báo.

Lời nói:

> Admin quản lý dữ liệu nền. Bài tập/sản phẩm từng được tham chiếu không xóa cứng mà chuyển trạng thái để bảo toàn lịch sử. Trạng thái đơn hàng chỉ đi đúng chiều PAID → PREPARING → SHIPPING → DELIVERED → COMPLETED.

### 28:30–30:00 — Kiến trúc và kết luận

Lời nói:

> Frontend dùng Vue 3, Pinia, Vue Router, Axios và Element Plus. Backend dùng Java 17, Spring Boot, Spring Security, JWT, JPA và transaction; H2 phục vụ demo. Phần lõi của đề tài là chuỗi dữ liệu hồ sơ → giáo án → buổi tập → checkout → tiến độ → điều chỉnh. Hai tài khoản đối chứng chứng minh thuật toán có đầu vào, quy tắc và đầu ra kiểm tra được.

## 5. Nếu giảng viên ngắt lời: “Tôi biết dựa trên hồ sơ, cách tạo cụ thể là gì?”

Trả lời ngay, không mở đầu lại:

> Backend gọi `MuscleGroupSplitPlanner` để phân bổ số bài cho từng nhóm cơ theo mục tiêu và số buổi. Với mỗi nhóm cơ, repository lấy các bài đang active theo độ khó. Service loại bài thiếu thiết bị, xung đột chấn thương và bài user không thích. Danh sách còn lại được sắp theo score của mục tiêu, ví dụ tăng cơ dùng `muscleGainScore`, giảm cân dùng `weightLossScore`; bằng điểm thì ID nhỏ hơn được chọn để kết quả ổn định. Sau đó `FitnessCalculator` tạo sets/reps theo mức thể lực và mục tiêu, service tính nghỉ, mức tạ, cuối cùng cắt số bài theo thời lượng buổi.

## 6. Câu hỏi phản biện trực tiếp về hai tài khoản

### Vì sao Demo 2 không có Squat/Lunge dù mục tiêu giảm cân?

> Vì điều kiện an toàn là bộ lọc cứng chạy trước điểm mục tiêu. Bài có điểm giảm cân cao vẫn bị loại nếu xung đột chấn thương đầu gối.

### Tại sao Demo 1 có máy/cáp còn Demo 2 không có?

> Hồ sơ Demo 1 là phòng gym đầy đủ thiết bị. Demo 2 là tại nhà; thuật toán suy ra thiết bị yêu cầu từ bài và chỉ giữ bài bodyweight hoặc dụng cụ đã khai báo.

### Nếu hai bài bằng điểm thì có random không?

> Không. Hiện tại hệ thống dùng ID tăng dần làm tiêu chí phụ nên cùng dữ liệu sẽ cho kết quả ổn định. Khi có thêm dữ liệu, tiêu chí phụ có thể đổi thành bài lâu chưa tập hoặc nhóm cơ hồi phục tốt hơn.

### Tại sao tài khoản 2 chỉ có 3 buổi dù giảm cân từng khuyến nghị 4 buổi?

> Lịch rảnh là ràng buộc cứng. Hệ thống không được ép user tập nhiều hơn khả năng thực tế; nó tạo phương án 3 buổi phù hợp thay vì sinh một lịch khó tuân thủ.

## 7. Phương án dự phòng

- Không tạo giáo án mới trực tiếp nếu sợ làm mất giáo án seed; mở giáo án có sẵn.
- Nếu webhook không chạy do ngrok, mở giao dịch PAID và giải thích payload/mã/số tiền.
- Nếu backend dừng, dùng hai cửa sổ đã tải sẵn và mở H2 Console chứng minh dữ liệu.
- Nếu thiếu thời gian, bỏ phần dinh dưỡng/cửa hàng và giữ phần so sánh hai giáo án.
- Luôn ưu tiên 13 phút đầu vì đó là phần giảng viên yêu cầu trực tiếp.
