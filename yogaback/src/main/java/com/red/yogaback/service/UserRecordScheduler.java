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
     * - currentExerciseDate가 null이면 아무 작업도 하지 않습니다.
     * - currentExerciseDate가 어제 날짜가 아니라면,
     *      만약 previousExerciseDate도 어제 날짜가 아니라면 exConDays(연속 운동 일수)를 0으로 재설정합니다.
     */
    @Scheduled(cron = "0 1 00 * * *")
    public void updateConsecutiveExerciseDays() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<UserRecord> userRecords = userRecordRepository.findAll();

        for (UserRecord record : userRecords) {
            // 현재 운동 기록이 없으면 무시
            if (record.getCurrentExerciseDate() != null) {
                // 오늘 기록이 어제가 아니라면
                if (!record.getCurrentExerciseDate().equals(yesterday)) {
                    // 이전 운동 기록이 없거나 어제와 다르면 연속 운동이 끊겼으므로 0으로 재설정
                    if (record.getPreviousExerciseDate() == null || !record.getPreviousExerciseDate().equals(yesterday)) {
                        record.setExConDays(0L);
                        log.info("User {} did not exercise yesterday. Reset exConDays to 0.", record.getUser().getUserId());
                    }
                }
            }
            userRecordRepository.save(record);
            log.info("Updated exConDays for userId {}: exConDays = {}", record.getUser().getUserId(), record.getExConDays());
        }
    }
}
