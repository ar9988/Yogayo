package com.red.yogaback.service;

import com.red.yogaback.dto.request.RoomRequest;
import com.red.yogaback.model.Pose;
import com.red.yogaback.model.Room;
import com.red.yogaback.model.RoomCoursePose;
import com.red.yogaback.model.User;
import com.red.yogaback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomRecordRepository roomRecordRepository;
    private final RoomCoursePoseRepository roomCoursePoseRepository;
    private final PoseRepository poseRepository;
    private final SseEmitterService sseEmitterService;
    Map<Long, List<RoomRequest.PoseDetail>> roomPoseMap = new ConcurrentHashMap<>();

    // 방 만들기
    public RoomRequest createRooms(RoomRequest roomReq, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("유저를 찾을 수 없습니다.")
        );

        Room room = Room.builder()
                .creator(user)
                .password(roomReq.getPassword())
                .roomName(roomReq.getRoomName())
                .roomMax(roomReq.getRoomMax())
                .hasPassword(roomReq.isHasPassword())
                .createdAt(System.currentTimeMillis())
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
        roomReq.setUserNickname(user.getUserNickname());
        roomPoseMap.put(savedRoom.getRoomId(), roomReq.getPose());
        sseEmitterService.notifyRoomUpdate(getAllRooms());
        return roomReq;

    }

    // 방 조회 / SSE 연결
    public List<RoomRequest> getAllRooms() {
        List<Room> allRooms = roomRepository.findAll();
        if (allRooms.isEmpty()){
            return new ArrayList<>();
        }
        return allRooms.stream().filter(room ->
                room.getRoomState() == 1).map(room -> {
            RoomRequest roomRequest = new RoomRequest();
            roomRequest.setRoomId(room.getRoomId());
            roomRequest.setRoomCount(room.getRoomCount());
            roomRequest.setRoomMax(room.getRoomMax());
            roomRequest.setUserNickname(room.getCreator().getUserNickname());
            roomRequest.setRoomName(room.getRoomName());
            roomRequest.setHasPassword(room.isHasPassword());

            List<RoomRequest.PoseDetail> poseDetails = roomPoseMap.getOrDefault(room.getRoomId(), new ArrayList<>());
            roomRequest.setPose(poseDetails);

            return roomRequest;

        }).collect(Collectors.toList());
    }


}
