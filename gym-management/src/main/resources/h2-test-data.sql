-- ============================================================
-- GYMPRO - DU LIEU FULL TEST CHO H2 CONSOLE
-- URL:      http://localhost:8080/h2-console
-- JDBC URL: jdbc:h2:file:./gymdb
-- User: sa | Password: de trong
--
-- Tai khoan 1: fulltest@gym.com / password (VIP, tang co, phong gym, 4 buoi)
-- Tai khoan 2: fulltest2@gym.com / password (FREE, giam can, tai nha, 3 buoi)
-- Quen mat khau: Full Test 1 dung 1234; Full Test 2 dung 5678
-- Co the chay lai file nay nhieu lan ma khong tao trung du lieu.
-- ============================================================

-- 1. Tai khoan test toan bo chuc nang
INSERT INTO users (id, full_name, email, password, phone, status, email_verified, created_at, role_id)
SELECT 9001, 'Full Test', 'fulltest@gym.com',
       '$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W',
       '0900001234', TRUE, TRUE, CURRENT_TIMESTAMP,
       (SELECT id FROM roles WHERE role_name = 'ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'fulltest@gym.com');

UPDATE users
SET full_name = 'Full Test', phone = '0900001234', status = TRUE, email_verified = TRUE,
    password = '$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W'
WHERE email = 'fulltest@gym.com';

-- 2. Ho so day du de vao thang cac chuc nang giao an, dinh duong, tien do
INSERT INTO user_profiles
    (id, user_id, height, weight, age, gender, bmi, body_fat_percentage, goal,
     fitness_level, available_days_per_week, preferred_session_duration,
     medical_conditions, date_of_birth)
SELECT 9001, u.id, 175.0, 72.0, 25, 'MALE', 23.51, 16.0, 'MUSCLE_GAIN',
       'INTERMEDIATE', 4, 60, 'Khong', DATE '2001-01-01'
FROM users u
WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM user_profiles p WHERE p.user_id = u.id);

-- Hồ sơ ràng buộc dùng để demo thuật toán: phòng gym, 4 ngày, không chấn thương.
UPDATE user_profiles SET height=175.0,weight=72.0,age=25,gender='MALE',bmi=23.51,
 body_fat_percentage=16.0,goal='MUSCLE_GAIN',fitness_level='INTERMEDIATE',
 available_days_per_week=4,preferred_session_duration=60,medical_conditions='Không',
 training_experience_months=18, daily_activity_level='MODERATE',
 training_location='GYM',
 available_equipment='BODYWEIGHT,DUMBBELL,BENCH,BARBELL,CABLE,MACHINE,CARDIO_MACHINE',
 preferred_training_days='1,2,4,6', injury_areas='', disliked_exercises=''
WHERE user_id=(SELECT id FROM users WHERE email='fulltest@gym.com');

-- 3. Goi VIP da thanh toan, con han 1 nam
INSERT INTO memberships
    (id, user_id, membership_type, start_date, end_date, price, is_active,
     payment_status, transaction_id, payment_method, paid_at, created_at, notes)
SELECT 9001, u.id, 'VIP', CURRENT_DATE, DATEADD('DAY', 365, CURRENT_DATE),
       99000.0, TRUE, 'PAID', 'FULLTEST-VIP-PAID', 'BANK_TRANSFER',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Goi VIP cho tai khoan Full Test'
FROM users u
WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (
      SELECT 1 FROM memberships m
      WHERE m.user_id = u.id AND m.membership_type = 'VIP' AND m.is_active = TRUE
  );

-- 4. Du lieu tien do mau de test bieu do va lich su
INSERT INTO progress_tracking
    (id, user_id, weight, height, bmi, body_fat_percentage, muscle_mass_kg,
     chest_cm, waist_cm, hip_cm, arm_cm, thigh_cm, recorded_date, recorded_at, source, notes)
SELECT 9001, u.id, 75.0, 175.0, 24.49, NULL, 31.0,
       94.0, NULL, 96.0, 34.0, 55.0, DATEADD('MONTH', -2, CURRENT_DATE),
       DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 'MANUAL', 'Moc dau thang 6'
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id = 9001);

INSERT INTO progress_tracking
    (id, user_id, weight, height, bmi, body_fat_percentage, muscle_mass_kg,
     chest_cm, waist_cm, hip_cm, arm_cm, thigh_cm, recorded_date, recorded_at, source, notes)
SELECT 9002, u.id, 72.0, 175.0, 23.51, NULL, 32.5,
       96.0, NULL, 95.0, 35.0, 56.0, CURRENT_DATE,
       CURRENT_TIMESTAMP, 'MANUAL', 'Tien do hien tai Full Test'
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id = 9002);

-- Du lieu can nang theo tuan: co tang va giam de bieu do the hien bien dong ro rang.
INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9010,u.id,74.6,175.0,24.36,DATEADD('DAY',7,DATEADD('MONTH',-2,CURRENT_DATE)),DATEADD('DAY',7,DATEADD('MONTH',-2,CURRENT_TIMESTAMP)),'MANUAL','Tuần 2 tháng 6: giảm 0.4 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9010);
INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9011,u.id,74.9,175.0,24.46,DATEADD('DAY',14,DATEADD('MONTH',-2,CURRENT_DATE)),DATEADD('DAY',14,DATEADD('MONTH',-2,CURRENT_TIMESTAMP)),'MANUAL','Tuần 3 tháng 6: tăng 0.3 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9011);
INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9012,u.id,74.2,175.0,24.23,DATEADD('DAY',21,DATEADD('MONTH',-2,CURRENT_DATE)),DATEADD('DAY',21,DATEADD('MONTH',-2,CURRENT_TIMESTAMP)),'MANUAL','Tuần 4 tháng 6: giảm 0.7 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9012);

INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9020,u.id,74.0,175.0,24.16,DATEADD('MONTH',-1,CURRENT_DATE),DATEADD('MONTH',-1,CURRENT_TIMESTAMP),'MANUAL','Tuần 1 tháng 7: giảm 0.2 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9020);
INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9021,u.id,73.5,175.0,24.00,DATEADD('DAY',7,DATEADD('MONTH',-1,CURRENT_DATE)),DATEADD('DAY',7,DATEADD('MONTH',-1,CURRENT_TIMESTAMP)),'MANUAL','Tuần 2 tháng 7: giảm 0.5 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9021);
INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9022,u.id,73.8,175.0,24.10,DATEADD('DAY',14,DATEADD('MONTH',-1,CURRENT_DATE)),DATEADD('DAY',14,DATEADD('MONTH',-1,CURRENT_TIMESTAMP)),'MANUAL','Tuần 3 tháng 7: tăng 0.3 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9022);
INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9023,u.id,72.8,175.0,23.77,DATEADD('DAY',21,DATEADD('MONTH',-1,CURRENT_DATE)),DATEADD('DAY',21,DATEADD('MONTH',-1,CURRENT_TIMESTAMP)),'MANUAL','Tuần 4 tháng 7: giảm 1 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9023);

INSERT INTO progress_tracking (id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 9030,u.id,74.0,175.0,24.16,DATEADD('DAY',-2,CURRENT_DATE),DATEADD('DAY',-2,CURRENT_TIMESTAMP),'MANUAL','Đầu tháng 8 trước khi giảm 2 kg'
FROM users u WHERE u.email='fulltest@gym.com' AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id=9030);

-- Cap nhat lai cac moc neu file da tung duoc chay truoc do.
UPDATE progress_tracking SET weight=75.0,bmi=24.49,body_fat_percentage=NULL,waist_cm=NULL,
 recorded_date=DATEADD('MONTH',-2,CURRENT_DATE),recorded_at=DATEADD('MONTH',-2,CURRENT_TIMESTAMP),notes='Moc dau thang 6'
WHERE id=9001;
UPDATE progress_tracking SET weight=72.0,bmi=23.51,body_fat_percentage=NULL,waist_cm=NULL,
 recorded_date=CURRENT_DATE,recorded_at=CURRENT_TIMESTAMP,notes='Tiến độ hiện tại Full Test'
WHERE id=9002;

-- 5. Thu cung va trang phuc da mua de test cua hang/trang bi
INSERT INTO pet_profiles
    (user_id, stage, current_streak, missed_streak, aura_tier, web_count,
     last_calculated_at, equipped_shirt, equipped_pants, equipped_hair)
SELECT u.id, 'AVERAGE', 5, 0, 'BLUE', 0, CURRENT_TIMESTAMP,
       'SHIRT_ORANGE', 'PANTS_ORANGE', 'HAIR_YELLOW'
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM pet_profiles p WHERE p.user_id = u.id);

INSERT INTO user_cosmetic_ownership (id, user_id, cosmetic_code, purchased_at)
SELECT 9001, u.id, 'SHIRT_BLUE', CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (
      SELECT 1 FROM user_cosmetic_ownership o
      WHERE o.user_id = u.id AND o.cosmetic_code = 'SHIRT_BLUE'
  );

-- Hoa don da thanh toan de test lich su giao dich
INSERT INTO invoices
    (id, user_id, membership_type, membership_id, price, status, transaction_id,
     result_message, transfer_code, created_at, expires_at, paid_at,
     regenerate_count, invoice_type)
SELECT 9001, u.id, 'VIP', m.id, 99000.0, 'PAID', 'FULLTEST-INVOICE-PAID',
       'Thanh toan test thanh cong', 'GYMPROFULLTEST', DATEADD('MINUTE', -2, CURRENT_TIMESTAMP),
       DATEADD('MINUTE', 3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, 0, 'MEMBERSHIP'
FROM users u
JOIN memberships m ON m.user_id = u.id AND m.membership_type = 'VIP' AND m.is_active = TRUE
WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM invoices WHERE transaction_id = 'FULLTEST-INVOICE-PAID');

-- Dong bo bang gia VIP 99.000 d/thang cho ca goi va lich su giao dich.
UPDATE memberships SET price = 99000.0
WHERE membership_type = 'VIP';
UPDATE memberships SET payment_method = 'BANK_TRANSFER'
WHERE membership_type = 'VIP';
UPDATE invoices SET price = 99000.0
WHERE membership_type = 'VIP' AND invoice_type = 'MEMBERSHIP';

-- 6. Bai tap test active va bai tap an de kiem tra filter, phan trang, khoi phuc
-- Bo sung mo ta tung buoc va video ky thuat cho cac bai pho bien.
UPDATE exercises SET
  description = 'Bước 1: Đặt hai tay rộng hơn vai, siết bụng và giữ người thành một đường thẳng. Bước 2: Hạ ngực xuống có kiểm soát, khuỷu tay chếch khoảng 45 độ. Bước 3: Đẩy người lên và thở ra, không võng lưng.',
  video_url = 'https://www.youtube.com/watch?v=IODxDxX7oi4'
WHERE name = 'Push Up';
UPDATE exercises SET
  description = 'Bước 1: Nằm chắc trên ghế, kéo bả vai về sau và đặt chân vững. Bước 2: Hạ thanh đòn về giữa ngực, cẳng tay gần thẳng đứng. Bước 3: Đẩy thanh lên có kiểm soát, không nhấc mông khỏi ghế.',
  video_url = 'https://www.youtube.com/watch?v=rT7DgCr-3pg'
WHERE name IN ('Bench Press', 'TEST Bench Press');
UPDATE exercises SET
  description = 'Bước 1: Đứng với thanh tạ trên giữa bàn chân, lưng trung lập. Bước 2: Siết cơ lõi, đạp chân xuống sàn và đưa hông đứng thẳng. Bước 3: Hạ tạ sát chân, không cong lưng hoặc giật tạ.',
  video_url = 'https://www.youtube.com/watch?v=op9kVnSso6Q'
WHERE name = 'Deadlift';
UPDATE exercises SET
  description = 'Bước 1: Đứng chân rộng bằng vai, mũi chân hơi hướng ra ngoài. Bước 2: Đẩy hông ra sau và hạ người, giữ gối cùng hướng mũi chân. Bước 3: Đạp cả bàn chân xuống để đứng lên, giữ ngực mở.',
  video_url = 'https://www.youtube.com/watch?v=aclHkVaku9U'
WHERE name = 'Squat';
UPDATE exercises SET
  description = 'Bước 1: Chống hai cẳng tay dưới vai, duỗi chân và siết bụng. Bước 2: Giữ đầu, lưng và hông thành một đường thẳng. Bước 3: Thở đều, dừng khi hông bắt đầu võng hoặc nâng quá cao.',
  video_url = 'https://www.youtube.com/watch?v=ASdvN_XEl_c'
WHERE name = 'Plank';

INSERT INTO exercises
    (id, name, description, muscle_group, difficulty, calories_burned,
     default_sets, default_reps, rest_seconds, muscle_gain_score,
     weight_loss_score, endurance_score, flexibility_score, maintenance_score,
     stamina_cost, is_active, uses_weight, is_assessment)
SELECT 9001, 'TEST Bench Press', 'Bai test dung ta', 'CHEST', 'MEDIUM', 12,
       4, 10, 90, 10, 4, 4, 1, 7, 20, TRUE, TRUE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE id = 9001);

INSERT INTO exercises
    (id, name, description, muscle_group, difficulty, calories_burned,
     default_sets, default_reps, rest_seconds, muscle_gain_score,
     weight_loss_score, endurance_score, flexibility_score, maintenance_score,
     stamina_cost, is_active, uses_weight, is_assessment)
SELECT 9002, 'TEST Hidden Exercise', 'Bai test chuc nang an va khoi phuc', 'CORE', 'EASY', 5,
       3, 15, 30, 3, 5, 7, 4, 6, 10, FALSE, FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM exercises WHERE id = 9002);

-- 7. Hai muoi mon an mau: dinh duong tinh theo mot khau phan va duoc API quy doi ve 100 g
-- Gia tri chi dung cho demo, co the thay doi theo nguyen lieu va cach che bien thuc te.
INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9401,'Ức gà áp chảo với rau củ',330,46.0,9.0,350.0,
       'Ức gà 180 g; bông cải xanh 80 g; cà rốt 50 g; dầu ô liu 5 g; tỏi, tiêu, muối.',
       'Bước 1: Rửa và cắt rau củ. Bước 2: Ướp ức gà với tỏi, tiêu và ít muối trong 15 phút. Bước 3: Áp chảo gà 6-7 phút mỗi mặt. Bước 4: Xào nhanh rau củ và dùng cùng gà.',
       'https://images.unsplash.com/photo-1532550907401-a500c9a57435?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,WEIGHT_LOSS,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9401 OR name='Ức gà áp chảo với rau củ');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9402,'Cá hồi nướng khoai lang',520,39.0,22.0,420.0,
       'Cá hồi 170 g; khoai lang 180 g; măng tây 60 g; dầu ô liu 5 g; chanh, tiêu, muối.',
       'Bước 1: Làm nóng lò 190°C. Bước 2: Cắt khoai và nướng 20 phút. Bước 3: Ướp cá hồi với chanh, tiêu. Bước 4: Nướng cá và măng tây thêm 12-15 phút.',
       'https://images.unsplash.com/photo-1467003909585-2f8a72700288?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9402 OR name='Cá hồi nướng khoai lang');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9403,'Yến mạch chuối sữa chua',385,17.0,8.0,360.0,
       'Yến mạch 60 g; chuối 100 g; sữa chua Hy Lạp 150 g; hạt chia 10 g; quế.',
       'Bước 1: Ngâm yến mạch với một ít nước ấm 10 phút. Bước 2: Cắt chuối. Bước 3: Trộn yến mạch, sữa chua và hạt chia. Bước 4: Thêm chuối và quế trước khi ăn.',
       'https://images.unsplash.com/photo-1517673400267-0251440c45dc?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE,ENDURANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9403 OR name='Yến mạch chuối sữa chua');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9404,'Cơm gạo lứt bò xào',565,38.0,18.0,450.0,
       'Cơm gạo lứt chín 220 g; thịt bò nạc 150 g; ớt chuông 60 g; hành tây 40 g; dầu ăn 7 g; tỏi.',
       'Bước 1: Thái bò mỏng và ướp tỏi, tiêu. Bước 2: Xào bò lửa lớn 2-3 phút. Bước 3: Thêm hành tây và ớt chuông, đảo chín tới. Bước 4: Dùng với cơm gạo lứt.',
       'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,ENDURANCE,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9404 OR name='Cơm gạo lứt bò xào');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9405,'Salad cá ngừ trứng',355,35.0,17.0,380.0,
       'Cá ngừ ngâm nước 120 g; trứng gà 2 quả; xà lách 100 g; cà chua 80 g; dưa leo 60 g; sốt sữa chua 20 g.',
       'Bước 1: Luộc trứng 9 phút rồi cắt múi. Bước 2: Để ráo cá ngừ. Bước 3: Rửa và cắt rau. Bước 4: Trộn tất cả với sốt sữa chua.',
       'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9405 OR name='Salad cá ngừ trứng');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9406,'Phở bò nạc',475,35.0,12.0,600.0,
       'Bánh phở 180 g; thịt bò nạc 120 g; nước dùng bò 300 ml; hành lá, hành tây, rau thơm; chanh.',
       'Bước 1: Đun sôi nước dùng và nêm vừa ăn. Bước 2: Chần bánh phở. Bước 3: Xếp thịt bò thái mỏng lên bánh. Bước 4: Chan nước dùng sôi và thêm rau thơm.',
       'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,ENDURANCE,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9406 OR name='Phở bò nạc');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9407,'Bún gà rau xanh',420,36.0,9.0,520.0,
       'Bún tươi 180 g; ức gà 150 g; rau xà lách 80 g; dưa leo 60 g; cà rốt 40 g; nước mắm chua ngọt 25 ml.',
       'Bước 1: Luộc hoặc áp chảo gà rồi xé sợi. Bước 2: Rửa và thái rau. Bước 3: Cho bún, rau và gà vào tô. Bước 4: Rưới lượng nước mắm vừa đủ.',
       'https://images.unsplash.com/photo-1559314809-0d155014e29e?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE,ENDURANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9407 OR name='Bún gà rau xanh');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9408,'Trứng cuộn rau củ',305,24.0,19.0,300.0,
       'Trứng gà 3 quả; lòng trắng trứng 2 quả; cà rốt 30 g; hành lá 15 g; nấm 50 g; dầu ăn 5 g.',
       'Bước 1: Thái nhỏ rau và nấm. Bước 2: Đánh trứng cùng rau. Bước 3: Đổ từng lớp mỏng lên chảo chống dính. Bước 4: Cuộn lại, làm chín và cắt khoanh.',
       'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,WEIGHT_LOSS,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9408 OR name='Trứng cuộn rau củ');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9409,'Đậu hũ sốt cà chua',340,22.0,18.0,400.0,
       'Đậu hũ cứng 250 g; cà chua 150 g; hành tím 20 g; dầu ăn 6 g; hành lá, tiêu, nước tương.',
       'Bước 1: Cắt đậu hũ và áp chảo vàng nhẹ. Bước 2: Phi hành, cho cà chua vào nấu mềm. Bước 3: Thêm đậu hũ và nước tương. Bước 4: Om 8 phút rồi thêm hành lá.',
       'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9409 OR name='Đậu hũ sốt cà chua');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9410,'Tôm xào bông cải',360,42.0,12.0,400.0,
       'Tôm bóc vỏ 200 g; bông cải xanh 150 g; ớt chuông 40 g; dầu ô liu 7 g; tỏi, tiêu, nước tương ít muối.',
       'Bước 1: Làm sạch tôm và ướp tiêu. Bước 2: Chần bông cải 2 phút. Bước 3: Phi tỏi, xào tôm đến khi đổi màu. Bước 4: Thêm rau, đảo nhanh và nêm vừa ăn.',
       'https://images.unsplash.com/photo-1603133872878-684f208fb84b?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,WEIGHT_LOSS,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9410 OR name='Tôm xào bông cải');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9411,'Sandwich ức gà nguyên cám',445,38.0,13.0,350.0,
       'Bánh mì nguyên cám 3 lát; ức gà chín 140 g; xà lách 40 g; cà chua 50 g; bơ quả 35 g; mù tạt.',
       'Bước 1: Áp chảo hoặc luộc ức gà rồi thái lát. Bước 2: Nướng nhẹ bánh mì. Bước 3: Xếp rau, gà và bơ lên bánh. Bước 4: Thêm mù tạt và kẹp bánh.',
       'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,ENDURANCE,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9411 OR name='Sandwich ức gà nguyên cám');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9412,'Sữa chua Hy Lạp trái cây',290,22.0,7.0,330.0,
       'Sữa chua Hy Lạp 220 g; dâu tây 60 g; việt quất 40 g; hạnh nhân 15 g; mật ong 8 g.',
       'Bước 1: Rửa và để ráo trái cây. Bước 2: Cho sữa chua vào bát. Bước 3: Thêm trái cây và hạnh nhân. Bước 4: Rưới một ít mật ong trước khi dùng.',
       'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE,ENDURANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9412 OR name='Sữa chua Hy Lạp trái cây');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9413,'Sinh tố chuối bơ đậu phộng',510,24.0,18.0,500.0,
       'Chuối 120 g; sữa ít béo 250 ml; yến mạch 45 g; bơ đậu phộng 25 g; bột whey 20 g; đá viên.',
       'Bước 1: Cắt chuối thành miếng. Bước 2: Cho toàn bộ nguyên liệu vào máy xay. Bước 3: Xay 45-60 giây đến khi mịn. Bước 4: Dùng ngay sau khi tập.',
       'https://images.unsplash.com/photo-1553530666-ba11a7da3888?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,ENDURANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9413 OR name='Sinh tố chuối bơ đậu phộng');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9414,'Cháo yến mạch thịt bằm',390,29.0,11.0,500.0,
       'Yến mạch 65 g; thịt heo nạc bằm 120 g; cà rốt 40 g; nấm 50 g; hành lá; tiêu; nước 450 ml.',
       'Bước 1: Xào sơ thịt bằm. Bước 2: Đun nước, thêm cà rốt và nấm. Bước 3: Cho yến mạch và thịt vào nấu 6-8 phút. Bước 4: Nêm nhẹ, thêm hành tiêu.',
       'https://images.unsplash.com/photo-1517673132405-a56a62b18caf?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE,ENDURANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9414 OR name='Cháo yến mạch thịt bằm');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9415,'Cơm gà teriyaki ít đường',575,44.0,15.0,470.0,
       'Cơm trắng chín 220 g; ức gà 180 g; bông cải 70 g; nước tương 15 ml; mật ong 8 g; gừng, tỏi; dầu 5 g.',
       'Bước 1: Trộn nước tương, mật ong, gừng và tỏi. Bước 2: Áp chảo gà chín vàng. Bước 3: Cho sốt vào đảo đến khi sệt. Bước 4: Dùng cùng cơm và bông cải hấp.',
       'https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,ENDURANCE,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9415 OR name='Cơm gà teriyaki ít đường');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9416,'Mì Ý bò bằm nguyên cám',610,39.0,18.0,480.0,
       'Mì Ý nguyên cám chín 240 g; bò nạc bằm 140 g; cà chua nghiền 120 g; hành tây 40 g; dầu ô liu 6 g; tỏi.',
       'Bước 1: Luộc mì vừa chín. Bước 2: Xào hành tỏi và bò bằm. Bước 3: Thêm cà chua, nấu sốt 10 phút. Bước 4: Trộn mì với sốt và dùng nóng.',
       'https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,ENDURANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9416 OR name='Mì Ý bò bằm nguyên cám');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9417,'Cá basa hấp gừng',315,38.0,10.0,390.0,
       'Cá basa phi lê 220 g; gừng 15 g; hành lá 20 g; nấm 60 g; nước tương ít muối 12 ml; tiêu.',
       'Bước 1: Rửa cá và thấm khô. Bước 2: Xếp cá cùng gừng và nấm vào đĩa. Bước 3: Hấp 12-15 phút. Bước 4: Thêm hành lá và nước tương trước khi dùng.',
       'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9417 OR name='Cá basa hấp gừng');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9418,'Khoai lang trứng luộc',365,21.0,12.0,390.0,
       'Khoai lang 230 g; trứng gà 2 quả; lòng trắng trứng 2 quả; dưa leo 60 g; tiêu.',
       'Bước 1: Rửa và hấp khoai 20-25 phút. Bước 2: Luộc trứng 9 phút. Bước 3: Cắt khoai, trứng và dưa leo. Bước 4: Rắc tiêu và dùng cho bữa sáng hoặc trước tập.',
       'https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,ENDURANCE,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9418 OR name='Khoai lang trứng luộc');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9419,'Súp bí đỏ ức gà',350,32.0,10.0,500.0,
       'Bí đỏ 220 g; ức gà 140 g; sữa tươi không đường 100 ml; hành tây 30 g; dầu ô liu 5 g; tiêu.',
       'Bước 1: Luộc chín gà rồi xé nhỏ. Bước 2: Hấp bí đỏ và xay với sữa. Bước 3: Phi hành, thêm hỗn hợp bí và nấu 5 phút. Bước 4: Cho gà vào, nêm nhẹ và dùng nóng.',
       'https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?auto=format&fit=crop&w=900&q=80','WEIGHT_LOSS,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9419 OR name='Súp bí đỏ ức gà');

INSERT INTO foods (id,name,calories,protein_grams,fat_grams,weight_grams,ingredients,instructions,image_url,suitable_goals,is_active)
SELECT 9420,'Cơm cuộn cá ngừ bơ',495,30.0,16.0,420.0,
       'Cơm chín 220 g; cá ngừ ngâm nước 110 g; rong biển 2 lá; bơ quả 50 g; dưa leo 50 g; mè rang 5 g.',
       'Bước 1: Để ráo cá ngừ, cắt bơ và dưa leo. Bước 2: Trải rong biển và dàn cơm mỏng. Bước 3: Xếp nhân rồi cuộn chặt. Bước 4: Cắt khoanh và rắc mè.',
       'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=900&q=80','MUSCLE_GAIN,ENDURANCE,MAINTENANCE',TRUE
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE id=9420 OR name='Cơm cuộn cá ngừ bơ');

-- 8. Hai giao an da hoan thanh trong 2 thang lien truoc (VD thang 6 va 7 neu hien tai la thang 8)
INSERT INTO workout_plans
    (id, user_id, plan_name, description, goal, target_level, duration_weeks,
     sessions_per_week, current_week, is_active, is_ai_generated, is_template,
     is_completed, week_start_date, starting_bmi, starting_weight,
     difficulty_adjustment, sets_adjustment, reps_adjustment, exercises_adjustment,
     weight_adjustment_note, max_mana, current_mana, created_at, estimated_weeks)
SELECT 9001, u.id, 'Full Test - Tang co thang 1',
       'Giao an hoan thanh trong thang cach hien tai 2 thang',
       'MUSCLE_GAIN', 'INTERMEDIATE', 4, 2, 4, FALSE, TRUE, FALSE, TRUE,
       DATEADD('MONTH', -2, CURRENT_DATE), 24.16, 74.0,
       1, 1, 1, 0, 'Da hoan thanh du 8/8 buoi', 100, 82,
       DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 4
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_plans WHERE id = 9001);

INSERT INTO workout_plans
    (id, user_id, plan_name, description, goal, target_level, duration_weeks,
     sessions_per_week, current_week, is_active, is_ai_generated, is_template,
     is_completed, week_start_date, starting_bmi, starting_weight,
     difficulty_adjustment, sets_adjustment, reps_adjustment, exercises_adjustment,
     weight_adjustment_note, max_mana, current_mana, created_at, estimated_weeks)
SELECT 9002, u.id, 'Full Test - Dot mo thang 2',
       'Giao an hoan thanh trong thang lien truoc',
       'WEIGHT_LOSS', 'INTERMEDIATE', 4, 2, 4, FALSE, TRUE, FALSE, TRUE,
       DATEADD('MONTH', -1, CURRENT_DATE), 23.84, 73.0,
       1, 0, 2, 0, 'Da hoan thanh du 8/8 buoi', 100, 88,
       DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 4
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_plans WHERE id = 9002);

-- Hai ngay tap cho moi giao an
INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9011, 9001, 1, 'Buoi 1 - Than tren'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9011);
INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9012, 9001, 4, 'Buoi 2 - Than duoi'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9012);
INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9021, 9002, 2, 'Buoi 1 - Cardio va nguc'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9021);
INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9022, 9002, 5, 'Buoi 2 - Toan than'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9022);

INSERT INTO workout_plan_exercises
    (id, plan_day_id, exercise_id, sets, reps, rest_seconds, order_index, notes,
     base_weight_kg, current_weight_kg, weight_updated_week,
     recommended_weight_kg, current_recommended_weight_kg, is_assessment)
SELECT 9051, 9011, 9001, 4, 10, 90, 1, 'Bai chinh', 30.0, 37.5, 4, 30.0, 30.0, FALSE
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id = 9051);
INSERT INTO workout_plan_exercises
    (id, plan_day_id, exercise_id, sets, reps, rest_seconds, order_index, notes,
     base_weight_kg, current_weight_kg, weight_updated_week,
     recommended_weight_kg, current_recommended_weight_kg, is_assessment)
SELECT 9052, 9012, 9001, 3, 12, 75, 1, 'Bai bo tro', 25.0, 32.5, 4, 25.0, 25.0, FALSE
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id = 9052);
INSERT INTO workout_plan_exercises
    (id, plan_day_id, exercise_id, sets, reps, rest_seconds, order_index, notes,
     base_weight_kg, current_weight_kg, weight_updated_week,
     recommended_weight_kg, current_recommended_weight_kg, is_assessment)
SELECT 9061, 9021, 9001, 3, 12, 60, 1, 'Bai chinh', 27.5, 32.5, 4, 27.5, 27.5, FALSE
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id = 9061);
INSERT INTO workout_plan_exercises
    (id, plan_day_id, exercise_id, sets, reps, rest_seconds, order_index, notes,
     base_weight_kg, current_weight_kg, weight_updated_week,
     recommended_weight_kg, current_recommended_weight_kg, is_assessment)
SELECT 9062, 9022, 9001, 4, 10, 75, 1, 'Bai toan than', 30.0, 35.0, 4, 30.0, 30.0, FALSE
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id = 9062);

-- Moi giao an co 8 buoi COMPLETED trong 4 tuan (2 buoi/tuan)
INSERT INTO workout_sessions
    (id, user_id, workout_plan_id, plan_day_id, session_date, scheduled_time,
     check_in_time, check_out_time, status, total_calories_burned,
     duration_minutes, notes, week_number, completion_rate,
     checkout_weight, checkout_body_fat, is_last_session_of_week, is_custom)
SELECT 9100 + r.X, u.id, 9001,
       CASE WHEN MOD(r.X, 2) = 1 THEN 9011 ELSE 9012 END,
       DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -2, CURRENT_DATE)),
       TIME '18:00:00',
       DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -2, CURRENT_DATE)) AS TIMESTAMP)),
       DATEADD('MINUTE', 60, DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -2, CURRENT_DATE)) AS TIMESTAMP))),
       'COMPLETED', 380 + r.X * 5, 60, 'Full Test - da checkout',
       CAST(FLOOR((r.X - 1) / 2) + 1 AS INTEGER), 85 + MOD(r.X, 4) * 5,
       74.0 - r.X * 0.12, 18.0 - r.X * 0.10, MOD(r.X, 2) = 0, FALSE
FROM users u, SYSTEM_RANGE(1, 8) r
WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions s WHERE s.id = 9100 + r.X);

INSERT INTO workout_sessions
    (id, user_id, workout_plan_id, plan_day_id, session_date, scheduled_time,
     check_in_time, check_out_time, status, total_calories_burned,
     duration_minutes, notes, week_number, completion_rate,
     checkout_weight, checkout_body_fat, is_last_session_of_week, is_custom)
SELECT 9120 + r.X, u.id, 9002,
       CASE WHEN MOD(r.X, 2) = 1 THEN 9021 ELSE 9022 END,
       DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -1, CURRENT_DATE)),
       TIME '18:30:00',
       DATEADD('MINUTE', 30, DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -1, CURRENT_DATE)) AS TIMESTAMP))),
       DATEADD('MINUTE', 58, DATEADD('MINUTE', 30, DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -1, CURRENT_DATE)) AS TIMESTAMP)))),
       'COMPLETED', 410 + r.X * 6, 58, 'Full Test - da checkout',
       CAST(FLOOR((r.X - 1) / 2) + 1 AS INTEGER), 90 + MOD(r.X, 3) * 5,
       73.0 - r.X * 0.12, 17.0 - r.X * 0.10, MOD(r.X, 2) = 0, FALSE
FROM users u, SYSTEM_RANGE(1, 8) r
WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions s WHERE s.id = 9120 + r.X);

-- Log bai tap cua 16 buoi de trang chi tiet buoi tap co du lieu
INSERT INTO session_exercise_logs
    (id, session_id, exercise_id, sets_completed, reps_completed,
     weight_used_kg, is_completed, notes, logged_at, completion_percent)
SELECT 9200 + r.X, 9100 + r.X, 9001, 4, 10,
       30.0 + r.X, TRUE, 'Hoan thanh bai tap',
       (SELECT check_out_time FROM workout_sessions WHERE id = 9100 + r.X), 100
FROM SYSTEM_RANGE(1, 8) r
WHERE NOT EXISTS (SELECT 1 FROM session_exercise_logs l WHERE l.id = 9200 + r.X);

INSERT INTO session_exercise_logs
    (id, session_id, exercise_id, sets_completed, reps_completed,
     weight_used_kg, is_completed, notes, logged_at, completion_percent)
SELECT 9220 + r.X, 9120 + r.X, 9001, 3, 12,
       27.5 + r.X * 0.5, TRUE, 'Hoan thanh bai tap',
       (SELECT check_out_time FROM workout_sessions WHERE id = 9120 + r.X), 100
FROM SYSTEM_RANGE(1, 8) r
WHERE NOT EXISTS (SELECT 1 FROM session_exercise_logs l WHERE l.id = 9220 + r.X);

-- Dat lai moc thoi gian theo cong thuc co dinh de chay lai SQL van luon ra 2 thang lien truoc.
UPDATE workout_plans
SET week_start_date = DATEADD('MONTH', -2, CURRENT_DATE),
    created_at = DATEADD('MONTH', -2, CURRENT_TIMESTAMP),
    description = 'Giao an hoan thanh trong thang cach hien tai 2 thang'
WHERE id = 9001;

UPDATE workout_plans
SET week_start_date = DATEADD('MONTH', -1, CURRENT_DATE),
    created_at = DATEADD('MONTH', -1, CURRENT_TIMESTAMP),
    description = 'Giao an hoan thanh trong thang lien truoc'
WHERE id = 9002;

UPDATE workout_sessions
SET session_date = DATEADD('DAY', FLOOR((id - 9101) / 2) * 7
        + CASE WHEN MOD(id - 9100, 2) = 1 THEN 0 ELSE 3 END,
        DATEADD('MONTH', -2, CURRENT_DATE)),
    check_in_time = DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((id - 9101) / 2) * 7
        + CASE WHEN MOD(id - 9100, 2) = 1 THEN 0 ELSE 3 END,
        DATEADD('MONTH', -2, CURRENT_DATE)) AS TIMESTAMP)),
    check_out_time = DATEADD('HOUR', 19, CAST(DATEADD('DAY', FLOOR((id - 9101) / 2) * 7
        + CASE WHEN MOD(id - 9100, 2) = 1 THEN 0 ELSE 3 END,
        DATEADD('MONTH', -2, CURRENT_DATE)) AS TIMESTAMP))
WHERE id BETWEEN 9101 AND 9108;

UPDATE workout_sessions
SET session_date = DATEADD('DAY', FLOOR((id - 9121) / 2) * 7
        + CASE WHEN MOD(id - 9120, 2) = 1 THEN 0 ELSE 3 END,
        DATEADD('MONTH', -1, CURRENT_DATE)),
    check_in_time = DATEADD('MINUTE', 30, DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((id - 9121) / 2) * 7
        + CASE WHEN MOD(id - 9120, 2) = 1 THEN 0 ELSE 3 END,
        DATEADD('MONTH', -1, CURRENT_DATE)) AS TIMESTAMP))),
    check_out_time = DATEADD('MINUTE', 28, DATEADD('HOUR', 19, CAST(DATEADD('DAY', FLOOR((id - 9121) / 2) * 7
        + CASE WHEN MOD(id - 9120, 2) = 1 THEN 0 ELSE 3 END,
        DATEADD('MONTH', -1, CURRENT_DATE)) AS TIMESTAMP)))
WHERE id BETWEEN 9121 AND 9128;

UPDATE session_exercise_logs
SET logged_at = (SELECT s.check_out_time FROM workout_sessions s WHERE s.id = session_exercise_logs.session_id)
WHERE session_id BETWEEN 9101 AND 9108 OR session_id BETWEEN 9121 AND 9128;

-- 9. Giao an dang hoat dong de Dashboard co du lieu tuan hien tai
INSERT INTO workout_plans
    (id, user_id, plan_name, description, goal, target_level, duration_weeks,
     sessions_per_week, current_week, is_active, is_ai_generated, is_template,
     is_completed, week_start_date, starting_bmi, starting_weight,
     difficulty_adjustment, sets_adjustment, reps_adjustment, exercises_adjustment,
     weight_adjustment_note, max_mana, current_mana, created_at, estimated_weeks)
SELECT 9003, u.id, 'Full Test - Giao an hien tai',
       'Giao an dang active dung de demo Dashboard va checkout',
       'MUSCLE_GAIN', 'INTERMEDIATE', 8, 4, 1, TRUE, TRUE, FALSE, FALSE,
       CAST(DATE_TRUNC('WEEK', CURRENT_DATE) AS DATE),
       23.51, 72.0, 0, 0, 0, 0, 'Giao an Full Test 4 buoi/tuan', 100, 76,
       DATE_TRUNC('WEEK', CURRENT_TIMESTAMP), 8
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_plans WHERE id = 9003);

INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9031, 9003, 1, 'Buoi 1 - Nguc va tay sau'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9031);
INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9032, 9003, 4, 'Buoi 2 - Lung va chan'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9032);
INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9033, 9003, 4, 'Buoi 3 - Chan va co loi'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9033);
INSERT INTO workout_plan_days (id, workout_plan_id, day_of_week, day_name)
SELECT 9034, 9003, 6, 'Buoi 4 - Toan than va cardio'
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_days WHERE id = 9034);

-- Chay lai SQL van dong bo dung lich 4 buoi: Thu Hai, Thu Ba, Thu Nam, Thu Bay.
UPDATE workout_plan_days SET day_of_week=1,day_name='Buoi 1 - Nguc, vai va tay sau' WHERE id=9031;
UPDATE workout_plan_days SET day_of_week=2,day_name='Buoi 2 - Lung va tay truoc' WHERE id=9032;
UPDATE workout_plan_days SET day_of_week=4,day_name='Buoi 3 - Chan va co loi' WHERE id=9033;
UPDATE workout_plan_days SET day_of_week=6,day_name='Buoi 4 - Toan than va cardio' WHERE id=9034;

INSERT INTO workout_plan_exercises
    (id, plan_day_id, exercise_id, sets, reps, duration_seconds, rest_seconds,
     order_index, notes, base_weight_kg, current_weight_kg, weight_updated_week,
     recommended_weight_kg, current_recommended_weight_kg, is_assessment)
SELECT 9071, 9031, 9001, 4, 10, 45, 90, 1, 'Bai chinh tuan hien tai',
       35.0, 35.0, 1, 35.0, 35.0, FALSE
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id = 9071);
INSERT INTO workout_plan_exercises
    (id, plan_day_id, exercise_id, sets, reps, duration_seconds, rest_seconds,
     order_index, notes, base_weight_kg, current_weight_kg, weight_updated_week,
     recommended_weight_kg, current_recommended_weight_kg, is_assessment)
SELECT 9072, 9032, 9001, 3, 12, 45, 75, 1, 'Bai chinh tuan hien tai',
       30.0, 30.0, 1, 30.0, 30.0, FALSE
WHERE NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id = 9072);

-- Bo sung bai tap da dang cho giao an Full Test hien tai.
-- Buoi 1: nguc, vai, tay. Buoi 2: lung, chan, co loi.
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9073,9031,e.id,3,15,60,2,'Vai - nang ta ngang',FALSE FROM exercises e
WHERE e.name='Lateral Raise' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9073);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9074,9031,e.id,3,15,60,3,'Tay sau - day cap',FALSE FROM exercises e
WHERE e.name='Tricep Pushdown' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9074);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9075,9032,e.id,3,6,120,2,'Lung - keo ta dat',FALSE FROM exercises e
WHERE e.name='Deadlift' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9075);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9076,9032,e.id,4,12,90,3,'Chan - squat',FALSE FROM exercises e
WHERE e.name='Squat' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9076);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment)
SELECT 9077,9032,e.id,3,60,30,4,'Co loi - plank',FALSE FROM exercises e
WHERE e.name='Plank' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9077);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9078,9031,e.id,3,15,60,4,'Nguc - hit dat',FALSE FROM exercises e
WHERE e.name='Push Up' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9078);

-- Buoi 3: chan va co loi.
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9079,9033,e.id,4,12,90,1,'Chan - dap chan',FALSE FROM exercises e
WHERE e.name='Leg Press' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9079);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9080,9033,e.id,3,12,75,2,'Chan - buoc chan',FALSE FROM exercises e
WHERE e.name='Lunge' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9080);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9081,9033,e.id,3,15,45,3,'Co loi - gap bung',FALSE FROM exercises e
WHERE e.name='Crunch' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9081);

-- Buoi 4: toan than va cardio.
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment)
SELECT 9082,9034,e.id,1,900,90,1,'Cardio 15 phut',FALSE FROM exercises e
WHERE e.name='Treadmill Run' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9082);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 9083,9034,e.id,3,12,60,2,'Than tren',FALSE FROM exercises e
WHERE e.name='Push Up' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9083);
INSERT INTO workout_plan_exercises
    (id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment)
SELECT 9084,9034,e.id,3,45,45,3,'On dinh co loi',FALSE FROM exercises e
WHERE e.name='Plank' AND NOT EXISTS (SELECT 1 FROM workout_plan_exercises WHERE id=9084);

-- Mot buoi da hoan thanh va mot buoi sap tap trong tuan hien tai
INSERT INTO workout_sessions
    (id, user_id, workout_plan_id, plan_day_id, session_date, scheduled_time,
     check_in_time, check_out_time, status, total_calories_burned,
     duration_minutes, notes, week_number, completion_rate,
     checkout_weight, checkout_body_fat, is_last_session_of_week, is_custom)
SELECT 9141, u.id, 9003, 9031,
       CAST(DATE_TRUNC('WEEK', CURRENT_DATE) AS DATE), TIME '18:00:00',
       DATEADD('HOUR', 18, DATE_TRUNC('WEEK', CURRENT_TIMESTAMP)),
       DATEADD('HOUR', 19, DATE_TRUNC('WEEK', CURRENT_TIMESTAMP)),
       'COMPLETED', 420, 60, 'Full Test - buoi tuan nay da checkout',
       1, 95, 71.8, 15.8, FALSE, FALSE
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions WHERE id = 9141);

INSERT INTO workout_sessions
    (id, user_id, workout_plan_id, plan_day_id, session_date, scheduled_time,
     status, notes, week_number, is_last_session_of_week, is_custom)
SELECT 9142, u.id, 9003, 9032,
       DATEADD('DAY', 1, CAST(DATE_TRUNC('WEEK', CURRENT_DATE) AS DATE)), TIME '18:30:00',
       'SCHEDULED', 'Full Test - buoi 2', 1, FALSE, FALSE
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions WHERE id = 9142);

INSERT INTO workout_sessions
    (id,user_id,workout_plan_id,plan_day_id,session_date,scheduled_time,
     status,notes,week_number,is_last_session_of_week,is_custom)
SELECT 9146,u.id,9003,9033,
       DATEADD('DAY',3,CAST(DATE_TRUNC('WEEK',CURRENT_DATE) AS DATE)),TIME '18:30:00',
       'SCHEDULED','Full Test - buoi 3',1,FALSE,FALSE
FROM users u WHERE u.email='fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions WHERE id=9146);

INSERT INTO workout_sessions
    (id,user_id,workout_plan_id,plan_day_id,session_date,scheduled_time,
     status,notes,week_number,is_last_session_of_week,is_custom)
SELECT 9147,u.id,9003,9034,
       DATEADD('DAY',5,CAST(DATE_TRUNC('WEEK',CURRENT_DATE) AS DATE)),TIME '08:00:00',
       'SCHEDULED','Full Test - buoi 4',1,TRUE,FALSE
FROM users u WHERE u.email='fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions WHERE id=9147);

UPDATE workout_sessions SET plan_day_id=9032,
 session_date=DATEADD('DAY',1,CAST(DATE_TRUNC('WEEK',CURRENT_DATE) AS DATE)),
 is_last_session_of_week=FALSE,notes='Full Test - buoi 2' WHERE id=9142;

INSERT INTO session_exercise_logs
    (id, session_id, exercise_id, sets_completed, reps_completed,
     duration_seconds, weight_used_kg, is_completed, notes, logged_at, completion_percent)
SELECT 9241, 9141, 9001, 4, 40, 180, 35.0, TRUE,
       'Hoan thanh 4 x 10',
       (SELECT check_out_time FROM workout_sessions WHERE id = 9141), 100
WHERE NOT EXISTS (SELECT 1 FROM session_exercise_logs WHERE id = 9241);

-- Du lieu rieng cho Dashboard phan tich thang hien tai:
-- co buoi hoan thanh tot, hoan thanh mot phan, bo buoi va nhieu nhom co.
INSERT INTO workout_sessions
    (id, user_id, workout_plan_id, plan_day_id, session_date, scheduled_time,
     check_in_time, check_out_time, status, total_calories_burned,
     duration_minutes, notes, week_number, completion_rate,
     is_last_session_of_week, is_custom)
SELECT 9143,u.id,9003,9032,DATEADD('DAY',1,CAST(DATE_TRUNC('MONTH',CURRENT_DATE) AS DATE)),TIME '18:00:00',
       DATEADD('HOUR',18,DATEADD('DAY',1,DATE_TRUNC('MONTH',CURRENT_TIMESTAMP))),
       DATEADD('MINUTE',52,DATEADD('HOUR',18,DATEADD('DAY',1,DATE_TRUNC('MONTH',CURRENT_TIMESTAMP)))),
       'COMPLETED',360,52,'Dashboard - hoan thanh mot phan',1,72,FALSE,FALSE
FROM users u WHERE u.email='fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions WHERE id=9143);

INSERT INTO workout_sessions
    (id, user_id, workout_plan_id, plan_day_id, session_date, scheduled_time,
     status, notes, week_number, completion_rate, is_last_session_of_week, is_custom)
SELECT 9144,u.id,9003,9031,CAST(DATE_TRUNC('MONTH',CURRENT_DATE) AS DATE),TIME '18:00:00',
       'SKIPPED','Dashboard - bo buoi de demo chat luong',1,0,FALSE,FALSE
FROM users u WHERE u.email='fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions WHERE id=9144);

INSERT INTO workout_sessions
    (id, user_id, workout_plan_id, plan_day_id, session_date, scheduled_time,
     check_in_time, check_out_time, status, total_calories_burned,
     duration_minutes, notes, week_number, completion_rate,
     is_last_session_of_week, is_custom)
SELECT 9145,u.id,9003,9031,DATEADD('DAY',2,CAST(DATE_TRUNC('MONTH',CURRENT_DATE) AS DATE)),TIME '06:30:00',
       DATEADD('MINUTE',390,DATE_TRUNC('MONTH',CURRENT_TIMESTAMP)),
       DATEADD('MINUTE',445,DATE_TRUNC('MONTH',CURRENT_TIMESTAMP)),
       'COMPLETED',440,55,'Dashboard - hoan thanh tot',1,100,FALSE,FALSE
FROM users u WHERE u.email='fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_sessions WHERE id=9145);

INSERT INTO session_exercise_logs
    (id,session_id,exercise_id,sets_completed,reps_completed,weight_used_kg,
     is_completed,notes,logged_at,completion_percent)
SELECT 9242,9143,e.id,3,30,25.0,TRUE,'Bai lung da hoan thanh',
       (SELECT check_out_time FROM workout_sessions WHERE id=9143),75
FROM exercises e WHERE e.name='Deadlift'
  AND NOT EXISTS (SELECT 1 FROM session_exercise_logs WHERE id=9242);
INSERT INTO session_exercise_logs
    (id,session_id,exercise_id,sets_completed,reps_completed,weight_used_kg,
     is_completed,notes,logged_at,completion_percent)
SELECT 9243,9143,e.id,3,30,20.0,TRUE,'Bai chan da hoan thanh',
       (SELECT check_out_time FROM workout_sessions WHERE id=9143),70
FROM exercises e WHERE e.name='Squat'
  AND NOT EXISTS (SELECT 1 FROM session_exercise_logs WHERE id=9243);
INSERT INTO session_exercise_logs
    (id,session_id,exercise_id,sets_completed,duration_seconds,weight_used_kg,
     is_completed,notes,logged_at,completion_percent)
SELECT 9244,9145,e.id,3,180,NULL,TRUE,'Bai co loi da hoan thanh',
       (SELECT check_out_time FROM workout_sessions WHERE id=9145),100
FROM exercises e WHERE e.name='Plank'
  AND NOT EXISTS (SELECT 1 FROM session_exercise_logs WHERE id=9244);
INSERT INTO session_exercise_logs
    (id,session_id,exercise_id,sets_completed,reps_completed,weight_used_kg,
     is_completed,notes,logged_at,completion_percent)
SELECT 9245,9145,e.id,3,45,NULL,TRUE,'Bai nguc da hoan thanh',
       (SELECT check_out_time FROM workout_sessions WHERE id=9145),100
FROM exercises e WHERE e.name='Push Up'
  AND NOT EXISTS (SELECT 1 FROM session_exercise_logs WHERE id=9245);
INSERT INTO session_exercise_logs
    (id,session_id,exercise_id,sets_completed,reps_completed,weight_used_kg,
     is_completed,notes,logged_at,completion_percent)
SELECT 9246,9145,e.id,3,45,7.5,TRUE,'Bai vai da hoan thanh',
       (SELECT check_out_time FROM workout_sessions WHERE id=9145),100
FROM exercises e WHERE e.name='Lateral Raise'
  AND NOT EXISTS (SELECT 1 FROM session_exercise_logs WHERE id=9246);
INSERT INTO session_exercise_logs
    (id,session_id,exercise_id,sets_completed,reps_completed,weight_used_kg,
     is_completed,notes,logged_at,completion_percent)
SELECT 9247,9145,e.id,3,45,15.0,TRUE,'Bai tay da hoan thanh',
       (SELECT check_out_time FROM workout_sessions WHERE id=9145),100
FROM exercises e WHERE e.name='Tricep Pushdown'
  AND NOT EXISTS (SELECT 1 FROM session_exercise_logs WHERE id=9247);

-- Khi chay lai file, luon dua bo demo Dashboard ve dung thang hien tai.
UPDATE workout_sessions SET
 session_date=DATEADD('DAY',1,CAST(DATE_TRUNC('MONTH',CURRENT_DATE) AS DATE)),status='COMPLETED',
 total_calories_burned=360,duration_minutes=52,completion_rate=72
WHERE id=9143;
UPDATE workout_sessions SET
 session_date=CAST(DATE_TRUNC('MONTH',CURRENT_DATE) AS DATE),status='SKIPPED',completion_rate=0
WHERE id=9144;
UPDATE workout_sessions SET
 session_date=DATEADD('DAY',2,CAST(DATE_TRUNC('MONTH',CURRENT_DATE) AS DATE)),status='COMPLETED',
 total_calories_burned=440,duration_minutes=55,completion_rate=100
WHERE id=9145;

-- 10. Muoi tai khoan cong dong de demo danh gia dich vu
-- Tat ca dang nhap bang mat khau: password
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9601,'Minh Anh','reviewer01@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000001',TRUE,TRUE,DATEADD('DAY',-40,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer01@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9602,'Quốc Bảo','reviewer02@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000002',TRUE,TRUE,DATEADD('DAY',-36,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer02@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9603,'Hoàng Duy','reviewer03@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000003',TRUE,TRUE,DATEADD('DAY',-33,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer03@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9604,'Thu Hà','reviewer04@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000004',TRUE,TRUE,DATEADD('DAY',-29,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer04@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9605,'Gia Huy','reviewer05@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000005',TRUE,TRUE,DATEADD('DAY',-25,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer05@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9606,'Ngọc Lan','reviewer06@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000006',TRUE,TRUE,DATEADD('DAY',-21,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer06@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9607,'Đức Long','reviewer07@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000007',TRUE,TRUE,DATEADD('DAY',-18,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer07@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9608,'Phương Linh','reviewer08@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000008',TRUE,TRUE,DATEADD('DAY',-14,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer08@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9609,'Tuấn Kiệt','reviewer09@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000009',TRUE,TRUE,DATEADD('DAY',-10,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer09@gym.com');
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 9610,'Khánh Vy','reviewer10@gym.com','$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W','0911000010',TRUE,TRUE,DATEADD('DAY',-7,CURRENT_TIMESTAMP),(SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='reviewer10@gym.com');

-- 10 danh gia cong khai, chi thuoc hai noi dung phu hop voi web: Giao an va Dinh duong
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at,admin_reply,replied_at)
SELECT 9601,u.id,5,'Giáo án rất sát mục tiêu','Các buổi được chia hợp lý, mức tạ đề xuất vừa sức và dễ theo dõi.','WORKOUT_PLAN',TRUE,DATEADD('DAY',-30,CURRENT_TIMESTAMP),'Cảm ơn bạn đã chia sẻ trải nghiệm!',DATEADD('DAY',-29,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer01@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9601);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at)
SELECT 9602,u.id,4,'Dễ tập theo từng tuần','Phần checkout và thống kê giúp tôi biết buổi nào chưa đạt yêu cầu.','WORKOUT_PLAN',TRUE,DATEADD('DAY',-27,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer02@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9602);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at,admin_reply,replied_at)
SELECT 9603,u.id,3,'Cần thêm lựa chọn bài tập','Giáo án ổn nhưng tôi muốn có thêm nhiều bài thay thế cho nhóm chân.','WORKOUT_PLAN',TRUE,DATEADD('DAY',-24,CURRENT_TIMESTAMP),'Admin đã ghi nhận góp ý và sẽ bổ sung thư viện bài tập.',DATEADD('DAY',-23,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer03@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9603);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at)
SELECT 9604,u.id,5,'Món ăn rõ ràng và dễ nấu','Có định lượng khẩu phần, calories, protein và các bước chế biến cụ thể.','NUTRITION',TRUE,DATEADD('DAY',-21,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer04@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9604);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at)
SELECT 9605,u.id,4,'Quy đổi khẩu phần tiện lợi','Thay đổi số gram là dinh dưỡng được tính lại ngay, rất dễ so sánh.','NUTRITION',TRUE,DATEADD('DAY',-18,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer05@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9605);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at,admin_reply,replied_at)
SELECT 9606,u.id,2,'Một số món chưa hợp khẩu vị','Tôi muốn có thêm bộ lọc món chay và món Việt ít dầu.','NUTRITION',TRUE,DATEADD('DAY',-15,CURRENT_TIMESTAMP),'Cảm ơn góp ý. Bộ lọc món ăn sẽ được xem xét trong bản tiếp theo.',DATEADD('DAY',-14,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer06@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9606);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at)
SELECT 9607,u.id,5,'Điều chỉnh tuần rất hữu ích','Sau khi hoàn thành tuần, giáo án VIP điều chỉnh vừa đủ và không tăng quá nhanh.','WORKOUT_PLAN',TRUE,DATEADD('DAY',-12,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer07@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9607);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at)
SELECT 9608,u.id,4,'Theo dõi tiến độ trực quan','Biểu đồ theo tháng giúp tôi nhìn rõ cân nặng tăng giảm qua từng tuần.','WORKOUT_PLAN',TRUE,DATEADD('DAY',-9,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer08@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9608);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at)
SELECT 9609,u.id,3,'Dinh dưỡng đủ dùng','Thông tin khá đầy đủ, nên bổ sung thêm món ăn sáng nhanh.','NUTRITION',TRUE,DATEADD('DAY',-6,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer09@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9609);
INSERT INTO service_ratings (id,user_id,rating,title,comment,service_type,is_public,created_at,admin_reply,replied_at)
SELECT 9610,u.id,5,'Trải nghiệm cá nhân hóa tốt','Lịch tập, mục tiêu và các bài trong giáo án phù hợp với thời gian rảnh của tôi.','WORKOUT_PLAN',TRUE,DATEADD('DAY',-3,CURRENT_TIMESTAMP),'Rất vui vì giáo án phù hợp với bạn. Chúc bạn tập luyện hiệu quả!',DATEADD('DAY',-2,CURRENT_TIMESTAMP)
FROM users u WHERE u.email='reviewer10@gym.com' AND NOT EXISTS (SELECT 1 FROM service_ratings WHERE id=9610);

-- 11. Giao an mau do Admin tao. User chon truc tiep, khong can qua buoc chon muc tieu.
INSERT INTO workout_plans
 (id,user_id,plan_name,description,goal,target_level,duration_weeks,sessions_per_week,current_week,
  is_active,is_ai_generated,is_template,is_completed,is_fitness_improvement,created_at)
SELECT 9701,NULL,'Toàn thân cơ bản 2 buổi','Giáo án Admin cho người mới: làm quen động tác toàn thân, cường độ vừa và dễ duy trì.','MAINTENANCE','BEGINNER',6,2,1,TRUE,FALSE,TRUE,FALSE,FALSE,CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM workout_plans WHERE id=9701);
INSERT INTO workout_plans
 (id,user_id,plan_name,description,goal,target_level,duration_weeks,sessions_per_week,current_week,
  is_active,is_ai_generated,is_template,is_completed,is_fitness_improvement,created_at)
SELECT 9702,NULL,'Đốt mỡ 3 buổi','Giáo án Admin kết hợp toàn thân và tim mạch, phù hợp người muốn tăng vận động và tiêu hao năng lượng.','WEIGHT_LOSS','INTERMEDIATE',8,3,1,TRUE,FALSE,TRUE,FALSE,FALSE,DATEADD('MINUTE',1,CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM workout_plans WHERE id=9702);
INSERT INTO workout_plans
 (id,user_id,plan_name,description,goal,target_level,duration_weeks,sessions_per_week,current_week,
  is_active,is_ai_generated,is_template,is_completed,is_fitness_improvement,created_at)
SELECT 9703,NULL,'Tăng cơ nền tảng 3 buổi','Giáo án Admin chia nhóm cơ Ngực - Lưng - Chân, ưu tiên kỹ thuật và tăng tải có kiểm soát.','MUSCLE_GAIN','INTERMEDIATE',8,3,1,TRUE,FALSE,TRUE,FALSE,FALSE,DATEADD('MINUTE',2,CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM workout_plans WHERE id=9703);
INSERT INTO workout_plans
 (id,user_id,plan_name,description,goal,target_level,duration_weeks,sessions_per_week,current_week,
  is_active,is_ai_generated,is_template,is_completed,is_fitness_improvement,created_at)
SELECT 9704,NULL,'Khởi động cho thể lực yếu','Không cần chọn mục tiêu. Giáo án Admin dành cho người thể lực yếu hoặc chưa đủ điều kiện tạo giáo án mục tiêu: bài nhẹ, ít hiệp và nghỉ dài.','MAINTENANCE','BEGINNER',4,2,1,TRUE,FALSE,TRUE,FALSE,FALSE,DATEADD('MINUTE',3,CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM workout_plans WHERE id=9704);

INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9711,9701,2,'Buổi 1 - Toàn thân nhẹ' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9711);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9712,9701,5,'Buổi 2 - Chân và cơ lõi' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9712);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9713,9702,2,'Buổi 1 - Toàn thân' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9713);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9714,9702,4,'Buổi 2 - Tim mạch' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9714);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9715,9702,6,'Buổi 3 - Chân và bụng' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9715);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9716,9703,2,'Buổi 1 - Ngực và tay' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9716);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9717,9703,4,'Buổi 2 - Lưng và vai' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9717);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9718,9703,6,'Buổi 3 - Chân và cơ lõi' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9718);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9719,9704,3,'Buổi 1 - Làm quen vận động' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9719);
INSERT INTO workout_plan_days (id,workout_plan_id,day_of_week,day_name) SELECT 9720,9704,6,'Buổi 2 - Thăng bằng và cơ lõi' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=9720);

INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9751,9711,e.id,2,10,90,1,'Thực hiện chậm, ưu tiên đúng kỹ thuật',FALSE FROM exercises e WHERE e.name='Push Up' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9751);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9752,9711,e.id,2,10,90,2,'Không cần dùng tạ nặng',FALSE FROM exercises e WHERE e.name='Squat' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9752);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9753,9712,e.id,2,10,90,1,'Bước ngắn và giữ thăng bằng',FALSE FROM exercises e WHERE e.name='Lunge' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9753);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment) SELECT 9754,9712,e.id,2,30,60,2,'Giữ lưng thẳng',FALSE FROM exercises e WHERE e.name='Plank' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9754);

INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9755,9713,e.id,3,12,60,1,'Giữ nhịp đều',FALSE FROM exercises e WHERE e.name='Burpee' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9755);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9756,9713,e.id,3,15,60,2,'Không võng lưng',FALSE FROM exercises e WHERE e.name='Push Up' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9756);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment) SELECT 9757,9714,e.id,1,900,120,1,'Cường độ vừa, vẫn nói chuyện được',FALSE FROM exercises e WHERE e.name='Treadmill Run' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9757);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9758,9715,e.id,3,15,60,1,'Kiểm soát đầu gối',FALSE FROM exercises e WHERE e.name='Squat' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9758);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9759,9715,e.id,3,20,45,2,'Thở ra khi gập bụng',FALSE FROM exercises e WHERE e.name='Crunch' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9759);

INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9760,9716,e.id,4,10,90,1,'Tăng tạ từ từ',FALSE FROM exercises e WHERE e.name='Bench Press' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9760);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9761,9716,e.id,3,12,60,2,'Giữ khuỷu tay ổn định',FALSE FROM exercises e WHERE e.name='Tricep Pushdown' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9761);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9762,9717,e.id,3,12,75,1,'Kéo bằng cơ lưng',FALSE FROM exercises e WHERE e.name='Lat Pulldown' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9762);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9763,9717,e.id,3,15,60,2,'Không nhún người',FALSE FROM exercises e WHERE e.name='Lateral Raise' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9763);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9764,9718,e.id,4,12,90,1,'Giữ lưng trung lập',FALSE FROM exercises e WHERE e.name='Squat' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9764);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment) SELECT 9765,9718,e.id,3,45,45,2,'Siết cơ bụng',FALSE FROM exercises e WHERE e.name='Plank' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9765);

INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9766,9719,e.id,1,6,120,1,'Có thể chống tay lên ghế hoặc tường',FALSE FROM exercises e WHERE e.name='Push Up' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9766);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9767,9719,e.id,1,8,120,2,'Chỉ hạ sâu trong phạm vi thoải mái',FALSE FROM exercises e WHERE e.name='Squat' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9767);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment) SELECT 9768,9720,e.id,1,6,120,1,'Bám điểm tựa nếu cần',FALSE FROM exercises e WHERE e.name='Lunge' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9768);
INSERT INTO workout_plan_exercises (id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment) SELECT 9769,9720,e.id,1,15,120,2,'Dừng ngay nếu đau hoặc chóng mặt',FALSE FROM exercises e WHERE e.name='Plank' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=9769);

-- 12. Tai khoan doi chung Full Test 2
-- Khac Full Test 1: FREE, nu, giam can, BEGINNER, 3 buoi/tuan, dang o tuan 1.
INSERT INTO users (id,full_name,email,password,phone,status,email_verified,created_at,role_id)
SELECT 20001,'Full Test 2','fulltest2@gym.com',
       '$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W',
       '0911115678',TRUE,TRUE,DATEADD('DAY',-45,CURRENT_TIMESTAMP),
       (SELECT id FROM roles WHERE role_name='ROLE_USER')
WHERE NOT EXISTS(SELECT 1 FROM users WHERE email='fulltest2@gym.com');

UPDATE users SET full_name='Full Test 2',phone='0911115678',status=TRUE,email_verified=TRUE,
 password='$2a$10$YGwKIlx5.AEpPvDkj6QlkO1kR6MWAmMti0vlD5Dbeeznfvll5.d8W'
WHERE email='fulltest2@gym.com';

INSERT INTO user_profiles
 (id,user_id,height,weight,age,gender,bmi,body_fat_percentage,goal,fitness_level,
  available_days_per_week,preferred_session_duration,medical_conditions,date_of_birth)
SELECT 20001,u.id,160.0,68.0,30,'FEMALE',26.56,NULL,'WEIGHT_LOSS','BEGINNER',
       3,45,'Đau đầu gối nhẹ, ưu tiên bài tác động thấp',DATE '1996-04-12'
FROM users u WHERE u.email='fulltest2@gym.com'
 AND NOT EXISTS(SELECT 1 FROM user_profiles p WHERE p.user_id=u.id);

-- Hồ sơ đối chứng: tập tại nhà, ít thiết bị và đau đầu gối nên bài chân tác động mạnh bị loại.
UPDATE user_profiles SET training_experience_months=2, daily_activity_level='SEDENTARY',
 training_location='HOME', available_equipment='BODYWEIGHT,MAT,RESISTANCE_BAND,DUMBBELL',
 preferred_training_days='2,4,7', injury_areas='KNEE', disliked_exercises='Burpee'
WHERE user_id=(SELECT id FROM users WHERE email='fulltest2@gym.com');

INSERT INTO memberships
 (id,user_id,membership_type,start_date,end_date,price,is_active,payment_status,
  payment_method,paid_at,created_at,notes)
SELECT 20001,u.id,'FREE',DATEADD('DAY',-45,CURRENT_DATE),DATEADD('YEAR',100,CURRENT_DATE),
       0.0,TRUE,'PAID','SYSTEM',CURRENT_TIMESTAMP,DATEADD('DAY',-45,CURRENT_TIMESTAMP),
       'Gói thường để so sánh với Full Test VIP'
FROM users u WHERE u.email='fulltest2@gym.com'
 AND NOT EXISTS(SELECT 1 FROM memberships m WHERE m.user_id=u.id AND m.is_active=TRUE);

-- Tiến độ cân nặng khác Full Test 1: giảm chậm, có một tuần tăng nhẹ.
INSERT INTO progress_tracking(id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 20501,u.id,70.0,160.0,27.34,DATEADD('DAY',-28,CURRENT_DATE),DATEADD('DAY',-28,CURRENT_TIMESTAMP),'MANUAL','Mốc bắt đầu Full Test 2'
FROM users u WHERE u.email='fulltest2@gym.com' AND NOT EXISTS(SELECT 1 FROM progress_tracking WHERE id=20501);
INSERT INTO progress_tracking(id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 20502,u.id,69.2,160.0,27.03,DATEADD('DAY',-21,CURRENT_DATE),DATEADD('DAY',-21,CURRENT_TIMESTAMP),'MANUAL','Tuần 2 giảm 0.8 kg'
FROM users u WHERE u.email='fulltest2@gym.com' AND NOT EXISTS(SELECT 1 FROM progress_tracking WHERE id=20502);
INSERT INTO progress_tracking(id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 20503,u.id,69.5,160.0,27.15,DATEADD('DAY',-14,CURRENT_DATE),DATEADD('DAY',-14,CURRENT_TIMESTAMP),'MANUAL','Tuần 3 tăng nhẹ 0.3 kg'
FROM users u WHERE u.email='fulltest2@gym.com' AND NOT EXISTS(SELECT 1 FROM progress_tracking WHERE id=20503);
INSERT INTO progress_tracking(id,user_id,weight,height,bmi,recorded_date,recorded_at,source,notes)
SELECT 20504,u.id,68.0,160.0,26.56,CURRENT_DATE,CURRENT_TIMESTAMP,'MANUAL','Hiện tại giảm tổng 2 kg'
FROM users u WHERE u.email='fulltest2@gym.com' AND NOT EXISTS(SELECT 1 FROM progress_tracking WHERE id=20504);

INSERT INTO workout_plans
 (id,user_id,plan_name,description,goal,target_level,duration_weeks,sessions_per_week,current_week,
  is_active,is_ai_generated,is_template,is_completed,week_start_date,starting_bmi,starting_weight,
  difficulty_adjustment,sets_adjustment,reps_adjustment,exercises_adjustment,weight_adjustment_note,
  max_mana,current_mana,fitness_score,fitness_level,body_type,created_at,estimated_weeks)
SELECT 20001,u.id,'Full Test 2 - Giảm cân cho người mới',
       'Giáo án đối chứng: cường độ nhẹ, cardio tác động thấp và 3 buổi mỗi tuần.',
       'WEIGHT_LOSS','BEGINNER',8,3,1,TRUE,TRUE,FALSE,FALSE,
       CAST(DATE_TRUNC('WEEK',CURRENT_DATE) AS DATE),27.34,70.0,
       -1,-1,0,0,'Gói thường: giữ nguyên mức bài sau mỗi tuần',132,92,66,'GOOD','THUA_CAN',
       DATEADD('DAY',-5,CURRENT_TIMESTAMP),8
FROM users u WHERE u.email='fulltest2@gym.com'
 AND NOT EXISTS(SELECT 1 FROM workout_plans WHERE id=20001);

UPDATE workout_plans SET is_active=FALSE
WHERE user_id=(SELECT id FROM users WHERE email='fulltest2@gym.com') AND id<>20001;
UPDATE workout_plans SET is_active=TRUE,sessions_per_week=3,current_week=1,
 max_mana=132,current_mana=92,fitness_score=66,fitness_level='GOOD',body_type='THUA_CAN'
WHERE id=20001;

INSERT INTO workout_plan_days(id,workout_plan_id,day_of_week,day_name)
SELECT 20101,20001,2,'Buổi 1 - Cardio nhẹ và toàn thân' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=20101);
INSERT INTO workout_plan_days(id,workout_plan_id,day_of_week,day_name)
SELECT 20102,20001,4,'Buổi 2 - Chân và cơ lõi' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=20102);
INSERT INTO workout_plan_days(id,workout_plan_id,day_of_week,day_name)
SELECT 20103,20001,7,'Buổi 3 - Toàn thân đốt mỡ' WHERE NOT EXISTS(SELECT 1 FROM workout_plan_days WHERE id=20103);
UPDATE workout_plan_days SET day_of_week=7,day_name='Buổi 3 - Toàn thân đốt mỡ' WHERE id=20103;
UPDATE workout_plan_days SET day_name='Buổi 1 - Circuit thân trên và cơ lõi' WHERE id=20101;
UPDATE workout_plan_days SET day_name='Buổi 2 - Vai và cơ lõi' WHERE id=20102;
UPDATE workout_plan_days SET day_name='Buổi 3 - Circuit thân trên đốt mỡ' WHERE id=20103;

INSERT INTO workout_plan_exercises(id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment)
SELECT 20201,20101,e.id,3,15,45,1,'Circuit cơ lõi tại nhà, nghỉ ngắn để tăng tiêu hao',FALSE FROM exercises e WHERE e.name='Crunch' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=20201);
INSERT INTO workout_plan_exercises(id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 20202,20101,e.id,2,8,90,2,'Có thể chống tay lên ghế',FALSE FROM exercises e WHERE e.name='Push Up' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=20202);
INSERT INTO workout_plan_exercises(id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 20203,20102,e.id,2,12,60,1,'Dùng tạ đơn nhẹ, không gây tải lên đầu gối',FALSE FROM exercises e WHERE e.name='Lateral Raise' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=20203);
INSERT INTO workout_plan_exercises(id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 20204,20102,e.id,2,12,60,2,'Siết cơ bụng và thở đều',FALSE FROM exercises e WHERE e.name='Crunch' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=20204);
INSERT INTO workout_plan_exercises(id,plan_day_id,exercise_id,sets,reps,rest_seconds,order_index,notes,is_assessment)
SELECT 20205,20103,e.id,3,10,45,1,'Chống tay cao nếu cần, thực hiện theo circuit',FALSE FROM exercises e WHERE e.name='Push Up' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=20205);
INSERT INTO workout_plan_exercises(id,plan_day_id,exercise_id,sets,duration_seconds,rest_seconds,order_index,notes,is_assessment)
SELECT 20206,20103,e.id,2,25,60,2,'Giữ lưng trung lập',FALSE FROM exercises e WHERE e.name='Plank' AND NOT EXISTS(SELECT 1 FROM workout_plan_exercises WHERE id=20206);

-- Đồng bộ cả khi file đã từng chạy với phiên bản cũ: tuyệt đối tránh bài cần máy
-- hoặc gây tải đầu gối trong giáo án tại nhà của Full Test 2.
UPDATE workout_plan_exercises SET exercise_id=(SELECT id FROM exercises WHERE name='Crunch'),sets=3,reps=15,duration_seconds=NULL,rest_seconds=45,
 notes='Circuit cơ lõi tại nhà, nghỉ ngắn để tăng tiêu hao' WHERE id=20201;
UPDATE workout_plan_exercises SET exercise_id=(SELECT id FROM exercises WHERE name='Lateral Raise'),sets=2,reps=12,rest_seconds=60,
 notes='Dùng tạ đơn nhẹ, không gây tải lên đầu gối' WHERE id=20203;
UPDATE workout_plan_exercises SET exercise_id=(SELECT id FROM exercises WHERE name='Push Up'),sets=3,reps=10,rest_seconds=45,
 notes='Chống tay cao nếu cần, thực hiện theo circuit' WHERE id=20205;

-- Một buổi hoàn thành 78%, một buổi bỏ qua và một buổi sắp tập để thấy khác biệt thống kê.
INSERT INTO workout_sessions
 (id,user_id,workout_plan_id,plan_day_id,session_date,scheduled_time,check_in_time,check_out_time,
  status,total_calories_burned,duration_minutes,notes,week_number,completion_rate,is_last_session_of_week,is_custom)
SELECT 20301,u.id,20001,20101,CAST(DATE_TRUNC('WEEK',CURRENT_DATE) AS DATE),TIME '18:00:00',
 DATEADD('HOUR',18,DATE_TRUNC('WEEK',CURRENT_TIMESTAMP)),DATEADD('MINUTE',38,DATEADD('HOUR',18,DATE_TRUNC('WEEK',CURRENT_TIMESTAMP))),
 'COMPLETED',245,38,'Full Test 2 hoàn thành một phần',1,78,FALSE,FALSE
FROM users u WHERE u.email='fulltest2@gym.com' AND NOT EXISTS(SELECT 1 FROM workout_sessions WHERE id=20301);
INSERT INTO workout_sessions(id,user_id,workout_plan_id,plan_day_id,session_date,scheduled_time,status,notes,week_number,completion_rate,is_last_session_of_week,is_custom)
SELECT 20302,u.id,20001,20102,DATEADD('DAY',2,CAST(DATE_TRUNC('WEEK',CURRENT_DATE) AS DATE)),TIME '18:00:00',
 'SKIPPED','Bỏ buổi do đau đầu gối',1,0,FALSE,FALSE
FROM users u WHERE u.email='fulltest2@gym.com' AND NOT EXISTS(SELECT 1 FROM workout_sessions WHERE id=20302);
INSERT INTO workout_sessions(id,user_id,workout_plan_id,plan_day_id,session_date,scheduled_time,status,notes,week_number,is_last_session_of_week,is_custom)
SELECT 20303,u.id,20001,20103,DATEADD('DAY',6,CAST(DATE_TRUNC('WEEK',CURRENT_DATE) AS DATE)),TIME '08:00:00',
 'SCHEDULED','Buổi cuối tuần đang chờ tập',1,TRUE,FALSE
FROM users u WHERE u.email='fulltest2@gym.com' AND NOT EXISTS(SELECT 1 FROM workout_sessions WHERE id=20303);

INSERT INTO session_exercise_logs(id,session_id,exercise_id,sets_completed,duration_seconds,is_completed,notes,logged_at,completion_percent)
SELECT 20401,20301,e.id,1,600,TRUE,'Hoàn thành 10/12 phút cardio',
 (SELECT check_out_time FROM workout_sessions WHERE id=20301),83
FROM exercises e WHERE e.name='Crunch' AND NOT EXISTS(SELECT 1 FROM session_exercise_logs WHERE id=20401);
INSERT INTO session_exercise_logs(id,session_id,exercise_id,sets_completed,reps_completed,is_completed,notes,logged_at,completion_percent)
SELECT 20402,20301,e.id,2,12,TRUE,'Hoàn thành 12/16 lần',
 (SELECT check_out_time FROM workout_sessions WHERE id=20301),75
FROM exercises e WHERE e.name='Push Up' AND NOT EXISTS(SELECT 1 FROM session_exercise_logs WHERE id=20402);
UPDATE session_exercise_logs SET exercise_id=(SELECT id FROM exercises WHERE name='Crunch'),duration_seconds=NULL,
 reps_completed=38,notes='Hoàn thành 38/45 lần Crunch',completion_percent=84 WHERE id=20401;

-- Bản ghi bài tập 9001 từng được dùng để test sửa tên trên admin; chuẩn hóa lại
-- để giáo án demo hiển thị tên có nghĩa nhưng không đụng bản Bench Press đã ẩn (id 2).
UPDATE exercises SET name='Dumbbell Bench Press' WHERE id=9001 AND name='TEST Bench Press';

-- 13. Dong bo IDENTITY sau khi chen ID test thu cong.
-- Khong dat lai ve 9xxx: khi chay lai SQL, cac ID do co the da duoc Hibernate
-- su dung va se gay loi PRIMARY KEY luc bam "Bat dau tap". Vung 10000 tro len
-- duoc danh rieng cho du lieu phat sinh tu ung dung.
ALTER TABLE users ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE user_profiles ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE memberships ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE progress_tracking ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE exercises ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE foods ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE user_cosmetic_ownership ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE invoices ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE workout_plans ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE workout_plan_days ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE workout_plan_exercises ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE workout_sessions ALTER COLUMN id RESTART WITH 1000000;
ALTER TABLE session_exercise_logs ALTER COLUMN id RESTART WITH 1000000;

-- Dam bao du lieu Dashboard luon thuoc dung tai khoan Full Test, ke ca khi tai khoan
-- da ton tai truoc va co ID khac 9001.
UPDATE workout_plans SET user_id=(SELECT id FROM users WHERE email='fulltest@gym.com')
WHERE id IN (9001,9002,9003);

-- Đồng bộ giáo án đang tập khi chạy lại file SQL. Các lệnh INSERT phía trên dùng
-- NOT EXISTS nên bản ghi 9003 cũ từng có max_mana/current_mana = 0 sẽ không tự sửa.
-- Chỉ giữ một giáo án active cho Full Test để API không lấy nhầm giáo án cũ 0/0.
UPDATE workout_plans
SET is_active = FALSE
WHERE user_id = (SELECT id FROM users WHERE email='fulltest@gym.com')
  AND id <> 9003;

UPDATE workout_plans
SET is_active = TRUE,
    is_completed = FALSE,
    sessions_per_week = 4,
    max_mana = 100,
    current_mana = 76,
    fitness_score = 50,
    fitness_level = 'AVERAGE',
    body_type = 'CAN_DOI',
    last_mana_regen_date = CURRENT_DATE,
    weight_adjustment_note = 'Giáo án Full Test 4 buổi/tuần'
WHERE id = 9003
  AND user_id = (SELECT id FROM users WHERE email='fulltest@gym.com');

UPDATE workout_sessions SET user_id=(SELECT id FROM users WHERE email='fulltest@gym.com')
WHERE id BETWEEN 9101 AND 9145;
UPDATE progress_tracking SET user_id=(SELECT id FROM users WHERE email='fulltest@gym.com')
WHERE id IN (9001,9002,9010,9011,9012,9020,9021,9022,9023,9030);
ALTER TABLE service_ratings ALTER COLUMN id RESTART WITH 1000000;

-- CỬA HÀNG GYMPRO: sản phẩm mẫu thuộc đủ 3 nhóm nghiệp vụ
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,sale_price,stock,active,suitable_goals,created_at)
SELECT 30001,'Whey Protein 1 kg','SUPPLEMENT','Bổ sung protein thuận tiện sau buổi tập.','GYMPRO Nutrition','https://images.unsplash.com/photo-1593095948071-474c5cc2989d?auto=format&fit=crop&w=800&q=80',799000,749000,25,TRUE,'MUSCLE_GAIN,MAINTENANCE',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30001);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,created_at)
SELECT 30002,'Creatine Monohydrate 300 g','SUPPLEMENT','Creatine nguyên chất dùng theo hướng dẫn trên nhãn.','GYMPRO Nutrition','https://images.unsplash.com/photo-1579722821273-0f6c7d44362f?auto=format&fit=crop&w=800&q=80',399000,30,TRUE,'MUSCLE_GAIN,ENDURANCE',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30002);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,created_at)
SELECT 30003,'Protein Bar ít đường','SUPPLEMENT','Thanh protein 20 g đạm, phù hợp bữa phụ.','Fit Snack','https://images.unsplash.com/photo-1571748982800-fa51082c2224?auto=format&fit=crop&w=800&q=80',45000,60,TRUE,'WEIGHT_LOSS,MAINTENANCE',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30003);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,created_at)
SELECT 30004,'Ức gà áp chảo 300 g','FOOD','Suất ăn giàu protein, bảo quản lạnh.','GYMPRO Kitchen','https://images.unsplash.com/photo-1532550907401-a500c9a57435?auto=format&fit=crop&w=800&q=80',79000,20,TRUE,'MUSCLE_GAIN,WEIGHT_LOSS',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30004);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,created_at)
SELECT 30005,'Yến mạch hạt 500 g','FOOD','Yến mạch nguyên hạt cho bữa sáng.','Healthy Farm','https://images.unsplash.com/photo-1517673400267-0251440c45dc?auto=format&fit=crop&w=800&q=80',69000,35,TRUE,'WEIGHT_LOSS,ENDURANCE,MAINTENANCE',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30005);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,created_at)
SELECT 30006,'Combo 5 bữa kiểm soát calo','FOOD','Năm suất ăn đóng gói, thông tin năng lượng trên từng hộp.','GYMPRO Kitchen','https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=800&q=80',425000,15,TRUE,'WEIGHT_LOSS',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30006);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,required_equipment_code,created_at)
SELECT 30007,'Bộ dây kháng lực 5 mức','EQUIPMENT','Năm mức kháng lực dùng tập tại nhà.','GYMPRO Gear','https://images.unsplash.com/photo-1598289431512-b97b0917affc?auto=format&fit=crop&w=800&q=80',249000,25,TRUE,'MUSCLE_GAIN,WEIGHT_LOSS,ENDURANCE','RESISTANCE_BAND',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30007);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,required_equipment_code,created_at)
SELECT 30008,'Thảm tập 8 mm','EQUIPMENT','Thảm chống trượt cho bài sàn và giãn cơ.','GYMPRO Gear','https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?auto=format&fit=crop&w=800&q=80',299000,18,TRUE,'WEIGHT_LOSS,ENDURANCE,MAINTENANCE','MAT',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30008);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,required_equipment_code,created_at)
SELECT 30009,'Cặp tạ đơn 5 kg','EQUIPMENT','Cặp tạ bọc cao su tổng khối lượng 10 kg.','GYMPRO Gear','https://images.unsplash.com/photo-1586401100295-7a8096fd231a?auto=format&fit=crop&w=800&q=80',499000,12,TRUE,'MUSCLE_GAIN,MAINTENANCE','DUMBBELL',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30009);
INSERT INTO shop_products(id,name,category,description,brand,image_url,price,stock,active,suitable_goals,created_at)
SELECT 30010,'Găng tay tập gym','EQUIPMENT','Găng tay thoáng khí hỗ trợ cầm nắm.','GYMPRO Gear','https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=800&q=80',159000,40,TRUE,'MUSCLE_GAIN,MAINTENANCE',CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM shop_products WHERE id=30010);
ALTER TABLE shop_products ALTER COLUMN id RESTART WITH 1000000;

-- Kiem tra nhanh
SELECT u.id, u.full_name, u.email, u.phone, m.membership_type, m.payment_status, m.end_date
FROM users u
LEFT JOIN memberships m ON m.user_id = u.id AND m.is_active = TRUE
WHERE u.email = 'fulltest@gym.com';

SELECT p.id, p.plan_name, p.goal, p.created_at, p.is_completed,
       p.is_active, p.current_mana, p.max_mana, p.fitness_score,
       COUNT(s.id) AS completed_sessions
FROM workout_plans p
LEFT JOIN workout_sessions s ON s.workout_plan_id = p.id AND s.status = 'COMPLETED'
WHERE p.id IN (9001, 9002, 9003)
GROUP BY p.id, p.plan_name, p.goal, p.created_at, p.is_completed,
         p.is_active, p.current_mana, p.max_mana, p.fitness_score
ORDER BY p.created_at;
