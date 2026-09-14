package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.request.ProgressRequest;
import com.example.gymmanagement.dto.response.ProgressResponse;
import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.gymmanagement.enums.ProgressSource;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressTrackingRepository progressRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final MembershipService membershipService;

    @Transactional
    public ProgressResponse addProgress(String email, ProgressRequest request) {
        if (request.getWeight() == null || !Double.isFinite(request.getWeight())
                || request.getWeight() < 30 || request.getWeight() > 250) {
            throw new RuntimeException("Cân nặng phải từ 30 đến 250 kg");
        }
        User user = getUser(email);
        LocalDate recordedDate = request.getRecordedDate() != null ? request.getRecordedDate() : LocalDate.now();

        double bmi = 0;
        if (request.getWeight() != null && request.getHeight() != null && request.getHeight() > 0) {
            double h = request.getHeight() / 100.0;
            bmi = Math.round(request.getWeight() / (h * h) * 10.0) / 10.0;
        } else {
            // Use height from profile
            Optional<UserProfile> profile = profileRepository.findByUserId(user.getId());
            if (profile.isPresent() && profile.get().getHeight() != null && request.getWeight() != null) {
                double h = profile.get().getHeight() / 100.0;
                bmi = Math.round(request.getWeight() / (h * h) * 10.0) / 10.0;
            }
        }

        // Một bản ghi có thể đã được tạo tự động từ hồ sơ hoặc lúc checkout.
        // Khi người dùng nhập lại trong cùng ngày, cập nhật bản ghi gần nhất thay vì
        // từ chối khiến cân nặng mới không được lưu.
        ProgressTracking pt = progressRepository
                .findFirstByUserIdAndRecordedDateOrderByIdDesc(user.getId(), recordedDate)
                .orElseGet(() -> ProgressTracking.builder()
                        .user(user)
                        .recordedDate(recordedDate)
                        .build());
        pt.setWeight(request.getWeight());
        pt.setHeight(request.getHeight());
        pt.setBmi(bmi > 0 ? bmi : null);
        pt.setBodyFatPercentage(request.getBodyFatPercentage());
        pt.setSource(ProgressSource.MANUAL);
        pt.setMuscleMassKg(request.getMuscleMassKg());
        pt.setChestCm(request.getChestCm());
        pt.setWaistCm(request.getWaistCm());
        pt.setHipCm(request.getHipCm());
        pt.setArmCm(request.getArmCm());
        pt.setThighCm(request.getThighCm());
        pt.setNotes(request.getNotes());
        progressRepository.save(pt);
        removeOlderRecordsFromSameDay(user.getId(), recordedDate, pt.getId());

        // Tạo biến effectively final để dùng trong Lambda
        double finalBmi = bmi;

        // Update profile weight/bmi
        profileRepository.findByUserId(user.getId()).ifPresent(p -> {
            if (request.getWeight() != null) p.setWeight(request.getWeight());
            if (finalBmi > 0) p.setBmi(finalBmi); // Đã hết lỗi đỏ
            profileRepository.save(p);
        });

        return buildResponse(pt, null);
    }

    public List<ProgressResponse> getMyProgress(String email) {
        User user = getUser(email);
        List<ProgressTracking> list = progressRepository.findByUserIdOrderByDateAsc(user.getId());

        List<ProgressResponse> result = java.util.stream.IntStream.range(0, list.size()).mapToObj(i -> {
            ProgressTracking pt = list.get(i);
            Double weightChange = null;
            if (i > 0) {
                ProgressTracking prev = list.get(i - 1);
                if (pt.getWeight() != null && prev.getWeight() != null) {
                    weightChange = Math.round((pt.getWeight() - prev.getWeight()) * 10.0) / 10.0;
                }
            }
            return buildResponse(pt, weightChange);
        }).collect(Collectors.toList());
        if (!membershipService.isVip(user)) {
            LocalDate cutoff = LocalDate.now().minusWeeks(4);
            return result.stream()
                    .filter(p -> p.getRecordedDate() == null || !p.getRecordedDate().isBefore(cutoff))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public ProgressResponse getLatestProgress(String email) {
        User user = getUser(email);
        return progressRepository.findFirstByUserIdOrderByRecordedDateDesc(user.getId())
                .map(p -> buildResponse(p, null))
                .orElse(null);
    }

    public ProgressResponse updateProgress(String email, Long id, ProgressRequest request) {
        User user = getUser(email);
        ProgressTracking pt = progressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        if (!pt.getUser().getId().equals(user.getId())) throw new RuntimeException("Access denied");

        if (request.getWeight() != null) pt.setWeight(request.getWeight());
        if (request.getBodyFatPercentage() != null) pt.setBodyFatPercentage(request.getBodyFatPercentage());
        if (request.getWaistCm() != null) pt.setWaistCm(request.getWaistCm());
        if (request.getChestCm() != null) pt.setChestCm(request.getChestCm());
        if (request.getNotes() != null) pt.setNotes(request.getNotes());
        progressRepository.save(pt);

        profileRepository.findByUserId(user.getId())
                .ifPresent(profile -> {

                    if (request.getWeight() != null) {
                        profile.setWeight(request.getWeight());
                    }

                    if (request.getBodyFatPercentage() != null) {
                        profile.setBodyFatPercentage(
                                request.getBodyFatPercentage()
                        );
                    }

                    profileRepository.save(profile);
                });

        return buildResponse(pt, null);
    }

    private ProgressResponse buildResponse(ProgressTracking pt, Double weightChange) {
        return ProgressResponse.builder()
                .id(pt.getId())
                .weight(pt.getWeight())
                .height(pt.getHeight())
                .bmi(pt.getBmi())
                .bodyFatPercentage(pt.getBodyFatPercentage())
                .muscleMassKg(pt.getMuscleMassKg())
                .chestCm(pt.getChestCm())
                .waistCm(pt.getWaistCm())
                .hipCm(pt.getHipCm())
                .armCm(pt.getArmCm())
                .thighCm(pt.getThighCm())
                .recordedDate(pt.getRecordedDate())
                .notes(pt.getNotes())
                .weightChange(weightChange)
                .source(pt.getSource())
                .build();
    }

    @Transactional
    public void autoSaveProgress(
            User user,
            Double weight,
            Double bodyFat,
            String note,
            ProgressSource source,
            LocalDate recordedDate) {  //mới thêm  LocalDate recordedDate

        UserProfile profile =
                profileRepository.findByUserId(user.getId())
                        .orElse(null);

        Double height = null;

        if(profile != null){
            height = profile.getHeight();
        }

        Double bmi = null;

        if(weight != null &&
                height != null &&
                height > 0){

            double h = height / 100.0;

            bmi = Math.round(
                    weight / (h * h) * 10.0
            ) / 10.0;
        }

        LocalDate effectiveDate = recordedDate != null ? recordedDate : LocalDate.now();
        ProgressTracking progress = progressRepository
                .findFirstByUserIdAndRecordedDateOrderByIdDesc(user.getId(), effectiveDate)
                .orElseGet(() -> ProgressTracking.builder()
                        .user(user)
                        .recordedDate(effectiveDate)
                        .build());

        progress.setWeight(weight);
        progress.setHeight(height);
        progress.setBodyFatPercentage(bodyFat);
        progress.setBmi(bmi);
        progress.setSource(source);
        progress.setNotes(note);

        progressRepository.save(progress);
        removeOlderRecordsFromSameDay(user.getId(), effectiveDate, progress.getId());
    }

    private void removeOlderRecordsFromSameDay(Long userId, LocalDate recordedDate, Long keptId) {
        List<ProgressTracking> duplicates = progressRepository
                .findByUserIdAndRecordedDateOrderByIdDesc(userId, recordedDate)
                .stream()
                .filter(record -> !record.getId().equals(keptId))
                .toList();
        if (!duplicates.isEmpty()) progressRepository.deleteAll(duplicates);
    }


    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
