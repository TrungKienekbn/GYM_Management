package com.example.gymmanagement.config;

import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.*;
import com.example.gymmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SystemConfigRepository systemConfigRepository;
    private final InjuryAreaOptionRepository injuryAreaOptionRepository;
    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
        initExercises();
        initSystemConfigs();
        initInjuryAreas();
        log.info("Data initialization complete.");
    }

    private void initInjuryAreas() {
        if (injuryAreaOptionRepository.count() == 0) {
            injuryAreaOptionRepository.saveAll(List.of(
                    new InjuryAreaOption(null, "KNEE", "Đầu gối"),
                    new InjuryAreaOption(null, "LOWER_BACK", "Lưng dưới"),
                    new InjuryAreaOption(null, "SHOULDER", "Vai"),
                    new InjuryAreaOption(null, "WRIST", "Cổ tay"),
                    new InjuryAreaOption(null, "ELBOW", "Khuỷu tay"),
                    new InjuryAreaOption(null, "ANKLE", "Cổ chân"),
                    new InjuryAreaOption(null, "NECK", "Cổ")
            ));
        }
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(null, "ROLE_ADMIN"),
                    new Role(null, "ROLE_USER")
            ));
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail("admin@gym.com")) {
            Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN").orElseThrow();
            User admin = User.builder()
                    .fullName("Admin").email("admin@gym.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("0900000000").status(true).emailVerified(true)
                    .role(adminRole).build();
            userRepository.save(admin);
            log.info("Admin created: admin@gym.com / admin123");
        }
    }

    private void initExercises() {
        if (exerciseRepository.count() == 0) {
            // muscleGain, weightLoss, endurance, flexibility, maintenance (0-10)
            List<Exercise> exercises = List.of(

                    // ── CHEST ─────────────────────────────────────────────────
                    Exercise.builder().name("Push Up").description("Bước 1: Đặt hai tay rộng hơn vai, siết bụng và giữ người thẳng. Bước 2: Hạ ngực có kiểm soát, khuỷu tay chếch khoảng 45 độ. Bước 3: Đẩy người lên và thở ra, không võng lưng.")
                            .videoUrl("https://www.youtube.com/watch?v=IODxDxX7oi4")
                            .muscleGroup(MuscleGroup.CHEST).difficulty(Difficulty.EASY)
                            .caloriesBurned(8).defaultSets(3).defaultReps(15).restSeconds(60)
                            .muscleGainScore(6).weightLossScore(5).enduranceScore(7).flexibilityScore(2).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Bench Press").description("Bước 1: Nằm chắc trên ghế, kéo bả vai về sau và đặt chân vững. Bước 2: Hạ thanh đòn về giữa ngực. Bước 3: Đẩy thanh lên có kiểm soát, không nhấc mông khỏi ghế.")
                            .videoUrl("https://www.youtube.com/watch?v=rT7DgCr-3pg")
                            .muscleGroup(MuscleGroup.CHEST).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(12).defaultSets(4).defaultReps(10).restSeconds(90)
                            .muscleGainScore(10).weightLossScore(4).enduranceScore(4).flexibilityScore(1).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Incline Dumbbell Press").description("Đẩy tạ đôi trên ghế nghiêng - phần trên ngực")
                            .muscleGroup(MuscleGroup.CHEST).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(10).defaultSets(3).defaultReps(12).restSeconds(90)
                            .muscleGainScore(9).weightLossScore(4).enduranceScore(3).flexibilityScore(2).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Cable Fly").description("Kéo cáp chéo - co cơ ngực sâu")
                            .muscleGroup(MuscleGroup.CHEST).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(7).defaultSets(3).defaultReps(15).restSeconds(60)
                            .muscleGainScore(8).weightLossScore(3).enduranceScore(3).flexibilityScore(4).maintenanceScore(6)
                            .isActive(true).build(),

                    // ── BACK ──────────────────────────────────────────────────
                    Exercise.builder().name("Pull Up").description("Xà đơn - lưng và bắp tay")
                            .muscleGroup(MuscleGroup.BACK).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(10).defaultSets(3).defaultReps(8).restSeconds(90)
                            .muscleGainScore(9).weightLossScore(5).enduranceScore(7).flexibilityScore(3).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Deadlift").description("Bước 1: Đặt thanh tạ trên giữa bàn chân, giữ lưng trung lập. Bước 2: Siết cơ lõi, đạp chân và đưa hông đứng thẳng. Bước 3: Hạ tạ sát chân, không cong lưng.")
                            .videoUrl("https://www.youtube.com/watch?v=op9kVnSso6Q")
                            .muscleGroup(MuscleGroup.BACK).difficulty(Difficulty.HARD)
                            .caloriesBurned(15).defaultSets(3).defaultReps(6).restSeconds(120)
                            .muscleGainScore(10).weightLossScore(6).enduranceScore(5).flexibilityScore(2).maintenanceScore(8)
                            .isActive(true).build(),

                    Exercise.builder().name("Seated Cable Row").description("Kéo cáp ngồi - lưng giữa và dày")
                            .muscleGroup(MuscleGroup.BACK).difficulty(Difficulty.EASY)
                            .caloriesBurned(8).defaultSets(3).defaultReps(12).restSeconds(60)
                            .muscleGainScore(8).weightLossScore(4).enduranceScore(5).flexibilityScore(2).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Lat Pulldown").description("Kéo xà xuống - lưng to và rộng")
                            .muscleGroup(MuscleGroup.BACK).difficulty(Difficulty.EASY)
                            .caloriesBurned(8).defaultSets(3).defaultReps(12).restSeconds(60)
                            .muscleGainScore(8).weightLossScore(4).enduranceScore(5).flexibilityScore(3).maintenanceScore(7)
                            .isActive(true).build(),

                    // ── SHOULDERS ─────────────────────────────────────────────
                    Exercise.builder().name("Overhead Press").description("Đẩy tạ trên đầu - vai trước và giữa")
                            .muscleGroup(MuscleGroup.SHOULDERS).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(10).defaultSets(3).defaultReps(10).restSeconds(90)
                            .muscleGainScore(9).weightLossScore(4).enduranceScore(4).flexibilityScore(2).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Lateral Raise").description("Nâng tạ ngang - vai giữa")
                            .muscleGroup(MuscleGroup.SHOULDERS).difficulty(Difficulty.EASY)
                            .caloriesBurned(6).defaultSets(3).defaultReps(15).restSeconds(60)
                            .muscleGainScore(7).weightLossScore(3).enduranceScore(5).flexibilityScore(3).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Face Pull").description("Kéo dây về mặt - vai sau và cuff xoay")
                            .muscleGroup(MuscleGroup.SHOULDERS).difficulty(Difficulty.EASY)
                            .caloriesBurned(5).defaultSets(3).defaultReps(15).restSeconds(60)
                            .muscleGainScore(6).weightLossScore(3).enduranceScore(4).flexibilityScore(6).maintenanceScore(7)
                            .isActive(true).build(),

                    // ── ARMS ──────────────────────────────────────────────────
                    Exercise.builder().name("Barbell Curl").description("Cuộn tạ thẳng - bắp tay trước")
                            .muscleGroup(MuscleGroup.ARMS).difficulty(Difficulty.EASY)
                            .caloriesBurned(6).defaultSets(3).defaultReps(12).restSeconds(60)
                            .muscleGainScore(8).weightLossScore(3).enduranceScore(4).flexibilityScore(2).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Tricep Pushdown").description("Đẩy cáp xuống - bắp tay sau")
                            .muscleGroup(MuscleGroup.ARMS).difficulty(Difficulty.EASY)
                            .caloriesBurned(6).defaultSets(3).defaultReps(15).restSeconds(60)
                            .muscleGainScore(8).weightLossScore(3).enduranceScore(4).flexibilityScore(2).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Hammer Curl").description("Cuộn búa - bắp tay và cẳng tay")
                            .muscleGroup(MuscleGroup.ARMS).difficulty(Difficulty.EASY)
                            .caloriesBurned(6).defaultSets(3).defaultReps(12).restSeconds(60)
                            .muscleGainScore(7).weightLossScore(3).enduranceScore(4).flexibilityScore(2).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Skull Crusher").description("Tạ đầu - bắp tay sau chuyên sâu")
                            .muscleGroup(MuscleGroup.ARMS).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(7).defaultSets(3).defaultReps(10).restSeconds(75)
                            .muscleGainScore(9).weightLossScore(3).enduranceScore(3).flexibilityScore(1).maintenanceScore(6)
                            .isActive(true).build(),

                    // ── LEGS ──────────────────────────────────────────────────
                    Exercise.builder().name("Squat").description("Bước 1: Đứng chân rộng bằng vai. Bước 2: Đẩy hông ra sau và hạ người, giữ gối cùng hướng mũi chân. Bước 3: Đạp cả bàn chân xuống để đứng lên.")
                            .videoUrl("https://www.youtube.com/watch?v=aclHkVaku9U")
                            .muscleGroup(MuscleGroup.LEGS).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(12).defaultSets(4).defaultReps(12).restSeconds(90)
                            .muscleGainScore(10).weightLossScore(7).enduranceScore(6).flexibilityScore(4).maintenanceScore(8)
                            .isActive(true).build(),

                    Exercise.builder().name("Romanian Deadlift").description("Kéo tạ Romania - đùi sau và mông")
                            .muscleGroup(MuscleGroup.LEGS).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(11).defaultSets(3).defaultReps(10).restSeconds(90)
                            .muscleGainScore(9).weightLossScore(6).enduranceScore(5).flexibilityScore(5).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Lunge").description("Bước lunge - đùi và mông đều tác động")
                            .muscleGroup(MuscleGroup.LEGS).difficulty(Difficulty.EASY)
                            .caloriesBurned(9).defaultSets(3).defaultReps(12).restSeconds(60)
                            .muscleGainScore(7).weightLossScore(7).enduranceScore(6).flexibilityScore(5).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Leg Press").description("Đẩy chân máy - đùi trước an toàn")
                            .muscleGroup(MuscleGroup.LEGS).difficulty(Difficulty.EASY)
                            .caloriesBurned(10).defaultSets(4).defaultReps(15).restSeconds(75)
                            .muscleGainScore(8).weightLossScore(5).enduranceScore(5).flexibilityScore(2).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Calf Raise").description("Nâng gót - bắp chân")
                            .muscleGroup(MuscleGroup.LEGS).difficulty(Difficulty.EASY)
                            .caloriesBurned(5).defaultSets(4).defaultReps(20).restSeconds(45)
                            .muscleGainScore(6).weightLossScore(3).enduranceScore(6).flexibilityScore(3).maintenanceScore(5)
                            .isActive(true).build(),

                    // ── CORE ──────────────────────────────────────────────────
                    Exercise.builder().name("Plank").description("Bước 1: Chống cẳng tay dưới vai, duỗi chân và siết bụng. Bước 2: Giữ đầu, lưng và hông thành một đường thẳng. Bước 3: Thở đều và dừng khi hông bắt đầu võng.")
                            .videoUrl("https://www.youtube.com/watch?v=ASdvN_XEl_c")
                            .muscleGroup(MuscleGroup.CORE).difficulty(Difficulty.EASY)
                            .caloriesBurned(5).defaultSets(3).defaultDurationSeconds(60).restSeconds(30)
                            .muscleGainScore(5).weightLossScore(5).enduranceScore(8).flexibilityScore(3).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Crunch").description("Gập bụng cơ bản")
                            .muscleGroup(MuscleGroup.CORE).difficulty(Difficulty.EASY)
                            .caloriesBurned(5).defaultSets(3).defaultReps(20).restSeconds(30)
                            .muscleGainScore(5).weightLossScore(5).enduranceScore(6).flexibilityScore(3).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Russian Twist").description("Xoay người với tạ - cơ chéo bụng")
                            .muscleGroup(MuscleGroup.CORE).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(7).defaultSets(3).defaultReps(20).restSeconds(45)
                            .muscleGainScore(5).weightLossScore(7).enduranceScore(6).flexibilityScore(5).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Leg Raise").description("Nâng chân nằm - cơ bụng dưới")
                            .muscleGroup(MuscleGroup.CORE).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(6).defaultSets(3).defaultReps(15).restSeconds(45)
                            .muscleGainScore(5).weightLossScore(6).enduranceScore(6).flexibilityScore(4).maintenanceScore(6)
                            .isActive(true).build(),

                    // ── CARDIO ────────────────────────────────────────────────
                    Exercise.builder().name("Treadmill Run").description("Chạy bộ máy - cardio hiệu quả nhất")
                            .muscleGroup(MuscleGroup.CARDIO).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(12).defaultSets(1).defaultDurationSeconds(1800).restSeconds(0)
                            .muscleGainScore(1).weightLossScore(10).enduranceScore(10).flexibilityScore(1).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Jump Rope").description("Nhảy dây - đốt calories nhanh")
                            .muscleGroup(MuscleGroup.CARDIO).difficulty(Difficulty.EASY)
                            .caloriesBurned(13).defaultSets(3).defaultDurationSeconds(300).restSeconds(60)
                            .muscleGainScore(1).weightLossScore(9).enduranceScore(9).flexibilityScore(3).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Cycling").description("Đạp xe tại chỗ - cardio khớp gối an toàn")
                            .muscleGroup(MuscleGroup.CARDIO).difficulty(Difficulty.EASY)
                            .caloriesBurned(10).defaultSets(1).defaultDurationSeconds(1800).restSeconds(0)
                            .muscleGainScore(2).weightLossScore(8).enduranceScore(9).flexibilityScore(2).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Rowing Machine").description("Máy chèo thuyền - toàn thân và cardio")
                            .muscleGroup(MuscleGroup.CARDIO).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(11).defaultSets(1).defaultDurationSeconds(1200).restSeconds(0)
                            .muscleGainScore(4).weightLossScore(9).enduranceScore(9).flexibilityScore(3).maintenanceScore(7)
                            .isActive(true).build(),

                    // ── FULL BODY ─────────────────────────────────────────────
                    Exercise.builder().name("Burpee").description("Bài tập toàn thân - sức mạnh và cardio kết hợp")
                            .muscleGroup(MuscleGroup.FULL_BODY).difficulty(Difficulty.HARD)
                            .caloriesBurned(15).defaultSets(3).defaultReps(10).restSeconds(90)
                            .muscleGainScore(5).weightLossScore(10).enduranceScore(9).flexibilityScore(4).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Kettlebell Swing").description("Xoay tạ ấm - hông, lưng dưới, cardio")
                            .muscleGroup(MuscleGroup.FULL_BODY).difficulty(Difficulty.MEDIUM)
                            .caloriesBurned(13).defaultSets(4).defaultReps(15).restSeconds(60)
                            .muscleGainScore(6).weightLossScore(9).enduranceScore(8).flexibilityScore(4).maintenanceScore(7)
                            .isActive(true).build(),

                    Exercise.builder().name("Clean and Press").description("Kéo và đẩy tạ - sức mạnh bùng nổ")
                            .muscleGroup(MuscleGroup.FULL_BODY).difficulty(Difficulty.HARD)
                            .caloriesBurned(14).defaultSets(3).defaultReps(8).restSeconds(120)
                            .muscleGainScore(9).weightLossScore(7).enduranceScore(7).flexibilityScore(3).maintenanceScore(8)
                            .isActive(true).build(),

                    // ── FLEXIBILITY ───────────────────────────────────────────
                    Exercise.builder().name("Yoga Sun Salutation").description("Chào mặt trời - kéo giãn và thở")
                            .muscleGroup(MuscleGroup.FULL_BODY).difficulty(Difficulty.EASY)
                            .caloriesBurned(4).defaultSets(3).defaultDurationSeconds(300).restSeconds(30)
                            .muscleGainScore(1).weightLossScore(3).enduranceScore(4).flexibilityScore(10).maintenanceScore(6)
                            .isActive(true).build(),

                    Exercise.builder().name("Hip Flexor Stretch").description("Kéo giãn hông - giảm đau lưng")
                            .muscleGroup(MuscleGroup.LEGS).difficulty(Difficulty.EASY)
                            .caloriesBurned(2).defaultSets(2).defaultDurationSeconds(60).restSeconds(30)
                            .muscleGainScore(1).weightLossScore(1).enduranceScore(2).flexibilityScore(10).maintenanceScore(5)
                            .isActive(true).build()
            );

            exerciseRepository.saveAll(exercises);
            log.info("Exercises initialized: {} bài tập với chỉ số benefit", exercises.size());
        }
    }
    private void initSystemConfigs() {
        if (systemConfigRepository.count() > 0) return;

        systemConfigRepository.saveAll(List.of(
                SystemConfig.builder().configKey("FS_WEIGHT_AGE").configValue(0.4).category("Fitness Score")
                        .description("Trọng số điểm TUỔI trong công thức tính Fitness Score. " +
                                "FS = S_tuổi × trọng_số_tuổi + S_cân_nặng × trọng_số_cân_nặng. " +
                                "Tổng 2 trọng số (tuổi + cân nặng) nên = 1.0.").build(),

                SystemConfig.builder().configKey("FS_WEIGHT_WEIGHT").configValue(0.6).category("Fitness Score")
                        .description("Trọng số điểm CÂN NẶNG trong công thức tính Fitness Score. " +
                                "Cân nặng ảnh hưởng nhiều hơn tuổi vì phản ánh trực tiếp thể trạng hiện tại.").build(),

                SystemConfig.builder().configKey("MANA_MAX_MULTIPLIER").configValue(2.0).category("Mana")
                        .description("Hệ số nhân để tính Mana (thể lực) tối đa từ Fitness Score. " +
                                "Công thức: maxMana = round(FS × hệ_số_này). VD: FS=70 -> maxMana=140.").build(),

                SystemConfig.builder().configKey("MANA_REGEN_RATE_1_DAY").configValue(0.75).category("Mana")
                        .description("Tỷ lệ % Mana được hồi lại khi user nghỉ ĐÚNG 1 ngày rồi tập lại. " +
                                "VD: 0.75 = hồi 75% maxMana. Nếu nghỉ >= 2 ngày thì hồi đầy (100%), " +
                                "nghỉ 0 ngày (tập nhiều buổi/ngày) thì không hồi thêm.").build(),

                SystemConfig.builder().configKey("MANA_ENOUGH_THRESHOLD").configValue(0.75).category("Mana")
                        .description("Ngưỡng % Mana tối đa mới được coi là 'đủ sức' để hệ thống tự động " +
                                "hoàn thành giáo án 'cải thiện thể lực' và chuyển user sang giáo án chính thức mới.").build(),

                SystemConfig.builder().configKey("STAMINA_COST_DEFAULT").configValue(10.0).category("Mana")
                        .description("Chi phí thể lực (stamina) MẶC ĐỊNH cho 1 bài tập, dùng khi bài tập đó " +
                                "chưa được admin thiết lập staminaCost riêng trong phần Quản lý bài tập.").build(),

                SystemConfig.builder().configKey("FREE_PLAN_LIMIT_PER_MONTH").configValue(1.0).category("Giáo án")
                        .description("Số giáo án tối đa mà user dùng gói MIỄN PHÍ (không phải VIP) " +
                                "được phép tạo mới trong 1 tháng.").build(),

                SystemConfig.builder().configKey("PROGRESS_TOLERANCE_PERCENT").configValue(5.0).category("Giáo án")
                        .description("Dung sai % chênh lệch giữa 'tiến độ mục tiêu' và 'tiến độ thời gian đã dùng'. " +
                                "Nếu vượt ngưỡng này, hệ thống tự rút ngắn hoặc gia hạn thêm 1 tuần cho giáo án.").build(),

                SystemConfig.builder().configKey("MIN_DURATION_WEEKS").configValue(1.0).category("Giáo án")
                        .description("Số tuần TỐI THIỂU của 1 giáo án, dùng để chặn khi hệ thống tự rút ngắn thời lượng.").build(),

                SystemConfig.builder().configKey("MAX_DURATION_WEEKS").configValue(50.0).category("Giáo án")
                        .description("Số tuần TỐI ĐA của 1 giáo án. Khi chạm ngưỡng này mà chưa đạt mục tiêu, " +
                                "giáo án sẽ tự kết thúc và nhắc user tham khảo huấn luyện viên.").build(),

                SystemConfig.builder().configKey("ACHIEVEMENT_THRESHOLD").configValue(0.95).category("Giáo án")
                        .description("Tỷ lệ % hoàn thành khoảng cách từ baseline đến mục tiêu để hệ thống coi là " +
                                "'đã đạt mục tiêu'. VD: 0.95 = đạt 95% quãng đường từ điểm xuất phát đến mục tiêu là được tính đạt.").build(),

                SystemConfig.builder().configKey("REST_MULTIPLIER_MUSCLE_GAIN").configValue(1.3).category("Giáo án")
                        .description("Hệ số nhân thời gian NGHỈ giữa các set cho mục tiêu Tăng cơ. " +
                                "> 1.0 nghĩa là nghỉ LÂU HƠN bình thường để cơ bắp phục hồi sức mạnh trước set tiếp theo.").build(),

                SystemConfig.builder().configKey("REST_MULTIPLIER_WEIGHT_LOSS").configValue(0.7).category("Giáo án")
                        .description("Hệ số nhân thời gian NGHỈ giữa các set cho mục tiêu Giảm cân. " +
                                "< 1.0 nghĩa là nghỉ NGẮN HƠN bình thường để giữ nhịp tim cao, đốt nhiều calo hơn.").build(),

                SystemConfig.builder().configKey("EXERCISE_DURATION_BEGINNER").configValue(0.7).category("Giáo án")
                        .description("Hệ số nhân thời lượng thực hiện bài tập (giây) cho người trình độ MỚI BẮT ĐẦU. " +
                                "< 1.0 nghĩa là rút ngắn thời gian giữ tư thế/thực hiện so với mức chuẩn.").build(),

                SystemConfig.builder().configKey("EXERCISE_DURATION_ADVANCED").configValue(1.3).category("Giáo án")
                        .description("Hệ số nhân thời lượng thực hiện bài tập (giây) cho người trình độ NÂNG CAO. " +
                                "> 1.0 nghĩa là tăng thời gian giữ tư thế/thực hiện so với mức chuẩn.").build()
        ));
        log.info("System configs initialized.");
    }
}
