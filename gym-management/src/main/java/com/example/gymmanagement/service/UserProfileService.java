package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.request.UserProfileRequest;
import com.example.gymmanagement.dto.response.UserProfileResponse;
import com.example.gymmanagement.entity.User;
import com.example.gymmanagement.entity.UserProfile;
import com.example.gymmanagement.enums.ProgressSource;
import com.example.gymmanagement.repository.UserProfileRepository;
import com.example.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;

    public UserProfileResponse saveOrUpdate(String email, UserProfileRequest request) {
        validateProfile(request);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(UserProfile.builder().user(user).build());

        // 1. Cập nhật ngày sinh từ request
        profile.setDateOfBirth(request.getDateOfBirth());

        // 2. Tự động tính tuổi dựa trên ngày sinh (cập nhật vào trường age của Entity)
        if (request.getDateOfBirth() != null) {
            int calculatedAge = java.time.Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
            profile.setAge(calculatedAge);
        }

        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setBodyFatPercentage(request.getBodyFatPercentage());
        profile.setGender(request.getGender());
        profile.setGoal(request.getGoal());
        profile.setFitnessLevel(request.getFitnessLevel());
        profile.setAvailableDaysPerWeek(request.getAvailableDaysPerWeek());
        profile.setPreferredSessionDuration(request.getPreferredSessionDuration());
        profile.setMedicalConditions(request.getMedicalConditions());
        profile.setTrainingExperienceMonths(request.getTrainingExperienceMonths());
        profile.setDailyActivityLevel(request.getDailyActivityLevel());
        profile.setTrainingLocation(request.getTrainingLocation());
        profile.setAvailableEquipment(request.getAvailableEquipment());
        profile.setPreferredTrainingDays(request.getPreferredTrainingDays());
        profile.setInjuryAreas(request.getInjuryAreas());
        profile.setDislikedExercises(request.getDislikedExercises());

        // Calculate BMI (giữ nguyên logic)
        if (request.getHeight() != null && request.getWeight() != null && request.getHeight() > 0) {
            double heightM = request.getHeight() / 100.0;
            double bmi = request.getWeight() / (heightM * heightM);
            profile.setBmi(Math.round(bmi * 10.0) / 10.0);
        }

        profile = profileRepository.save(profile);

        progressService.autoSaveProgress(
                user,
                profile.getWeight(),
                profile.getBodyFatPercentage(),
                "Khởi tạo hồ sơ",
                ProgressSource.PROFILE,
                LocalDate.now()
        );

        return buildResponse(profile, user);
    }

    private void validateProfile(UserProfileRequest request) {
        if (request.getDateOfBirth() == null) throw new RuntimeException("Vui lòng chọn ngày sinh");
        int age = java.time.Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 16 || age > 80) throw new RuntimeException("Độ tuổi sử dụng hệ thống phải từ 16 đến 80");
        if (request.getHeight() == null || !Double.isFinite(request.getHeight())
                || request.getHeight() < 100 || request.getHeight() > 250)
            throw new RuntimeException("Chiều cao phải từ 100 đến 250 cm");
        if (request.getWeight() == null || !Double.isFinite(request.getWeight())
                || request.getWeight() < 30 || request.getWeight() > 250)
            throw new RuntimeException("Cân nặng phải từ 30 đến 250 kg");
        if (request.getAvailableDaysPerWeek() == null || request.getAvailableDaysPerWeek() < 1
                || request.getAvailableDaysPerWeek() > 7)
            throw new RuntimeException("Số ngày tập mỗi tuần phải từ 1 đến 7");
        if (request.getPreferredSessionDuration() == null || request.getPreferredSessionDuration() < 15
                || request.getPreferredSessionDuration() > 120)
            throw new RuntimeException("Thời lượng mỗi buổi phải từ 15 đến 120 phút");
        if (request.getTrainingExperienceMonths() == null || request.getTrainingExperienceMonths() < 0
                || request.getTrainingExperienceMonths() > 720)
            throw new RuntimeException("Kinh nghiệm tập luyện không hợp lệ");
    }

    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not set up yet. Please complete your profile."));

        return buildResponse(profile, user);
    }

    public UserProfileResponse getProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for this user"));

        return buildResponse(profile, user);
    }

    private UserProfileResponse buildResponse(UserProfile profile, User user) {
        String bmiCategory = getBmiCategory(profile.getBmi());
        return UserProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .bodyFatPercentage(profile.getBodyFatPercentage())
                // Thay thế .age(profile.getAge()) bằng dateOfBirth
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .bmi(profile.getBmi())
                .bmiCategory(bmiCategory)
                .goal(profile.getGoal())
                .fitnessLevel(profile.getFitnessLevel())
                .availableDaysPerWeek(profile.getAvailableDaysPerWeek())
                .preferredSessionDuration(profile.getPreferredSessionDuration())
                .medicalConditions(profile.getMedicalConditions())
                .trainingExperienceMonths(profile.getTrainingExperienceMonths())
                .dailyActivityLevel(profile.getDailyActivityLevel())
                .trainingLocation(profile.getTrainingLocation())
                .availableEquipment(profile.getAvailableEquipment())
                .preferredTrainingDays(profile.getPreferredTrainingDays())
                .injuryAreas(profile.getInjuryAreas())
                .dislikedExercises(profile.getDislikedExercises())
                .build();
    }

    private String getBmiCategory(Double bmi) {
        if (bmi == null) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }
}
