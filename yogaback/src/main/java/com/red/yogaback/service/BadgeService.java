package com.red.yogaback.service;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.dto.respond.UserInfoRes;
import com.red.yogaback.model.*;
import com.red.yogaback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


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
    public List<BadgeListRes> getBadgeList(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("유저를 찾을 수 없습니다.")
        );
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
        List<Badge> badges = badgeRepository.findAll();


        return badges.stream().map((badge) -> {
            Optional<UserBadge> userBadgeOpt = userBadges.stream().filter(userBadge -> userBadge.getBadge().equals(badge))
                    .findFirst();

            int progress = userBadgeOpt.map(UserBadge::getProgress).orElse(0);
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
    public UserInfoRes getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("유저를 찾을 수 없습니다."));
        UserRecord userRecord = userRecordRepository.findByUser(user).orElseThrow(() ->
                new NoSuchElementException("유저 기록을 찾을 수 없습니다."));
        UserInfoRes userInfoRes = UserInfoRes.builder()
                .userId(userId)
                .userName(user.getUserName())
                .userNickName(user.getUserNickname())
                .userProfile(user.getUserProfile())
                .exConDays(userRecord.getExConDays())
                .exDays(userRecord.getExDays())
                .roomWin(userRecord.getRoomWin())
                .build();

        return userInfoRes;
    }

    // 새로 달성된 배지 확인 요청
    public List<BadgeListRes> getNewBadge(Long userId){
        // 1. 유저 정보 가져오기
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("유저를 찾을 수 없습니다.")
        );

        // 2. 유저가 가지고 있는 배지들 가져오기
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);

        // 3. 새로 달성한 배지들만 필터링하여 반환
        List<BadgeListRes> newBadgeList = userBadges.stream()
                .filter(UserBadge::isNew)  // isNew가 true인 배지만 필터링
                .map(userBadge -> {
                    Badge badge = userBadge.getBadge();  // 유저가 달성한 배지 정보 가져오기
                    int progress = userBadge.getProgress();  // 유저의 배지 진행도
                    int highLevel = userBadge.getHighLevel();  // 유저의 배지 단계

                    // 배지 디테일 리스트 생성
                    List<BadgeListRes.BadgeDetailRes> badgeDetailRes = badge.getBadgeDetails()
                            .stream()
                            .filter(detail -> detail.getBadgeLevel() == highLevel)  // 새로 달성한 배지인지 확인
                            .map(detail -> new BadgeListRes.BadgeDetailRes(
                                    detail.getBadgeDetailId(),
                                    detail.getBadgeDetailName(),
                                    detail.getBadgeDetailImg(),
                                    detail.getBadgeDescription(),
                                    detail.getBadgeGoal(),
                                    detail.getBadgeLevel()
                            ))
                            .collect(Collectors.toList());

                    // 만약 새로 달성한 배지가 있으면 isNew를 false로 업데이트
                    if (!badgeDetailRes.isEmpty()) {
                        userBadge.setNew(false);  // isNew를 false로 설정
                        userBadgeRepository.save(userBadge);  // 업데이트된 UserBadge 저장
                    }

                    // 새로 달성한 배지 응답 객체 반환
                    return new BadgeListRes(
                            badge.getBadgeId(),
                            badge.getBadgeName(),
                            progress,
                            highLevel,
                            badgeDetailRes
                    );
                })
                .collect(Collectors.toList());  // 리스트로 반환

        return newBadgeList;
    }




    // 배지 부여
    public void assignBadge(User user, Long badgeId, int requiredLevel, int progress) {
        Badge badge = badgeRepository.findById(badgeId).orElseThrow(
                () -> new NoSuchElementException("배지를 찾을 수 없습니다.")
        );
        UserBadge userBadge = userBadgeRepository.findByUserAndBadge(user, badge);
        if (userBadge == null) {
            UserBadge savedUserBadge = UserBadge.builder()
                    .user(user)
                    .badge(badge)
                    .highLevel(requiredLevel)
                    .isNew(true)
                    .progress(progress)
                    .createdAt(System.currentTimeMillis()).build();
            userBadgeRepository.save(savedUserBadge);
        } else if (userBadge.getHighLevel() == requiredLevel - 1) {
            userBadge.setHighLevel(requiredLevel);
            userBadge.setProgress(progress);
            userBadge.setCreatedAt(System.currentTimeMillis());
            userBadge.setNew(true);
            userBadgeRepository.save(userBadge);
        }

    }

    // 유저 기록 체크
    public void updateUserRecordAndAssignBadge(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("유저를 찾을 수 없습니다.")
        );

        UserRecord userRecord = userRecordRepository.findByUser(user).orElseThrow(
                () -> new NoSuchElementException("유저 기록을 찾을 수 없습니다.")
        );


        boolean accOverNinety = poseRecordRepository.existsByUserAndAccuracyGreaterThanEqual(user, 90);
        boolean accOverEighty = poseRecordRepository.existsByUserAndAccuracyGreaterThanEqual(user, 80);
        boolean accOverSeventy = poseRecordRepository.existsByUserAndAccuracyGreaterThanEqual(user, 70);
        boolean poseTimeOverFifty = poseRecordRepository.existsByUserAndPoseTimeGreaterThanEqual(user, 15);

        int userCoursesCount = userCourseRepository.countByUser(user);
        int roomRecordsCount = roomRecordRepository.countByUser(user);
        Long roomWinCount = userRecord.getRoomWin();
        Long exDays = userRecord.getExDays();
        Long exConDays = userRecord.getExConDays();


        if (poseTimeOverFifty) assignBadge(user, 7L, 1, 1);

        if (accOverSeventy) assignBadge(user, 6L, 1, 1);
        if (accOverEighty) assignBadge(user, 6L, 2, 1);
        if (accOverNinety) assignBadge(user, 6L, 3, 1);

        if (userCoursesCount == 1) assignBadge(user, 5L, 1, 1);
        if (userCoursesCount == 2) assignBadge(user, 5L, 1, 2);
        if (userCoursesCount == 3) assignBadge(user, 5L, 2, 3);
        if (userCoursesCount == 4) assignBadge(user, 5L, 1, 4);
        if (userCoursesCount == 5) assignBadge(user, 5L, 3, 5);

        if (roomRecordsCount == 1) assignBadge(user, 3L, 1, 1);


        if (exDays == 1) assignBadge(user, 1L, 1, userRecord.getExDays().intValue());

        if (exConDays == 10) assignBadge(user, 2L, 1, exConDays.intValue());
        if (exConDays > 10 && exConDays < 20) userRecord.setExConDays(exConDays);
        if (exConDays == 20) assignBadge(user, 2L, 2, userRecord.getExConDays().intValue());
        if (exConDays > 20 && exConDays < 30) userRecord.setExConDays(exConDays);
        if (exConDays == 30) assignBadge(user, 2L, 3, userRecord.getExConDays().intValue());

        if (roomWinCount == 1) assignBadge(user, 4L, 1, roomWinCount.intValue());
        if (roomWinCount == 2) assignBadge(user, 4L, 2, roomWinCount.intValue());
        if (roomWinCount == 3) assignBadge(user, 4L, 3, roomWinCount.intValue());


    }
}


