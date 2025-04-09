package com.red.yogaback.service;

import com.red.yogaback.constant.BadgeType;
import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.dto.respond.UserInfoRes;
import com.red.yogaback.model.*;
import com.red.yogaback.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final PoseRecordRepository poseRecordRepository;
    private final RoomRecordRepository roomRecordRepository;
    private final UserCourseRepository userCourseRepository;

    // 배지 목록 요청
    @Transactional
    public List<BadgeListRes> getBadgeList(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("유저를 찾을 수 없습니다.")
        );
        Optional<UserRecord> userRecord = userRecordRepository.findByUser(user);
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
        List<Badge> badges = badgeRepository.findAll();


        return badges.stream().map((badge) -> {
            Optional<UserBadge> userBadgeOpt = userBadges.stream().filter(userBadge -> userBadge.getBadge().equals(badge))
                    .findFirst();

            int progress = userBadgeOpt.map(UserBadge::getProgress).orElse(0);
            if (badge.getBadgeId() == 2L) {
                progress = userRecord.get().getExConDays().intValue();
            }
            int highLevel = userBadgeOpt.map(UserBadge::getHighLevel).orElse(0);

            List<BadgeListRes.BadgeDetailRes> badgeDetailRes = badge.getBadgeDetails()
                    .stream().map((detail) -> new BadgeListRes.BadgeDetailRes(
                            detail.getBadgeDetailId(),
                            detail.getBadgeDetailName(),
                            detail.getBadgeDetailImg(),
                            detail.getBadgeDescription(),
                            detail.getBadgeGoal(),
                            detail.getBadgeLevel()
                    )).collect(Collectors.toList());

            return new BadgeListRes(
                    badge.getBadgeId(),
                    badge.getBadgeName(),
                    progress,
                    highLevel,
                    badgeDetailRes
            );
        }).collect(Collectors.toList());
    }


    // 유저 정보 요청
    @Transactional
    public UserInfoRes getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("유저를 찾을 수 없습니다."));
        UserRecord userRecord = userRecordRepository.findByUser(user).orElse(null);

        UserInfoRes userInfoRes = UserInfoRes.builder()
                .userId(userId)
                .userName(user.getUserName())
                .userNickName(user.getUserNickname())
                .userProfile(user.getUserProfile())
                .exConDays(userRecord != null ? userRecord.getExConDays() : null)
                .exDays(userRecord != null ? userRecord.getExDays() : null)
                .roomWin(userRecord != null ? userRecord.getRoomWin() : null)
                .build();
        return userInfoRes;
    }

    // 새로 달성된 배지 확인 요청 (모든 상세 정보 포함)
    @Transactional
    public List<BadgeListRes> getNewBadge(Long userId) {
        // 1. 유저 조회
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("유저를 찾을 수 없습니다.")
        );

        // 2. 페치 조인을 통해 새로 달성한 배지와 관련 정보를 한 번에 조회
        List<UserBadge> newUserBadges = userBadgeRepository.findNewBadgesWithDetails(user);

        // 3. 배지 응답 생성
        List<BadgeListRes> newBadgeList = newUserBadges.stream()
                .map(userBadge -> {
                    Badge badge = userBadge.getBadge();
                    int progress = userBadge.getProgress();
                    int highLevel = userBadge.getHighLevel();

                    // 현재 레벨에 해당하는 배지 디테일 정보 매핑
                    List<BadgeListRes.BadgeDetailRes> badgeDetailResList = badge.getBadgeDetails().stream()
                            .filter(detail -> detail.getBadgeLevel() == highLevel)
                            .map(detail -> new BadgeListRes.BadgeDetailRes(
                                    detail.getBadgeDetailId(),
                                    detail.getBadgeDetailName(),
                                    detail.getBadgeDetailImg(),
                                    detail.getBadgeDescription(),
                                    detail.getBadgeGoal(),
                                    detail.getBadgeLevel()
                            ))
                            .collect(Collectors.toList());

                    // 새로 달성한 배지 응답 객체 반환
                    return new BadgeListRes(
                            badge.getBadgeId(),
                            badge.getBadgeName(),
                            progress,
                            highLevel,
                            badgeDetailResList
                    );
                })
                .collect(Collectors.toList());

        // 4. 조회된 모든 새 배지를 비활성화 처리 (한 번에 처리)
        if (!newUserBadges.isEmpty()) {
            newUserBadges.forEach(userBadge -> userBadge.setNew(false));
            userBadgeRepository.saveAll(newUserBadges); // 일괄 저장으로 최적화
        }

        return newBadgeList;
    }

    // 배지 부여
//    public void assignBadge(User user, Long badgeId, int requiredLevel, int progress) {
//        Badge badge = badgeRepository.findById(badgeId).orElseThrow(
//                () -> new NoSuchElementException("배지를 찾을 수 없습니다.")
//        );
//        UserBadge userBadge = userBadgeRepository.findByUserAndBadge(user, badge);
//        if (userBadge == null) {
//            UserBadge savedUserBadge = UserBadge.builder()
//                    .user(user)
//                    .badge(badge)
//                    .highLevel(requiredLevel)
//                    .isNew(true)
//                    .progress(progress)
//                    .createdAt(System.currentTimeMillis()).build();
//            userBadgeRepository.save(savedUserBadge);
//        } else if (userBadge.getHighLevel() == requiredLevel - 1) {
//            userBadge.setHighLevel(requiredLevel);
//            userBadge.setProgress(progress);
//            userBadge.setCreatedAt(System.currentTimeMillis());
//            userBadge.setNew(true);
//            userBadgeRepository.save(userBadge);
//        }
//
//    }

    /**
     * 배지 부여 또는 업데이트
     */
    @Transactional
    public UserBadge assignBadge(User user, BadgeType badgeType, int level, int progress) {
        Badge badge = badgeRepository.findById(badgeType.getId())
                .orElseThrow(() -> new NoSuchElementException("배지를 찾을 수 없습니다."));

        UserBadge userBadge = userBadgeRepository.findByUserAndBadge(user, badge);
        boolean isNewAchievement = false;

        if (userBadge == null) {
            // 신규 배지 생성
            userBadge = UserBadge.builder()
                    .user(user)
                    .badge(badge)
                    .highLevel(level)
                    .progress(progress)
                    .isNew(true)
                    .createdAt(System.currentTimeMillis())
                    .build();
            isNewAchievement = true;
        } else if (userBadge.getHighLevel() < level) {
            // 기존 배지 레벨 업데이트
            userBadge.setHighLevel(level);
            userBadge.setProgress(progress);
            userBadge.setNew(true);
            userBadge.setCreatedAt(System.currentTimeMillis());
            isNewAchievement = true;
        } else {
            // 진행 상황만 업데이트 (필요한 경우)
            if (userBadge.getProgress() < progress) {
                userBadge.setProgress(progress);
            }
        }

        return isNewAchievement ? userBadgeRepository.save(userBadge) : null;
    }


    // 유저 기록 체크

    /**
     * 유저 기록 체크 및 배지 부여
     **/
    @Transactional
    public void updateUserRecordAndAssignBadges(User user) {
        UserRecord userRecord = userRecordRepository.findByUser(user)
                .orElseThrow(() -> new NoSuchElementException("유저 기록을 찾을 수 없습니다."));

        // 1. 정확도 기반 배지 체크 - 단 한 번의 쿼리로 최고 정확도 조회
        Optional<Float> maxAccuracy = poseRecordRepository.findMaxAccuracyByUser(user);

        log.info("user : {}, maxAccuracy: {}", user, maxAccuracy);
        maxAccuracy.ifPresent(accuracy -> {
            int newAccuracy = (int) (accuracy * 100);
            log.info("newAccuracy: {}", newAccuracy);
            if (newAccuracy >= 90) {
                assignBadge(user, BadgeType.YOGA_ACCURACY, 3, newAccuracy);
            } else if (newAccuracy >= 80) {
                assignBadge(user, BadgeType.YOGA_ACCURACY, 2, newAccuracy);
            } else if (newAccuracy >= 70) {
                assignBadge(user, BadgeType.YOGA_ACCURACY, 1, newAccuracy);
            }
        });

        // 2. 포즈 유지 시간 체크
        Optional<Integer> maxPoseTime = poseRecordRepository.findMaxPoseTimeByUser(user);
        maxPoseTime.ifPresent(poseTime -> {
            if (poseTime >= 15) {
                assignBadge(user, BadgeType.YOGA_POSETIME, 1, poseTime);
            }
        });

        // 3. 요가 코스 완료 수 체크
        int coursesCount = userCourseRepository.countByUser(user);
        if (coursesCount >= 5) {
            assignBadge(user, BadgeType.YOGA_COURSES, 3, coursesCount);
        } else if (coursesCount >= 3) {
            assignBadge(user, BadgeType.YOGA_COURSES, 2, coursesCount);
        } else if (coursesCount >= 1) {
            assignBadge(user, BadgeType.YOGA_COURSES, 1, coursesCount);
        }

        // 4. 첫 멀티플레이 체크
        int roomRecordsCount = roomRecordRepository.countByUser(user);
        if (roomRecordsCount >= 1) {
            assignBadge(user, BadgeType.FIRST_MULTIPLAYER, 1, 1);
        }

        // 5. 첫 운동 체크
        Long exDays = userRecord.getExDays();
        if (exDays >= 1) {
            assignBadge(user, BadgeType.FIRST_EXERCISE, 1, exDays.intValue());
        }

        // 6. 연속 운동 일수 체크
        Long exConDays = userRecord.getExConDays();
        if (exConDays >= 30) {
            assignBadge(user, BadgeType.CONSECUTIVE_DAYS, 3, exConDays.intValue());
        } else if (exConDays >= 20) {
            assignBadge(user, BadgeType.CONSECUTIVE_DAYS, 2, exConDays.intValue());
        } else if (exConDays >= 10) {
            assignBadge(user, BadgeType.CONSECUTIVE_DAYS, 1, exConDays.intValue());
        }

        // 7. 방 우승 수 체크
        Long roomWinCount = userRecord.getRoomWin();
        if (roomWinCount >= 5) {
            assignBadge(user, BadgeType.ROOM_WINS, 3, roomWinCount.intValue());
        } else if (roomWinCount >= 3) {
            assignBadge(user, BadgeType.ROOM_WINS, 2, roomWinCount.intValue());
        } else if (roomWinCount >= 1) {
            assignBadge(user, BadgeType.ROOM_WINS, 1, roomWinCount.intValue());
        }
    }
//    public void updateUserRecordAndAssignBadge(Long userId) {
//        User user = userRepository.findById(userId).orElseThrow(
//                () -> new NoSuchElementException("유저를 찾을 수 없습니다.")
//        );
//
//        UserRecord userRecord = userRecordRepository.findByUser(user).orElseThrow(
//                () -> new NoSuchElementException("유저 기록을 찾을 수 없습니다.")
//        );
//
//
//        boolean accOverNinety = poseRecordRepository.existsByUserAndAccuracyGreaterThanEqual(user, 90);
//        boolean accOverEighty = poseRecordRepository.existsByUserAndAccuracyGreaterThanEqual(user, 80);
//        boolean accOverSeventy = poseRecordRepository.existsByUserAndAccuracyGreaterThanEqual(user, 70);
//        boolean poseTimeOverFifty = poseRecordRepository.existsByUserAndPoseTimeGreaterThanEqual(user, 15);
//
//        int userCoursesCount = userCourseRepository.countByUser(user);
//        int roomRecordsCount = roomRecordRepository.countByUser(user);
//        Long roomWinCount = userRecord.getRoomWin();
//        Long exDays = userRecord.getExDays();
//        Long exConDays = userRecord.getExConDays();
//
//
//        if (poseTimeOverFifty) assignBadge(user, 7L, 1, 1);
//
//        if (accOverSeventy) assignBadge(user, 6L, 1, 1);
//        if (accOverEighty) assignBadge(user, 6L, 2, 1);
//        if (accOverNinety) assignBadge(user, 6L, 3, 1);
//
//        if (userCoursesCount == 1) assignBadge(user, 5L, 1, 1);
//        if (userCoursesCount == 2) assignBadge(user, 5L, 1, 2);
//        if (userCoursesCount == 3) assignBadge(user, 5L, 2, 3);
//        if (userCoursesCount == 4) assignBadge(user, 5L, 1, 4);
//        if (userCoursesCount == 5) assignBadge(user, 5L, 3, 5);
//
//        if (roomRecordsCount == 1) assignBadge(user, 3L, 1, 1);
//
//
//        if (exDays == 1) assignBadge(user, 1L, 1, userRecord.getExDays().intValue());
//
//        if (exConDays == 10) assignBadge(user, 2L, 1, exConDays.intValue());
//        if (exConDays > 10 && exConDays < 20) userRecord.setExConDays(exConDays);
//        if (exConDays == 20) assignBadge(user, 2L, 2, userRecord.getExConDays().intValue());
//        if (exConDays > 20 && exConDays < 30) userRecord.setExConDays(exConDays);
//        if (exConDays == 30) assignBadge(user, 2L, 3, userRecord.getExConDays().intValue());
//
//        if (roomWinCount == 1) assignBadge(user, 4L, 1, roomWinCount.intValue());
//        if (roomWinCount == 2) assignBadge(user, 4L, 2, roomWinCount.intValue());
//        if (roomWinCount == 3) assignBadge(user, 4L, 3, roomWinCount.intValue());
//
//
//    }
}


