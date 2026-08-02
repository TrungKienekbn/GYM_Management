-- ============================================================
-- GYMPRO - DU LIEU FULL TEST CHO H2 CONSOLE
-- URL:      http://localhost:8080/h2-console
-- JDBC URL: jdbc:h2:file:./gymdb
-- User: sa | Password: de trong
--
-- Tai khoan: fulltest@gym.com / password
-- Dang nhap bang dien thoai: email tren + 4 so cuoi 1234
-- Co the chay lai file nay nhieu lan ma khong tao trung du lieu.
-- ============================================================

-- 1. Tai khoan test toan bo chuc nang
INSERT INTO users (id, full_name, email, password, phone, status, email_verified, created_at, role_id)
SELECT 9001, 'Full Test', 'fulltest@gym.com',
       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
       '0900001234', TRUE, TRUE, CURRENT_TIMESTAMP,
       (SELECT id FROM roles WHERE role_name = 'ROLE_USER')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'fulltest@gym.com');

UPDATE users
SET full_name = 'Full Test', phone = '0900001234', status = TRUE, email_verified = TRUE,
    password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'
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

-- 3. Goi VIP da thanh toan, con han 1 nam
INSERT INTO memberships
    (id, user_id, membership_type, start_date, end_date, price, is_active,
     payment_status, transaction_id, payment_method, paid_at, created_at, notes)
SELECT 9001, u.id, 'VIP', CURRENT_DATE, DATEADD('DAY', 365, CURRENT_DATE),
       1199000.0, TRUE, 'PAID', 'FULLTEST-VIP-PAID', 'TEST',
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
SELECT 9001, u.id, 74.0, 175.0, 24.16, 18.0, 31.0,
       94.0, 82.0, 96.0, 34.0, 55.0, DATEADD('DAY', -30, CURRENT_DATE),
       DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'MANUAL', 'Moc bat dau Full Test'
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id = 9001);

INSERT INTO progress_tracking
    (id, user_id, weight, height, bmi, body_fat_percentage, muscle_mass_kg,
     chest_cm, waist_cm, hip_cm, arm_cm, thigh_cm, recorded_date, recorded_at, source, notes)
SELECT 9002, u.id, 72.0, 175.0, 23.51, 16.0, 32.5,
       96.0, 79.0, 95.0, 35.0, 56.0, CURRENT_DATE,
       CURRENT_TIMESTAMP, 'MANUAL', 'Tien do hien tai Full Test'
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM progress_tracking WHERE id = 9002);

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
SELECT 9001, u.id, 'VIP', m.id, 1199000.0, 'PAID', 'FULLTEST-INVOICE-PAID',
       'Thanh toan test thanh cong', 'GYMPROFULLTEST', DATEADD('MINUTE', -2, CURRENT_TIMESTAMP),
       DATEADD('MINUTE', 3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, 0, 'MEMBERSHIP'
FROM users u
JOIN memberships m ON m.user_id = u.id AND m.membership_type = 'VIP' AND m.is_active = TRUE
WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM invoices WHERE transaction_id = 'FULLTEST-INVOICE-PAID');

-- 6. Bai tap test active va bai tap an de kiem tra filter, phan trang, khoi phuc
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

-- 7. Hai giao an da hoan thanh trong 2-3 thang gan day
INSERT INTO workout_plans
    (id, user_id, plan_name, description, goal, target_level, duration_weeks,
     sessions_per_week, current_week, is_active, is_ai_generated, is_template,
     is_completed, week_start_date, starting_bmi, starting_weight,
     difficulty_adjustment, sets_adjustment, reps_adjustment, exercises_adjustment,
     weight_adjustment_note, max_mana, current_mana, created_at, estimated_weeks)
SELECT 9001, u.id, 'Full Test - Tang co thang 1',
       'Giao an 4 tuan da hoan thanh cach day khoang 3 thang',
       'MUSCLE_GAIN', 'INTERMEDIATE', 4, 2, 4, FALSE, TRUE, FALSE, TRUE,
       DATEADD('MONTH', -3, CURRENT_DATE), 24.16, 74.0,
       1, 1, 1, 0, 'Da hoan thanh du 8/8 buoi', 100, 82,
       DATEADD('MONTH', -3, CURRENT_TIMESTAMP), 4
FROM users u WHERE u.email = 'fulltest@gym.com'
  AND NOT EXISTS (SELECT 1 FROM workout_plans WHERE id = 9001);

INSERT INTO workout_plans
    (id, user_id, plan_name, description, goal, target_level, duration_weeks,
     sessions_per_week, current_week, is_active, is_ai_generated, is_template,
     is_completed, week_start_date, starting_bmi, starting_weight,
     difficulty_adjustment, sets_adjustment, reps_adjustment, exercises_adjustment,
     weight_adjustment_note, max_mana, current_mana, created_at, estimated_weeks)
SELECT 9002, u.id, 'Full Test - Dot mo thang 2',
       'Giao an 4 tuan da hoan thanh cach day khoang 2 thang',
       'WEIGHT_LOSS', 'INTERMEDIATE', 4, 2, 4, FALSE, TRUE, FALSE, TRUE,
       DATEADD('MONTH', -2, CURRENT_DATE), 23.84, 73.0,
       1, 0, 2, 0, 'Da hoan thanh du 8/8 buoi', 100, 88,
       DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 4
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
               DATEADD('MONTH', -3, CURRENT_DATE)),
       TIME '18:00:00',
       DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -3, CURRENT_DATE)) AS TIMESTAMP)),
       DATEADD('MINUTE', 60, DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -3, CURRENT_DATE)) AS TIMESTAMP))),
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
               DATEADD('MONTH', -2, CURRENT_DATE)),
       TIME '18:30:00',
       DATEADD('MINUTE', 30, DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -2, CURRENT_DATE)) AS TIMESTAMP))),
       DATEADD('MINUTE', 58, DATEADD('MINUTE', 30, DATEADD('HOUR', 18, CAST(DATEADD('DAY', FLOOR((r.X - 1) / 2) * 7 + CASE WHEN MOD(r.X, 2) = 1 THEN 0 ELSE 3 END,
               DATEADD('MONTH', -2, CURRENT_DATE)) AS TIMESTAMP)))),
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

-- 8. Dong bo IDENTITY sau khi chen ID test thu cong
ALTER TABLE users ALTER COLUMN id RESTART WITH 9100;
ALTER TABLE user_profiles ALTER COLUMN id RESTART WITH 9100;
ALTER TABLE memberships ALTER COLUMN id RESTART WITH 9100;
ALTER TABLE progress_tracking ALTER COLUMN id RESTART WITH 9100;
ALTER TABLE exercises ALTER COLUMN id RESTART WITH 9100;
ALTER TABLE user_cosmetic_ownership ALTER COLUMN id RESTART WITH 9100;
ALTER TABLE invoices ALTER COLUMN id RESTART WITH 9100;
ALTER TABLE workout_plans ALTER COLUMN id RESTART WITH 9300;
ALTER TABLE workout_plan_days ALTER COLUMN id RESTART WITH 9300;
ALTER TABLE workout_plan_exercises ALTER COLUMN id RESTART WITH 9300;
ALTER TABLE workout_sessions ALTER COLUMN id RESTART WITH 9300;
ALTER TABLE session_exercise_logs ALTER COLUMN id RESTART WITH 9300;

-- Kiem tra nhanh
SELECT u.id, u.full_name, u.email, u.phone, m.membership_type, m.payment_status, m.end_date
FROM users u
LEFT JOIN memberships m ON m.user_id = u.id AND m.is_active = TRUE
WHERE u.email = 'fulltest@gym.com';

SELECT p.id, p.plan_name, p.goal, p.created_at, p.is_completed,
       COUNT(s.id) AS completed_sessions
FROM workout_plans p
LEFT JOIN workout_sessions s ON s.workout_plan_id = p.id AND s.status = 'COMPLETED'
WHERE p.id IN (9001, 9002)
GROUP BY p.id, p.plan_name, p.goal, p.created_at, p.is_completed
ORDER BY p.created_at;
