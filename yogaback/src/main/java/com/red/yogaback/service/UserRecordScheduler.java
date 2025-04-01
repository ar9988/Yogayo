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
     * 매일 00:01에 실행하여 각 UserRecord의 연속 운동 일수를 업데이트합니다.
     * - 만약 lastExerciseDate가 어제 날짜와 일치하면 exConDays를 1 증가
     * - 그렇지 않으면 exConDays를 0으로 초기화
     */
    @Scheduled(cron = "0 6 17 * * *")
    public void updateConsecutiveExerciseDays() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<UserRecord> userRecords = userRecordRepository.findAll();
        for (UserRecord record : userRecords) {
            if (record.getLastExerciseDate() != null && record.getLastExerciseDate().equals(yesterday)) {
                record.setExConDays(record.getExConDays() + 1);
            } else {
                record.setExConDays(0L);
            }
            userRecordRepository.save(record);
            log.info("Updated exConDays for userId {}: exConDays = {}", record.getUser().getUserId(), record.getExConDays());
        }
    }
}
