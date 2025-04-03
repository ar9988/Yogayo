package com.red.yogaback.service;

import com.red.yogaback.model.UserRecord;
import com.red.yogaback.repository.UserRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRecordScheduler {

    private final UserRecordRepository userRecordRepository;

    /**
     * 매일 12시 1분(자정 12:01)에 실행하여,
     * 각 UserRecord의 운동 날짜 관련 필드를 확인합니다.
     *
     * [초기화하지 않는 조합]
     * 1. currentExerciseDate가 오늘, previousExerciseDate가 어제 또는 null
     * 2. currentExerciseDate가 어제, previousExerciseDate가 null 또는 어제보다 과거인 경우
     *
     * 이 외의 조합이면 연속 운동 기록(exConDays)을 0으로 초기화합니다.
     */
    @Scheduled(cron = "0 1 00 * * *")
    public void updateConsecutiveExerciseDays() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        List<UserRecord> userRecords = userRecordRepository.findAll();

        for (UserRecord record : userRecords) {
            // currentExerciseDate가 null이면 무시
            if (record.getCurrentExerciseDate() == null) {
                continue;
            }
            boolean valid = false;
            LocalDate current = record.getCurrentExerciseDate();
            LocalDate previous = record.getPreviousExerciseDate();
            if (current.equals(today)) {
                // 현재 운동 기록이 오늘일 경우, 이전 운동 기록이 어제이거나 null이면 유지
                if (previous == null || previous.equals(yesterday)) {
                    valid = true;
                }
            } else if (current.equals(yesterday)) {
                // 현재 운동 기록이 어제일 경우, 이전 운동 기록이 null이거나 어제보다 과거이면 유지
                if (previous == null || previous.isBefore(yesterday)) {
                    valid = true;
                }
            }
            if (!valid) {
                record.setExConDays(0L);
                log.info("User {}: currentExerciseDate={} and previousExerciseDate={} are not a valid consecutive combination. Reset exConDays to 0.",
                        record.getUser().getUserId(), current, previous);
            }
            userRecordRepository.save(record);
            log.info("Updated exConDays for userId {}: exConDays = {}", record.getUser().getUserId(), record.getExConDays());
        }
    }
}
