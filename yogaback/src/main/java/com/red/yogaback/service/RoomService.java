package com.red.yogaback.service;

import com.red.yogaback.dto.request.RoomEnterReq;
import com.red.yogaback.dto.request.RoomRequest;
import com.red.yogaback.model.Pose;
import com.red.yogaback.model.Room;
import com.red.yogaback.model.RoomCoursePose;
import com.red.yogaback.model.User;
import com.red.yogaback.repository.*;
import com.red.yogaback.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomCoursePoseRepository roomCoursePoseRepository;
    private final PoseRepository poseRepository;
    private final SseEmitterService sseEmitterService;
    private final CacheManager cacheManager;
//
    Map<Long, List<RoomRequest.PoseDetail>> roomPoseMap = new ConcurrentHashMap<>();

    // 방 만들기
    public RoomRequest createRooms(RoomRequest roomReq, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("유저를 찾을 수 없습니다.")
        );

        Room room = Room.builder()
                .creatorId(user.getUserId())
                .password(roomReq.getPassword())
                .roomName(roomReq.getRoomName())
                .roomMax(roomReq.getRoomMax())
                .hasPassword(roomReq.isHasPassword())
                .createdAt(System.currentTimeMillis())
                .roomCount(0)
                .roomState(1L)
                .build();

        Room savedRoom = roomRepository.save(room);
//        userCourseCache.storeUserCourse(savedRoom.getRoomId(), roomReq.getPose());

        List<RoomCoursePose> roomCoursePoses = new ArrayList<>();
        for (RoomRequest.PoseDetail poseDetail : roomReq.getPose()) {
            Pose findPose = poseRepository.findById(poseDetail.getPoseId()).orElseThrow(() -> new NoSuchElementException("포즈를 찾을 수 없습니다."));
            poseDetail.setPoseId(findPose.getPoseId());
            poseDetail.setPoseName(findPose.getPoseName());
            poseDetail.setPoseImg(findPose.getPoseImg());
            poseDetail.setPoseDescription(findPose.getPoseDescription());
            poseDetail.setPoseVideo(findPose.getPoseVideo());
            poseDetail.setPoseLevel(findPose.getPoseLevel());
            poseDetail.setSetPoseId(1);
            poseDetail.setPoseAnimation(findPose.getPoseAnimation());
            RoomCoursePose roomCoursePose = RoomCoursePose.builder()
                    .room(savedRoom)
                    .pose(findPose)
                    .roomOrderIndex(poseDetail.getUserOrderIndex())
                    .createdAt(System.currentTimeMillis())
                    .build();
            roomCoursePoses.add(roomCoursePose);
        }

        roomCoursePoseRepository.saveAll(roomCoursePoses);
        roomReq.setRoomId(savedRoom.getRoomId());
        roomReq.setUserId(user.getUserId());
        roomReq.setUserNickname(user.getUserNickname());
        roomPoseMap.put(savedRoom.getRoomId(), roomReq.getPose());
//        cashingRoomPoses(room.getRoomId(), roomReq.getPose());
        sseEmitterService.notifyRoomUpdate(getAllRooms(""));
        return roomReq;

    }

//    @CachePut(value = "roomPoses", key = "#roomId")
//    private static List<RoomRequest.PoseDetail> cashingRoomPoses(Long roomId, List<RoomRequest.PoseDetail> poses) {
//        return poses;
//    }

    // 방 조회 / SSE 연결
    public List<RoomRequest> getAllRooms(String roomName) {
//        log.info("현재 방, 포즈: {}", roomPoseMap);
        List<Room> allRooms = roomRepository.findByRoomNameContaining(roomName);
        if (allRooms.isEmpty()) {
            return new ArrayList<>();
        }
        return allRooms.stream().filter(room ->
                room.getRoomState() == 1L).map(room -> {
            User user = userRepository.findById(room.getCreatorId()).orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));
            RoomRequest roomRequest = new RoomRequest();
            roomRequest.setRoomId(room.getRoomId());
            roomRequest.setRoomCount(room.getRoomCount());
            roomRequest.setRoomMax(room.getRoomMax());
            roomRequest.setUserId(user.getUserId());
            roomRequest.setUserNickname(user.getUserNickname());
            roomRequest.setRoomName(room.getRoomName());
            roomRequest.setHasPassword(room.getHasPassword());

            List<RoomRequest.PoseDetail> poseDetails = roomPoseMap.getOrDefault(room.getRoomId(), new ArrayList<>());
            roomRequest.setPose(poseDetails);

//            roomRequest.setPose(roomPoseMap.get(room.getRoomId()));
            log.info("roomPoseMap: {}",roomPoseMap);

            return roomRequest;

        }).collect(Collectors.toList());
    }

    // 캐시에서 포즈 꺼내오기
    public List<RoomRequest.PoseDetail> getRoomPosesFromCache(Long roomId) {
        Cache cache = cacheManager.getCache("roomPoses");
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(roomId);
            if (valueWrapper != null) {
                return (List<RoomRequest.PoseDetail>) valueWrapper.get();
            }
        }
        return new ArrayList<>();
    }


    // 방 입장
    @Transactional
    public boolean enterRoom(RoomEnterReq roomEnterReq) {
        Long userId = SecurityUtil.getCurrentMemberId();
        User findUser = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));
        Room findRoom = roomRepository.findById(roomEnterReq.getRoomId()).orElseThrow(() -> new NoSuchElementException("방을 찾을 수 없습니다."));
        if (findRoom.getPassword().equals(roomEnterReq.getPassword())) {
            findUser.setRoom(findRoom);
            findRoom.setRoomCount(findRoom.getRoomCount() + 1);
            sseEmitterService.notifyRoomUpdate(getAllRooms(""));
        } else {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return true;
    }

}
