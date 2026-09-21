# Thông số cập nhật báo cáo: đủ 43 bảng

Đối chiếu mã nguồn GYM_Management và DDL Hibernate của kiểm thử H2 tạm. Ngày lập: 17/09/2026. Tài liệu dùng để thay/bổ sung báo cáo PDF SD-41_DATNSP26 (1).pdf; chưa sửa trực tiếp PDF hay database đang dùng.

## 1. Số lượng và quy ước

Có 43 bảng vật lý = 41 lớp @Entity + 2 bảng nối/collection. Đợt nâng cấp cửa hàng tăng từ 39 lên 43, không phải từ 17 lên 43; số 17 là phần tài liệu cũ chưa đầy đủ. Bốn bảng thêm: customer_addresses, customer_product_states, shop_order_events, shop_inventory_movements.

Kiểu dữ liệu trong phụ lục là DDL H2 thực tế của kiểm thử: Java Long thường là BIGINT; Double là FLOAT(53); ngày giờ là DATE/TIME(6)/TIMESTAMP(6). Không tự đổi thành DECIMAL/INT để làm đẹp tài liệu. NULL chỉ khả năng để trống tại database, không thay thế các kiểm tra đầu vào của dịch vụ. Giá trị khởi tạo trong Java không đồng nghĩa SQL DEFAULT.

Có 39 ràng buộc FK vật lý. Các ID liên kết bằng dịch vụ được liệt kê riêng, không vẽ thành FK đã tồn tại. Hai bảng phụ: variant_attribute_values có PK ghép (value_id, variant_id); voucher_scope_products không có PK/UNIQUE trong DDL hiện tại. Không tự thêm ràng buộc trong báo cáo.

## 2. Vị trí cần sửa trong PDF

### Trang 14, kế hoạch
Đổi mốc “17 entity” thành 41 entity JPA, sinh 43 bảng vật lý gồm 2 bảng liên kết/collection.

### Trang 21–22, mục 2.3.1
Bổ sung đầy đủ thực thể hiện có. Hai bảng nối được mô tả rõ ở mô hình logic/vật lý; không coi là hai lớp @Entity riêng.

### Trang 22–29, mục 2.3.2
Thay/bổ sung các quan hệ theo danh sách FK và liên kết logic trong tài liệu này. User–Food không có bảng nối hay FK hiện tại; thao tác tham khảo không phải quan hệ N:N được lưu.

### Trang 27, ChatMessage
ChatMessage là hội thoại USER/BOT theo user_id. Hỗ trợ người dùng–quản trị dùng support_sessions và support_messages.

### Trang 29, EnduranceTest
user_id có UNIQUE và NOT NULL: một người dùng có tối đa một bản ghi kiểm tra hiện tại, không phải bảng lịch sử nhiều lần kiểm tra.

### Trang 30 và 33, mục 2.4/2.6
Vẽ lại ERD. Sơ đồ vật lý chứa đủ 43 bảng; dùng các sơ đồ nhóm để đọc được các cột. Chỉ các liên kết FK thật nằm trong ERD vật lý.

### Trang 31–32, mục 2.5
Bổ sung nhân viên, cửa hàng, ca làm, nhân vật, cấu hình, hỗ trợ. Mô tả vai trò theo ADMIN/STAFF/USER; không tự thêm vai trò huấn luyện viên nếu chưa triển khai.

### Trang 34–35, mục 3.1
Thay danh sách bảng và hình cơ sở dữ liệu bằng đủ 43 bảng, sử dụng tên snake_case đúng database.

### Trang 111–122, Phụ lục B
Thay toàn bộ đặc tả cũ bằng 43 đặc tả trong phần 4. Ví dụ roles dùng role_name, không có name/description; PK kiểu BIGINT thay cho INT ở các bảng đang dùng Long.

### Các phần yêu cầu, Use Case, UI, triển khai, kiểm thử, hướng dẫn
Cập nhật các chức năng cửa hàng đã làm, chọn phân loại ở cả /shop và /app/shop, quản lý ca nhân viên, địa chỉ, đơn COD, báo cáo. Không ghi ví điện tử đã kiểm thử giao dịch thực tế.

## 3. Danh mục toàn bộ bảng

### Tài khoản và hội viên

| Bảng | Chức năng | Số cột |
|---|---|---|
| roles | Vai trò phân quyền | 2 |
| users | Tài khoản người dùng | 12 |
| user_profiles | Hồ sơ thể lực và điều kiện tập luyện | 22 |
| memberships | Gói hội viên đã đăng ký | 13 |
| invoices | Hóa đơn gói hội viên và vật phẩm trang trí | 22 |

### Tập luyện và dinh dưỡng

| Bảng | Chức năng | Số cột |
|---|---|---|
| exercises | Thư viện bài tập | 24 |
| foods | Thư viện món ăn | 11 |
| endurance_tests | Kết quả kiểm tra sức bền hiện tại của từng người dùng | 6 |
| progress_tracking | Lịch sử chỉ số cơ thể | 16 |
| workout_plans | Giáo án cá nhân và giáo án mẫu | 39 |
| workout_plan_days | Ngày tập trong giáo án | 4 |
| workout_plan_exercises | Bài tập và định mức trong ngày tập | 16 |
| plan_muscle_group_weight | Hệ số ưu tiên nhóm cơ theo giáo án | 4 |
| workout_sessions | Buổi tập thực tế | 19 |
| session_exercise_logs | Kết quả từng bài tập trong buổi | 11 |
| weekly_reviews | Đánh giá giáo án theo tuần | 7 |

### Cấu hình

| Bảng | Chức năng | Số cột |
|---|---|---|
| muscle_split_configs | Cấu hình chia nhóm cơ theo mục tiêu/số buổi | 4 |
| recommended_schedule_configs | Cấu hình ngày tập đề xuất theo số buổi | 3 |
| injury_area_options | Danh mục vùng chấn thương | 3 |
| system_configs | Cấu hình số của hệ thống | 4 |

### Tương tác và hỗ trợ

| Bảng | Chức năng | Số cột |
|---|---|---|
| chat_messages | Lịch sử hội thoại người dùng với chatbot | 9 |
| notifications | Thông báo người dùng | 11 |
| service_ratings | Đánh giá dịch vụ và phản hồi quản trị | 19 |
| support_sessions | Phiên hỗ trợ trực tiếp | 11 |
| support_messages | Tin nhắn trong phiên hỗ trợ | 9 |

### Nhân vật và nhân viên

| Bảng | Chức năng | Số cột |
|---|---|---|
| pet_profiles | Trạng thái nhân vật đồng hành | 10 |
| user_cosmetic_ownership | Vật phẩm trang trí người dùng sở hữu | 4 |
| work_shifts | Ca làm và đối soát tiền của nhân viên | 16 |

### Cửa hàng

| Bảng | Chức năng | Số cột |
|---|---|---|
| shop_products | Sản phẩm cửa hàng | 14 |
| product_attributes | Danh mục thuộc tính sản phẩm | 2 |
| product_attribute_values | Giá trị của thuộc tính | 3 |
| product_variants | Biến thể sản phẩm | 6 |
| variant_attribute_values | Bảng nối biến thể với các giá trị thuộc tính | 2 |
| shop_cart_items | Dòng giỏ hàng của tài khoản | 6 |
| shop_orders | Đơn hàng online và tại quầy | 25 |
| shop_order_items | Chi tiết hàng hóa được chốt trong đơn | 10 |
| vouchers | Mã giảm giá và điều kiện áp dụng | 15 |
| voucher_scope_products | Danh sách ID sản phẩm thuộc phạm vi voucher | 2 |
| product_reviews | Đánh giá sản phẩm theo đơn mua | 8 |
| customer_addresses | Sổ địa chỉ nhận hàng của khách | 6 |
| customer_product_states | Yêu thích và lần xem gần nhất trên cùng một dòng | 5 |
| shop_order_events | Lịch sử trạng thái và sự kiện đơn hàng | 7 |
| shop_inventory_movements | Nhật ký thay đổi tồn kho | 9 |

## 4. Đặc tả toàn bộ 43 bảng

### 4.1. roles
Vai trò phân quyền.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/Role.java.

Ràng buộc duy nhất: (role_name).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| role_name | VARCHAR(255) | Tên vai trò | NULL; UNIQUE |

### 4.2. users
Tài khoản người dùng.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/User.java.

Ràng buộc duy nhất: (email).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| email_verified | BOOLEAN | Đã xác minh email | NULL |
| password_reset_attempts | INTEGER | Số lần thử đặt lại mật khẩu | NULL |
| status | BOOLEAN | Trạng thái xử lý | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| password_reset_blocked_until | TIMESTAMP(6) | Thời hạn chặn đặt lại mật khẩu | NULL |
| role_id | BIGINT | Mã vai trò | NULL; FK → roles.id |
| email | VARCHAR(255) | Địa chỉ email | NOT NULL; UNIQUE |
| full_name | VARCHAR(255) | Họ và tên | NULL |
| password | VARCHAR(255) | Mật khẩu đã mã hóa | NULL |
| phone | VARCHAR(255) | Số điện thoại | NULL |
| verification_token | VARCHAR(255) | Mã xác minh tài khoản | NULL |

### 4.3. user_profiles
Hồ sơ thể lực và điều kiện tập luyện.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/UserProfile.java.

Ràng buộc duy nhất: (user_id).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| age | INTEGER | Tuổi | NULL |
| available_days_per_week | INTEGER | Số ngày có thể tập mỗi tuần | NULL |
| bmi | FLOAT(53) | Chỉ số BMI | NULL |
| body_fat_percentage | FLOAT(53) | Tỷ lệ mỡ cơ thể | NULL |
| date_of_birth | DATE | Ngày sinh | NULL |
| height | FLOAT(53) | Chiều cao | NULL |
| initial_weight | FLOAT(53) | Cân nặng ban đầu | NULL |
| preferred_session_duration | INTEGER | Thời lượng buổi tập mong muốn | NULL |
| training_experience_months | INTEGER | Kinh nghiệm tập (tháng) | NULL |
| weight | FLOAT(53) | Cân nặng | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| user_id | BIGINT | Mã người dùng | NULL; UNIQUE; FK → users.id |
| preferred_training_days | VARCHAR(500) | Các ngày tập mong muốn | NULL |
| available_equipment | VARCHAR(1000) | Danh sách thiết bị hiện có | NULL |
| disliked_exercises | VARCHAR(1000) | Danh sách bài tập không thích | NULL |
| injury_areas | VARCHAR(1000) | Các vùng chấn thương | NULL |
| daily_activity_level | VARCHAR(255) | Mức vận động hằng ngày | NULL |
| gender | VARCHAR(255) | Giới tính | NULL |
| medical_conditions | VARCHAR(255) | Thông tin sức khỏe | NULL |
| training_location | VARCHAR(255) | Địa điểm tập | NULL |
| fitness_level | ENUM | Mức thể lực | NULL |
| goal | ENUM | Mục tiêu tập luyện | NULL |

Miền giá trị fitness_level: ADVANCED, BEGINNER, INTERMEDIATE.

Miền giá trị goal: ENDURANCE, MAINTENANCE, MUSCLE_GAIN, WEIGHT_LOSS.

### 4.4. memberships
Gói hội viên đã đăng ký.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/Membership.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| end_date | DATE | Ngày kết thúc | NULL |
| is_active | BOOLEAN | Đang hoạt động | NULL |
| price | FLOAT(53) | Giá tiền | NULL |
| start_date | DATE | Ngày bắt đầu | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| paid_at | TIMESTAMP(6) | Thời điểm xác nhận đã trả tiền | NULL |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| notes | VARCHAR(255) | Ghi chú | NULL |
| payment_method | VARCHAR(255) | Phương thức thanh toán | NULL |
| transaction_id | VARCHAR(255) | Mã giao dịch | NULL |
| membership_type | ENUM | Loại hội viên | NULL |
| payment_status | ENUM | Trạng thái thanh toán | NULL |

Miền giá trị membership_type: FREE, VIP.

Miền giá trị payment_status: CANCELLED, EXPIRED, FAILED, PAID, PENDING, REFUNDED.

### 4.5. invoices
Hóa đơn gói hội viên và vật phẩm trang trí.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/Invoice.java.

Ràng buộc duy nhất: (momo_order_id); (transfer_code).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| price | FLOAT(53) | Giá tiền | NULL |
| regenerate_count | INTEGER | Số lần tạo lại thanh toán | NULL |
| cancelled_at | TIMESTAMP(6) | Thời điểm hủy | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| expires_at | TIMESTAMP(6) | Thời điểm hết hạn | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| membership_id | BIGINT | Mã đăng ký hội viên | NULL; FK → memberships.id |
| paid_at | TIMESTAMP(6) | Thời điểm xác nhận đã trả tiền | NULL |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| qr_raw_payload | VARCHAR(1000) | Dữ liệu QR gốc | NULL |
| cosmetic_item_code | VARCHAR(255) | Mã vật phẩm trang trí | NULL |
| deeplink | VARCHAR(255) | Liên kết mở ứng dụng | NULL |
| momo_order_id | VARCHAR(255) | Mã đơn MoMo | NULL; UNIQUE |
| momo_request_id | VARCHAR(255) | Mã yêu cầu MoMo | NULL |
| pay_url | VARCHAR(255) | URL thanh toán | NULL |
| qr_code_url | VARCHAR(255) | URL ảnh QR | NULL |
| result_message | VARCHAR(255) | Thông báo kết quả | NULL |
| transaction_id | VARCHAR(255) | Mã giao dịch | NULL |
| transfer_code | VARCHAR(255) | Mã nội dung chuyển khoản | NULL; UNIQUE |
| invoice_type | ENUM | Loại hóa đơn | NULL |
| membership_type | ENUM | Loại hội viên | NULL |
| status | ENUM | Trạng thái xử lý | NULL |

Miền giá trị invoice_type: COSMETIC, MEMBERSHIP.

Miền giá trị membership_type: FREE, VIP.

Miền giá trị status: CANCELLED, EXPIRED, FAILED, PAID, PENDING, REFUNDED.

### 4.6. exercises
Thư viện bài tập.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/Exercise.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| calories_burned | INTEGER | Năng lượng tiêu hao của bài tập | NULL |
| default_duration_seconds | INTEGER | Thời lượng (giây) mặc định | NULL |
| default_reps | INTEGER | Số lần lặp mặc định | NULL |
| default_sets | INTEGER | Số hiệp mặc định | NULL |
| endurance_score | INTEGER | Điểm phù hợp mục tiêu sức bền | NULL |
| flexibility_score | INTEGER | Điểm phù hợp mục tiêu độ linh hoạt | NULL |
| is_active | BOOLEAN | Đang hoạt động | NULL |
| is_assessment | BOOLEAN | Là bài kiểm tra | NULL |
| maintenance_score | INTEGER | Điểm phù hợp mục tiêu duy trì | NULL |
| muscle_gain_score | INTEGER | Điểm phù hợp mục tiêu tăng cơ | NULL |
| rest_seconds | INTEGER | Thời gian nghỉ (giây) | NULL |
| stamina_cost | INTEGER | Chi phí sức bền | NULL; DEFAULT 10 |
| uses_weight | BOOLEAN | Có sử dụng tạ | NULL |
| weight_loss_score | INTEGER | Điểm phù hợp mục tiêu giảm cân | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| contraindicated_injuries | VARCHAR(255) | Các chấn thương chống chỉ định | NULL |
| description | VARCHAR(255) | Nội dung mô tả | NULL |
| image_url | VARCHAR(255) | Đường dẫn ảnh đại diện | NULL |
| name | VARCHAR(255) | Tên hiển thị | NULL |
| secondary_muscle_groups | VARCHAR(255) | Nhóm cơ phụ | NULL |
| video_url | VARCHAR(255) | URL video bài tập | NULL |
| assessment_metric_type | ENUM | Chỉ tiêu kiểm tra | NULL |
| difficulty | ENUM | Độ khó | NULL |
| muscle_group | ENUM | Nhóm cơ | NULL |

Miền giá trị assessment_metric_type: PLANK_SECONDS, PUSHUP_REPS, SQUAT_REPS.

Miền giá trị difficulty: EASY, HARD, MEDIUM.

Miền giá trị muscle_group: ARMS, BACK, CARDIO, CHEST, CORE, FULL_BODY, LEGS, SHOULDERS.

### 4.7. foods
Thư viện món ăn.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/Food.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| calories | INTEGER | Năng lượng món ăn | NULL |
| fat_grams | FLOAT(53) | Lượng chất béo (g) | NULL |
| is_active | BOOLEAN | Đang hoạt động | NULL |
| protein_grams | FLOAT(53) | Lượng protein (g) | NULL |
| weight_grams | FLOAT(53) | Khối lượng khẩu phần (g) | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| image_url | VARCHAR(255) | Đường dẫn ảnh đại diện | NULL |
| name | VARCHAR(255) | Tên hiển thị | NULL |
| suitable_goals | VARCHAR(255) | Các mục tiêu phù hợp | NULL |
| ingredients | CLOB | Nguyên liệu | NULL |
| instructions | CLOB | Hướng dẫn chế biến | NULL |

### 4.8. endurance_tests
Kết quả kiểm tra sức bền hiện tại của từng người dùng.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/EnduranceTest.java.

Ràng buộc duy nhất: (user_id).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| plank_seconds | INTEGER | Thành tích plank (giây) | NULL |
| pushup_reps | INTEGER | Số lần chống đẩy | NULL |
| squat_reps | INTEGER | Số lần squat | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| tested_at | TIMESTAMP(6) | Thời điểm kiểm tra | NULL |
| user_id | BIGINT | Mã người dùng | NOT NULL; UNIQUE; FK → users.id |

### 4.9. progress_tracking
Lịch sử chỉ số cơ thể.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/ProgressTracking.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| arm_cm | FLOAT(53) | Vòng tay (cm) | NULL |
| bmi | FLOAT(53) | Chỉ số BMI | NULL |
| body_fat_percentage | FLOAT(53) | Tỷ lệ mỡ cơ thể | NULL |
| chest_cm | FLOAT(53) | Vòng ngực (cm) | NULL |
| height | FLOAT(53) | Chiều cao | NULL |
| hip_cm | FLOAT(53) | Vòng hông (cm) | NULL |
| muscle_mass_kg | FLOAT(53) | Khối lượng cơ (kg) | NULL |
| recorded_date | DATE | Ngày ghi nhận | NULL |
| thigh_cm | FLOAT(53) | Vòng đùi (cm) | NULL |
| waist_cm | FLOAT(53) | Vòng eo (cm) | NULL |
| weight | FLOAT(53) | Cân nặng | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| recorded_at | TIMESTAMP(6) | Thời điểm ghi nhận | NULL |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| notes | VARCHAR(255) | Ghi chú | NULL |
| source | ENUM | Nguồn ghi nhận | NULL |

Miền giá trị source: MANUAL, PROFILE, WEEKLY_CHECKOUT.

### 4.10. workout_plans
Giáo án cá nhân và giáo án mẫu.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/WorkoutPlan.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| current_mana | INTEGER | Sức bền hiện tại | NULL |
| current_week | INTEGER | Tuần hiện tại | NULL |
| difficulty_adjustment | INTEGER | Mức điều chỉnh độ khó | NULL |
| duration_weeks | INTEGER | Thời lượng giáo án (tuần) | NULL |
| estimated_weeks | INTEGER | Số tuần ước tính | NULL |
| exercises_adjustment | INTEGER | Mức điều chỉnh số bài tập | NULL |
| fitness_score | INTEGER | Điểm thể lực | NULL |
| is_active | BOOLEAN | Đang hoạt động | NULL |
| is_ai_generated | BOOLEAN | Cờ giáo án được sinh tự động | NULL |
| is_completed | BOOLEAN | Đã hoàn thành | NULL |
| is_fitness_improvement | BOOLEAN | Giáo án cải thiện thể lực | NULL |
| is_template | BOOLEAN | Là giáo án mẫu | NULL |
| last_mana_regen_date | DATE | Ngày hồi sức bền gần nhất | NULL |
| last_training_date | DATE | Ngày tập gần nhất | NULL |
| max_mana | INTEGER | Sức bền tối đa | NULL |
| reps_adjustment | INTEGER | Mức điều chỉnh số lần lặp | NULL |
| required_max_session_mana_cost | INTEGER | Chi phí sức bền lớn nhất cần cho một buổi | NULL |
| sessions_per_week | INTEGER | Số buổi mỗi tuần | NULL |
| sets_adjustment | INTEGER | Mức điều chỉnh số hiệp | NULL |
| starting_bmi | FLOAT(53) | BMI khi bắt đầu | NULL |
| starting_weight | FLOAT(53) | Cân nặng khi bắt đầu | NULL |
| target_achieved | BOOLEAN | Đã đạt mục tiêu | NULL |
| target_baseline_value | FLOAT(53) | Giá trị ban đầu của chỉ tiêu | NULL |
| target_current_value | FLOAT(53) | Giá trị hiện tại của chỉ tiêu | NULL |
| target_goal_value | FLOAT(53) | Giá trị mục tiêu | NULL |
| target_metric_type | TINYINT | Chỉ tiêu mục tiêu dạng số: 0 chống đẩy, 1 plank, 2 squat | NULL; CHECK 0..2 |
| week_start_date | DATE | Ngày bắt đầu tuần | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| original_plan_id | BIGINT | Mã giáo án nguồn | NULL; Logic → workout_plans (không FK) |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| confirmed_schedule_dows | VARCHAR(255) | Các ngày trong tuần đã xác nhận | NULL |
| description | VARCHAR(255) | Nội dung mô tả | NULL |
| plan_name | VARCHAR(255) | Tên giáo án | NULL |
| weight_adjustment_note | VARCHAR(255) | Ghi chú điều chỉnh tạ | NULL |
| body_type | ENUM | Phân loại thể trạng | NULL |
| fitness_level | ENUM | Mức thể lực | NULL |
| goal | ENUM | Mục tiêu tập luyện | NULL |
| target_level | ENUM | Trình độ mục tiêu | NULL |

Miền giá trị body_type: CAN_DOI, CAO_GAY, CO_BAP, GAY_CAN_DOI, THUA_CAN, VAN_DONG_VIEN.

Miền giá trị fitness_level: AVERAGE, EXCELLENT, GOOD, WEAK.

Miền giá trị goal: ENDURANCE, MAINTENANCE, MUSCLE_GAIN, WEIGHT_LOSS.

Miền giá trị target_level: ADVANCED, BEGINNER, INTERMEDIATE.

### 4.11. workout_plan_days
Ngày tập trong giáo án.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/WorkoutPlanDay.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| day_of_week | INTEGER | Ngày trong tuần | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| workout_plan_id | BIGINT | Mã giáo án | NULL; FK → workout_plans.id |
| day_name | VARCHAR(255) | Tên ngày tập | NULL |

### 4.12. workout_plan_exercises
Bài tập và định mức trong ngày tập.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/WorkoutPlanExercise.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| base_weight_kg | FLOAT(53) | Mức tạ gốc (kg) | NULL |
| current_recommended_weight_kg | FLOAT(53) | Mức tạ đề xuất hiện tại (kg) | NULL |
| current_weight_kg | FLOAT(53) | Mức tạ hiện tại (kg) | NULL |
| duration_seconds | INTEGER | Thời lượng (giây) | NULL |
| is_assessment | BOOLEAN | Là bài kiểm tra | NULL |
| order_index | INTEGER | Thứ tự bài tập | NULL |
| recommended_weight_kg | FLOAT(53) | Mức tạ đề xuất (kg) | NULL |
| reps | INTEGER | Số lần lặp | NULL |
| rest_seconds | INTEGER | Thời gian nghỉ (giây) | NULL |
| sets | INTEGER | Số hiệp | NULL |
| weight_updated_week | INTEGER | Tuần cập nhật tạ | NULL |
| exercise_id | BIGINT | Mã bài tập | NULL; FK → exercises.id |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| last_low_adjustment_log_id | BIGINT | Mã log đã dùng cho lần điều chỉnh giảm | NULL; Logic → session_exercise_logs (không FK) |
| plan_day_id | BIGINT | Mã ngày tập | NULL; FK → workout_plan_days.id |
| notes | VARCHAR(255) | Ghi chú | NULL |

### 4.13. plan_muscle_group_weight
Hệ số ưu tiên nhóm cơ theo giáo án.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/WorkoutPlanMuscleGroupWeight.java.

Ràng buộc duy nhất: (workout_plan_id, muscle_group).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| multiplier | FLOAT(53) | Hệ số ưu tiên | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| workout_plan_id | BIGINT | Mã giáo án | NULL; FK → workout_plans.id |
| muscle_group | ENUM | Nhóm cơ | NULL |

Miền giá trị muscle_group: ARMS, BACK, CARDIO, CHEST, CORE, FULL_BODY, LEGS, SHOULDERS.

### 4.14. workout_sessions
Buổi tập thực tế.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/WorkoutSession.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| checkout_body_fat | FLOAT(53) | Tỷ lệ mỡ lúc kết thúc | NULL |
| checkout_weight | FLOAT(53) | Cân nặng lúc kết thúc | NULL |
| completion_rate | INTEGER | Tỷ lệ hoàn thành buổi | NULL |
| duration_minutes | INTEGER | Thời lượng (phút) | NULL |
| is_custom | BOOLEAN | Buổi tự tạo | NULL |
| is_last_session_of_week | BOOLEAN | Buổi cuối tuần | NULL |
| scheduled_time | TIME(6) | Giờ tập dự kiến | NULL |
| session_date | DATE | Ngày tập | NULL |
| total_calories_burned | INTEGER | Tổng năng lượng tiêu hao | NULL |
| week_number | INTEGER | Tuần thực hiện | NULL |
| check_in_time | TIMESTAMP(6) | Thời điểm bắt đầu tập | NULL |
| check_out_time | TIMESTAMP(6) | Thời điểm kết thúc tập | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| plan_day_id | BIGINT | Mã ngày tập | NULL; FK → workout_plan_days.id |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| workout_plan_id | BIGINT | Mã giáo án | NULL; FK → workout_plans.id |
| custom_session_name | VARCHAR(255) | Tên buổi tự tạo | NULL |
| notes | VARCHAR(255) | Ghi chú | NULL |
| status | ENUM | Trạng thái xử lý | NULL |

Miền giá trị status: CHECKED_IN, COMPLETED, SCHEDULED, SKIPPED.

### 4.15. session_exercise_logs
Kết quả từng bài tập trong buổi.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/SessionExerciseLog.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| completion_percent | INTEGER | Tỷ lệ hoàn thành bài | NULL |
| duration_seconds | INTEGER | Thời lượng (giây) | NULL |
| is_completed | BOOLEAN | Đã hoàn thành | NULL |
| reps_completed | INTEGER | Số lần lặp đã hoàn thành | NULL |
| sets_completed | INTEGER | Số hiệp đã hoàn thành | NULL |
| weight_used_kg | FLOAT(53) | Mức tạ đã sử dụng (kg) | NULL |
| exercise_id | BIGINT | Mã bài tập | NULL; FK → exercises.id |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| logged_at | TIMESTAMP(6) | Thời điểm ghi log | NULL |
| session_id | BIGINT | Mã buổi tập hoặc phiên hỗ trợ | NULL; FK → workout_sessions.id |
| notes | VARCHAR(255) | Ghi chú | NULL |

### 4.16. weekly_reviews
Đánh giá giáo án theo tuần.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/WeeklyReview.java.

Ràng buộc duy nhất: (user_id, workout_plan_id, week_number).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| rating | INTEGER | Điểm đánh giá | NULL |
| week_number | INTEGER | Tuần thực hiện | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| workout_plan_id | BIGINT | Mã giáo án | NULL; FK → workout_plans.id |
| comment | VARCHAR(255) | Nội dung đánh giá | NULL |

### 4.17. muscle_split_configs
Cấu hình chia nhóm cơ theo mục tiêu/số buổi.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/MuscleSplitConfig.java.

Ràng buộc duy nhất: (goal, sessions_per_week).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| sessions_per_week | INTEGER | Số buổi mỗi tuần | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| day_groups | VARCHAR(1000) | Cấu hình nhóm cơ cho các ngày | NOT NULL |
| goal | ENUM | Mục tiêu tập luyện | NOT NULL |

Miền giá trị goal: ENDURANCE, MAINTENANCE, MUSCLE_GAIN, WEIGHT_LOSS.

### 4.18. recommended_schedule_configs
Cấu hình ngày tập đề xuất theo số buổi.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/RecommendedScheduleConfig.java.

Ràng buộc duy nhất: (sessions_per_week).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| sessions_per_week | INTEGER | Số buổi mỗi tuần | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| recommended_days | VARCHAR(30) | Các ngày tập đề xuất | NOT NULL |

### 4.19. injury_area_options
Danh mục vùng chấn thương.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/InjuryAreaOption.java.

Ràng buộc duy nhất: (code); (label).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| code | VARCHAR(100) | Mã nghiệp vụ | NOT NULL |
| label | VARCHAR(100) | Nhãn hiển thị | NOT NULL |

### 4.20. system_configs
Cấu hình số của hệ thống.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/SystemConfig.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| config_value | FLOAT(53) | Giá trị số cấu hình | NULL |
| category | VARCHAR(255) | Nhóm phân loại | NULL |
| config_key | VARCHAR(255) | Khóa cấu hình | PK; NOT NULL |
| description | CLOB | Nội dung mô tả | NULL |

### 4.21. chat_messages
Lịch sử hội thoại người dùng với chatbot.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/ChatMessage.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| attachment_size | BIGINT | Tệp đính kèm: kích thước | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| content | VARCHAR(2000) | Nội dung tin nhắn | NULL |
| attachment_name | VARCHAR(255) | Tệp đính kèm: tên | NULL |
| attachment_type | VARCHAR(255) | Tệp đính kèm: loại | NULL |
| attachment_url | VARCHAR(255) | Tệp đính kèm: đường dẫn | NULL |
| sender | VARCHAR(255) | Nguồn gửi USER/BOT | NULL |

### 4.22. notifications
Thông báo người dùng.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/Notification.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| is_read | BOOLEAN | Đã đọc | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| ref_id | BIGINT | Mã đối tượng tham chiếu theo ref_type | NULL |
| scheduled_at | TIMESTAMP(6) | Thời điểm dự kiến gửi | NULL |
| sent_at | TIMESTAMP(6) | Thời điểm đã gửi | NULL |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| message | VARCHAR(255) | Nội dung thông báo | NULL |
| ref_type | VARCHAR(255) | Loại đối tượng tham chiếu | NULL |
| title | VARCHAR(255) | Tiêu đề | NULL |
| type | VARCHAR(255) | Loại nghiệp vụ | NULL |

### 4.23. service_ratings
Đánh giá dịch vụ và phản hồi quản trị.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/ServiceRating.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| is_public | BOOLEAN | Cho phép hiển thị công khai | NULL |
| rating | INTEGER | Điểm đánh giá | NULL |
| attachment_size | BIGINT | Tệp đính kèm: kích thước | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| replied_at | TIMESTAMP(6) | Thời điểm phản hồi | NULL |
| reply_attachment_size | BIGINT | Tệp đính kèm phản hồi: kích thước | NULL |
| updated_at | TIMESTAMP(6) | Thời điểm cập nhật | NULL |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| admin_reply | VARCHAR(255) | Nội dung phản hồi quản trị | NULL |
| attachment_name | VARCHAR(255) | Tệp đính kèm: tên | NULL |
| attachment_type | VARCHAR(255) | Tệp đính kèm: loại | NULL |
| attachment_url | VARCHAR(255) | Tệp đính kèm: đường dẫn | NULL |
| comment | VARCHAR(255) | Nội dung đánh giá | NULL |
| reply_attachment_name | VARCHAR(255) | Tệp đính kèm phản hồi: tên | NULL |
| reply_attachment_type | VARCHAR(255) | Tệp đính kèm phản hồi: loại | NULL |
| reply_attachment_url | VARCHAR(255) | Tệp đính kèm phản hồi: đường dẫn | NULL |
| service_type | VARCHAR(255) | Loại dịch vụ được đánh giá | NULL |
| title | VARCHAR(255) | Tiêu đề | NULL |

### 4.24. support_sessions
Phiên hỗ trợ trực tiếp.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/SupportSession.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| user_rating | INTEGER | Điểm khách đánh giá hỗ trợ | NULL |
| accepted_at | TIMESTAMP(6) | Thời điểm tiếp nhận | NULL |
| admin_id | BIGINT | Mã người hỗ trợ | NULL; FK → users.id |
| closed_at | TIMESTAMP(6) | Thời điểm đóng | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| rated_at | TIMESTAMP(6) | Thời điểm đánh giá | NULL |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| user_rating_comment | VARCHAR(1000) | Nhận xét phiên hỗ trợ | NULL |
| subject | VARCHAR(255) | Chủ đề hỗ trợ | NULL |
| status | ENUM | Trạng thái xử lý | NULL |

Miền giá trị status: ACTIVE, CLOSED, PENDING, REJECTED.

### 4.25. support_messages
Tin nhắn trong phiên hỗ trợ.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/entity/SupportMessage.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| attachment_size | BIGINT | Tệp đính kèm: kích thước | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| session_id | BIGINT | Mã buổi tập hoặc phiên hỗ trợ | NULL; FK → support_sessions.id |
| content | VARCHAR(2000) | Nội dung tin nhắn | NULL |
| attachment_name | VARCHAR(255) | Tệp đính kèm: tên | NULL |
| attachment_type | VARCHAR(255) | Tệp đính kèm: loại | NULL |
| attachment_url | VARCHAR(255) | Tệp đính kèm: đường dẫn | NULL |
| sender_role | VARCHAR(255) | Vai trò bên gửi | NULL |

### 4.26. pet_profiles
Trạng thái nhân vật đồng hành.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/pet/PetProfile.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| current_streak | INTEGER | Chuỗi duy trì hiện tại | NULL |
| missed_streak | INTEGER | Chuỗi bỏ lỡ | NULL |
| web_count | INTEGER | Số mạng nhện hiển thị | NULL |
| last_calculated_at | TIMESTAMP(6) | Thời điểm tính trạng thái gần nhất | NULL |
| user_id | BIGINT | Mã người dùng | PK; NOT NULL; FK → users.id |
| equipped_hair | VARCHAR(255) | Tóc đang trang bị | NULL |
| equipped_pants | VARCHAR(255) | Quần đang trang bị | NULL |
| equipped_shirt | VARCHAR(255) | Áo đang trang bị | NULL |
| aura_tier | ENUM | Cấp hiệu ứng hào quang | NULL |
| stage | ENUM | Giai đoạn thể trạng nhân vật | NULL |

Miền giá trị aura_tier: BLACK, BLUE, GREEN, NONE, PURPLE, RED, YELLOW.

Miền giá trị stage: AVERAGE, FIT, LEAN, OVERWEIGHT, SLIM.

### 4.27. user_cosmetic_ownership
Vật phẩm trang trí người dùng sở hữu.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/pet/UserCosmeticOwnership.java.

Ràng buộc duy nhất: (user_id, cosmetic_code).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| purchased_at | TIMESTAMP(6) | Thời điểm mua | NULL |
| user_id | BIGINT | Mã người dùng | NOT NULL; Logic → users (không FK) |
| cosmetic_code | VARCHAR(255) | Mã vật phẩm sở hữu | NOT NULL |

### 4.28. work_shifts
Ca làm và đối soát tiền của nhân viên.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shift/WorkShift.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| cash_at_start | FLOAT(53) | Tiền mặt đầu ca | NULL |
| cash_counted | FLOAT(53) | Tiền mặt kiểm đếm | NULL |
| cash_difference | FLOAT(53) | Chênh lệch tiền mặt | NULL |
| expected_cash | FLOAT(53) | Tiền mặt dự kiến | NULL |
| planned_end | TIME(6) | Giờ kết thúc dự kiến | NULL |
| planned_start | TIME(6) | Giờ bắt đầu dự kiến | NULL |
| pos_bank_revenue | FLOAT(53) | Doanh thu chuyển khoản tại quầy | NULL |
| pos_cash_revenue | FLOAT(53) | Doanh thu tiền mặt tại quầy | NULL |
| shift_date | DATE | Ngày làm việc | NULL |
| check_in_at | TIMESTAMP(6) | Thời điểm vào ca | NULL |
| check_out_at | TIMESTAMP(6) | Thời điểm kết ca | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| handover_note | VARCHAR(2000) | Ghi chú bàn giao | NULL |
| status | ENUM | Trạng thái xử lý | NULL |

Miền giá trị status: CHECKED_IN, COMPLETED, SCHEDULED.

### 4.29. shop_products
Sản phẩm cửa hàng.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/Product.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| active | BOOLEAN | Đang hoạt động | NULL |
| price | FLOAT(53) | Giá tiền | NOT NULL |
| sale_price | FLOAT(53) | Giá bán ưu đãi | NULL |
| stock | INTEGER | Số lượng tồn | NOT NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| description | VARCHAR(2000) | Nội dung mô tả | NULL |
| images | VARCHAR(20000) | Danh sách URL ảnh, JSON trong VARCHAR(20000) | NULL |
| brand | VARCHAR(255) | Thương hiệu | NULL |
| image_url | VARCHAR(255) | Đường dẫn ảnh đại diện | NULL |
| name | VARCHAR(255) | Tên hiển thị | NOT NULL |
| required_equipment_code | VARCHAR(255) | Mã thiết bị liên quan | NULL |
| suitable_goals | VARCHAR(255) | Các mục tiêu phù hợp | NULL |
| category | ENUM | Nhóm phân loại | NULL |

Miền giá trị category: EQUIPMENT, FOOD, SUPPLEMENT.

### 4.30. product_attributes
Danh mục thuộc tính sản phẩm.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/ProductAttribute.java.

Ràng buộc duy nhất: (name).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| name | VARCHAR(255) | Tên hiển thị | NOT NULL; UNIQUE |

### 4.31. product_attribute_values
Giá trị của thuộc tính.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/ProductAttributeValue.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| attribute_id | BIGINT | Mã thuộc tính | NULL; FK → product_attributes.id |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| value | VARCHAR(255) | Giá trị thuộc tính hoặc mức giảm | NOT NULL |

### 4.32. product_variants
Biến thể sản phẩm.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/ProductVariant.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| active | BOOLEAN | Đang hoạt động | NULL |
| price_override | FLOAT(53) | Giá riêng của biến thể | NULL |
| stock | INTEGER | Số lượng tồn | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| product_id | BIGINT | Mã sản phẩm | NULL; FK → shop_products.id |
| sku | VARCHAR(255) | Mã SKU biến thể | NULL |

### 4.33. variant_attribute_values
Bảng nối biến thể với các giá trị thuộc tính.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/ProductVariant.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| value_id | BIGINT | Mã giá trị thuộc tính | PK; NOT NULL; FK → product_attribute_values.id |
| variant_id | BIGINT | Mã biến thể | PK; NOT NULL; FK → product_variants.id |

### 4.34. shop_cart_items
Dòng giỏ hàng của tài khoản.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/CartItem.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| quantity | INTEGER | Số lượng | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| product_id | BIGINT | Mã sản phẩm | NULL; FK → shop_products.id |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| variant_id | BIGINT | Mã biến thể | NULL; Logic → product_variants (không FK) |
| variant_label | VARCHAR(255) | Tên phân loại tại thời điểm mua/thêm giỏ | NULL |

### 4.35. shop_orders
Đơn hàng online và tại quầy.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/StoreOrder.java.

Ràng buộc duy nhất: (gateway_reference); (transfer_code).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| discount | FLOAT(53) | Khoản giảm giá | NULL |
| shipping_fee | FLOAT(53) | Phí vận chuyển | NULL |
| subtotal | FLOAT(53) | Tổng tiền hàng | NULL |
| total | FLOAT(53) | Tổng phải trả | NULL |
| voucher_discount | FLOAT(53) | Số tiền được giảm bằng voucher | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| created_by_staff_id | BIGINT | Mã nhân viên tạo đơn | NULL; Logic → users (không FK) |
| expires_at | TIMESTAMP(6) | Thời điểm hết hạn | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| paid_at | TIMESTAMP(6) | Thời điểm xác nhận đã trả tiền | NULL |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| note | VARCHAR(1000) | Ghi chú | NULL |
| qr_raw_payload | VARCHAR(1000) | Dữ liệu QR gốc | NULL |
| shipping_address | VARCHAR(1000) | Địa chỉ giao chốt trên đơn | NULL |
| payment_url | VARCHAR(2000) | URL thanh toán của cổng | NULL |
| gateway_reference | VARCHAR(255) | Mã tham chiếu cổng thanh toán | NULL; UNIQUE |
| payment_method | VARCHAR(255) | Phương thức thanh toán | NULL |
| phone | VARCHAR(255) | Số điện thoại | NULL |
| qr_code_url | VARCHAR(255) | URL ảnh QR | NULL |
| receiver_name | VARCHAR(255) | Tên người nhận | NULL |
| transaction_id | VARCHAR(255) | Mã giao dịch | NULL |
| transfer_code | VARCHAR(255) | Mã nội dung chuyển khoản | NULL; UNIQUE |
| voucher_code | VARCHAR(255) | Mã voucher chốt trên đơn | NULL |
| channel | ENUM | Kênh bán | NULL |
| status | ENUM | Trạng thái xử lý | NULL |

Miền giá trị channel: ONLINE, POS.

Miền giá trị status: CANCELLED, COMPLETED, CONFIRMED, DELIVERED, EXPIRED, PAID, PENDING_PAYMENT, PREPARING, SHIPPING.

### 4.36. shop_order_items
Chi tiết hàng hóa được chốt trong đơn.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/OrderItem.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| line_total | FLOAT(53) | Thành tiền của dòng | NULL |
| quantity | INTEGER | Số lượng | NULL |
| unit_price | FLOAT(53) | Đơn giá tại thời điểm mua | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| order_id | BIGINT | Mã đơn hàng | NULL; FK → shop_orders.id |
| product_id | BIGINT | Mã sản phẩm | NULL; Logic → shop_products (không FK) |
| variant_id | BIGINT | Mã biến thể | NULL; Logic → product_variants (không FK) |
| image_url | VARCHAR(255) | Đường dẫn ảnh đại diện | NULL |
| product_name | VARCHAR(255) | Tên sản phẩm tại thời điểm mua | NULL |
| variant_label | VARCHAR(255) | Tên phân loại tại thời điểm mua/thêm giỏ | NULL |

### 4.37. vouchers
Mã giảm giá và điều kiện áp dụng.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/Voucher.java.

Ràng buộc duy nhất: (code).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| active | BOOLEAN | Đang hoạt động | NULL |
| max_discount_amount | FLOAT(53) | Mức giảm tối đa | NULL |
| min_order_amount | FLOAT(53) | Giá trị đơn tối thiểu | NULL |
| usage_limit | INTEGER | Giới hạn lượt sử dụng | NULL |
| used_count | INTEGER | Số lượt đã dùng | NULL |
| value | FLOAT(53) | Giá trị thuộc tính hoặc mức giảm | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| end_at | TIMESTAMP(6) | Thời điểm kết thúc hiệu lực | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| start_at | TIMESTAMP(6) | Thời điểm bắt đầu hiệu lực | NULL |
| code | VARCHAR(255) | Mã nghiệp vụ | NULL; UNIQUE |
| description | VARCHAR(255) | Nội dung mô tả | NULL |
| scope_category | ENUM | Danh mục áp dụng | NULL |
| scope_type | ENUM | Loại phạm vi áp dụng | NULL |
| type | ENUM | Loại nghiệp vụ | NULL |

Miền giá trị scope_category: EQUIPMENT, FOOD, SUPPLEMENT.

Miền giá trị scope_type: ALL, CATEGORY, PRODUCT.

Miền giá trị type: AMOUNT, PERCENT.

### 4.38. voucher_scope_products
Danh sách ID sản phẩm thuộc phạm vi voucher.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/Voucher.java.

Bảng này chưa có khóa chính trong DDL hiện tại.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| product_id | BIGINT | Mã sản phẩm | NULL; Logic → shop_products (không FK) |
| voucher_id | BIGINT | Mã voucher | NOT NULL; FK → vouchers.id |

### 4.39. product_reviews
Đánh giá sản phẩm theo đơn mua.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/ProductReview.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| rating | INTEGER | Điểm đánh giá | NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| order_id | BIGINT | Mã đơn hàng | NULL; FK → shop_orders.id |
| product_id | BIGINT | Mã sản phẩm | NULL; FK → shop_products.id |
| user_id | BIGINT | Mã người dùng | NULL; FK → users.id |
| comment | VARCHAR(2000) | Nội dung đánh giá | NULL |
| images | VARCHAR(20000) | Danh sách URL ảnh, JSON trong VARCHAR(20000) | NULL |

### 4.40. customer_addresses
Sổ địa chỉ nhận hàng của khách.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/CustomerAddress.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| default_address | BOOLEAN | Địa chỉ mặc định | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| user_id | BIGINT | Mã người dùng | NULL; Logic → users (không FK) |
| address | VARCHAR(1000) | Địa chỉ nhận hàng | NULL |
| phone | VARCHAR(255) | Số điện thoại | NULL |
| receiver_name | VARCHAR(255) | Tên người nhận | NULL |

### 4.41. customer_product_states
Yêu thích và lần xem gần nhất trên cùng một dòng.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/CustomerProductState.java.

Ràng buộc duy nhất: (user_id, product_id).

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| wishlisted | BOOLEAN | Được đánh dấu yêu thích | NOT NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| product_id | BIGINT | Mã sản phẩm | NULL; Logic → shop_products (không FK) |
| user_id | BIGINT | Mã người dùng | NULL; Logic → users (không FK) |
| viewed_at | TIMESTAMP(6) | Lần xem gần nhất | NULL |

### 4.42. shop_order_events
Lịch sử trạng thái và sự kiện đơn hàng.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/ShopOrderEvent.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| order_id | BIGINT | Mã đơn hàng | NULL; Logic → shop_orders (không FK) |
| note | VARCHAR(1000) | Ghi chú | NULL |
| actor | VARCHAR(255) | Người/nguồn thực hiện | NULL |
| from_status | VARCHAR(255) | Trạng thái trước sự kiện | NULL |
| to_status | VARCHAR(255) | Trạng thái sau sự kiện | NULL |

### 4.43. shop_inventory_movements
Nhật ký thay đổi tồn kho.
Nguồn mã: gym-management/src/main/java/com/example/gymmanagement/shop/ShopInventoryMovement.java.

| Cột | Kiểu H2 | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| after_stock | INTEGER | Tồn sau thay đổi | NOT NULL |
| before_stock | INTEGER | Tồn trước thay đổi | NOT NULL |
| created_at | TIMESTAMP(6) | Thời điểm tạo | NULL |
| id | BIGINT | Mã định danh | PK; Tự tăng; NOT NULL; DEFAULT as |
| order_id | BIGINT | Mã đơn hàng | NULL; Logic → shop_orders (không FK) |
| product_id | BIGINT | Mã sản phẩm | NULL; Logic → shop_products (không FK) |
| variant_id | BIGINT | Mã biến thể | NULL; Logic → product_variants (không FK) |
| reason | VARCHAR(1000) | Lý do thay đổi | NULL |
| actor | VARCHAR(255) | Người/nguồn thực hiện | NULL |

## 5. Thông số ERD

Trong quan hệ dưới đây, bảng cha có 0..N dòng con nếu cột FK không duy nhất; có 0..1 dòng con nếu FK đồng thời UNIQUE hoặc là toàn bộ PK. Mỗi dòng con tham chiếu 1 cha nếu FK NOT NULL, hoặc 0..1 cha nếu FK cho phép NULL. Đây là bội số của cấu trúc vật lý, không tự suy diễn bắt buộc theo nghiệp vụ.

### 5.1. Các khóa ngoại thực tế

| Bảng con.cột | Bảng cha.cột | Số cha trên mỗi con | Số con trên mỗi cha |
|---|---|---|---|
| chat_messages.user_id | users.id | 0..1 | 0..N |
| endurance_tests.user_id | users.id | 1 | 0..1 |
| invoices.membership_id | memberships.id | 0..1 | 0..N |
| invoices.user_id | users.id | 0..1 | 0..N |
| memberships.user_id | users.id | 0..1 | 0..N |
| notifications.user_id | users.id | 0..1 | 0..N |
| pet_profiles.user_id | users.id | 1 | 0..1 |
| plan_muscle_group_weight.workout_plan_id | workout_plans.id | 0..1 | 0..N |
| product_attribute_values.attribute_id | product_attributes.id | 0..1 | 0..N |
| product_reviews.order_id | shop_orders.id | 0..1 | 0..N |
| product_reviews.product_id | shop_products.id | 0..1 | 0..N |
| product_reviews.user_id | users.id | 0..1 | 0..N |
| product_variants.product_id | shop_products.id | 0..1 | 0..N |
| progress_tracking.user_id | users.id | 0..1 | 0..N |
| service_ratings.user_id | users.id | 0..1 | 0..N |
| session_exercise_logs.exercise_id | exercises.id | 0..1 | 0..N |
| session_exercise_logs.session_id | workout_sessions.id | 0..1 | 0..N |
| shop_cart_items.product_id | shop_products.id | 0..1 | 0..N |
| shop_cart_items.user_id | users.id | 0..1 | 0..N |
| shop_order_items.order_id | shop_orders.id | 0..1 | 0..N |
| shop_orders.user_id | users.id | 0..1 | 0..N |
| support_messages.session_id | support_sessions.id | 0..1 | 0..N |
| support_sessions.admin_id | users.id | 0..1 | 0..N |
| support_sessions.user_id | users.id | 0..1 | 0..N |
| user_profiles.user_id | users.id | 0..1 | 0..1 |
| users.role_id | roles.id | 0..1 | 0..N |
| variant_attribute_values.value_id | product_attribute_values.id | 1 | 0..N |
| variant_attribute_values.variant_id | product_variants.id | 1 | 0..N |
| voucher_scope_products.voucher_id | vouchers.id | 1 | 0..N |
| weekly_reviews.user_id | users.id | 0..1 | 0..N |
| weekly_reviews.workout_plan_id | workout_plans.id | 0..1 | 0..N |
| work_shifts.user_id | users.id | 0..1 | 0..N |
| workout_plan_days.workout_plan_id | workout_plans.id | 0..1 | 0..N |
| workout_plan_exercises.exercise_id | exercises.id | 0..1 | 0..N |
| workout_plan_exercises.plan_day_id | workout_plan_days.id | 0..1 | 0..N |
| workout_plans.user_id | users.id | 0..1 | 0..N |
| workout_sessions.plan_day_id | workout_plan_days.id | 0..1 | 0..N |
| workout_sessions.user_id | users.id | 0..1 | 0..N |
| workout_sessions.workout_plan_id | workout_plans.id | 0..1 | 0..N |

### 5.2. Liên kết bằng ID trong ứng dụng, chưa có FK

| Bảng.cột | Đối tượng liên quan |
|---|---|
| customer_addresses.user_id | users |
| customer_product_states.user_id | users |
| customer_product_states.product_id | shop_products |
| shop_order_events.order_id | shop_orders |
| shop_inventory_movements.product_id | shop_products |
| shop_inventory_movements.variant_id | product_variants |
| shop_inventory_movements.order_id | shop_orders |
| shop_cart_items.variant_id | product_variants |
| shop_order_items.product_id | shop_products |
| shop_order_items.variant_id | product_variants |
| shop_orders.created_by_staff_id | users |
| voucher_scope_products.product_id | shop_products |
| user_cosmetic_ownership.user_id | users |
| workout_plans.original_plan_id | workout_plans |
| workout_plan_exercises.last_low_adjustment_log_id | session_exercise_logs |

notifications.ref_id là tham chiếu đa loại, phụ thuộc ref_type; không thể gán chung một FK. Các mã dạng chuỗi như voucher_code hoặc cosmetic_code cần chú thích theo nghiệp vụ, không tự vẽ quan hệ FK.

ERD_43_bang.mmd chứa đủ 43 bảng và toàn bộ cột cùng 39 FK. schema_43_bang.sql là DDL tham chiếu từ H2 tạm, không phải migration để chạy lên dữ liệu đang dùng. Để bố trí bản in: giữ một hình tổng quan đủ bảng, sau đó phóng to theo 6 nhóm tại phần 3; các bảng tham chiếu chéo có thể lặp trên hình nhóm và ghi “tham chiếu”.

## 6. Thông số cập nhật các phần chức năng

### Yêu cầu và Use Case
Tác nhân: khách vãng lai, hội viên (USER), nhân viên (STAFF), quản trị (ADMIN); cổng thanh toán là hệ thống bên ngoài. Bổ sung các ca sử dụng: tìm/lọc sản phẩm; xem chi tiết/chọn biến thể; quản lý giỏ; yêu thích/đã xem/so sánh; quản lý địa chỉ; đặt đơn và chọn thanh toán; theo dõi/hủy đơn hợp lệ; đánh giá có ảnh; quản lý thuộc tính/biến thể; xử lý đơn; bán tại quầy; xem báo cáo và nhật ký kho. Các ca đang có được cập nhật, không tạo bản sao chỉ vì thay giao diện.

### Giao diện và tuyến đường
/shop: danh mục công khai, giỏ khách vãng lai, phân loại. /shop/product/:id: chi tiết, bộ ảnh, biến thể, đánh giá và sản phẩm liên quan. /app/shop: cửa hàng hội viên, địa chỉ nhận, đặt đơn, lịch sử và đánh giá. /admin/shop: sản phẩm và xử lý đơn. /admin/product-attributes: thuộc tính, giá trị và biến thể. /admin/shop-reports: thống kê, tồn thấp và nhật ký. /staff/orders và /tra-cuu-don: thông tin đơn và phân loại.

### Đơn hàng và thanh toán
COD: CONFIRMED → PREPARING → SHIPPING → DELIVERED → COMPLETED; ghi paid_at khi nhân viên xác nhận giao/thu tiền. Thanh toán chờ có PENDING_PAYMENT; QR hết hạn sau 15 phút. Khách chỉ hủy khi đơn thỏa điều kiện dịch vụ. Trừ tồn khi đặt đơn, hoàn tồn khi hủy/hết hạn đúng một lần. Nhật ký lưu thay đổi từ thời điểm triển khai, không có lịch sử giả cho đơn cũ.

MoMo/ZaloPay cần cấu hình merchant và callback HTTPS. Callback kiểm tra chữ ký, thông tin thương nhân và số tiền; xử lý lặp có tính idempotent. Trang khách quay về không tự xác nhận đã trả tiền. Thanh toán đến sau hủy/hết hạn được ghi để đối soát, không tự hoàn tiền. Chưa kiểm thử giao dịch thực tế với merchant.

### Tối ưu bảng và quy tắc lưu trữ
Một bảng customer_product_states dùng chung yêu thích và lần xem gần nhất; UNIQUE(user_id, product_id). Ảnh sản phẩm/đánh giá là JSON được converter lưu trong VARCHAR(20000), không có bảng ảnh hay kiểu JSON gốc. Đơn lưu ảnh chụp thông tin sản phẩm và biến thể để giữ lịch sử. Biến thể ngừng bán bằng cờ active. Báo cáo tính từ dữ liệu đơn, không thêm bảng tổng hợp.

### Báo cáo doanh thu
Lọc ngày theo created_at của đơn. Chỉ cộng đơn có paid_at, loại CANCELLED/EXPIRED. Doanh thu gồm phí vận chuyển và đã trừ giảm giá; bảng sản phẩm bán chạy tính tiền dòng trước giảm giá/phí vận chuyển. Tồn thấp là ≤ 5. Chưa có tính lợi nhuận theo giá vốn.

### Kiểm thử và phạm vi đã xác minh
Kết quả có sẵn của đợt nâng cấp: ShopUpgradeTest 9/9, ShopPaymentTest 4/4; frontend build thành công. Toàn bộ backend 32/33, còn một lỗi kiểm thử bộ sinh giáo án được ghi nhận trước đó; không viết “toàn bộ kiểm thử đều đạt”. Số 43 được kiểm chứng bằng schema H2 tạm; không có thao tác migration trên database dữ liệu thật trong lần lập tài liệu này.

### Cách đưa vào báo cáo
Thay Phụ lục B bằng phần 4 đầy đủ; thay danh sách bảng mục 3.1.2 bằng phần 3; dựng lại các hình ERD từ phần 5 và tệp Mermaid. Sau khi chèn, cập nhật mục lục, số hình/bảng và các tham chiếu trang trong Word. Số trang ở phần 2 là số trang PDF gốc, sẽ thay đổi khi thêm phụ lục. Giữ nội dung thuật toán tập luyện trừ các mô tả tên trường/quan hệ sai với schema này.