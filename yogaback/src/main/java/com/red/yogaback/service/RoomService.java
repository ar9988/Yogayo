package com.red.yogaback.service;

import com.red.yogaback.dto.request.RoomRequest;
import com.red.yogaback.model.Pose;
import com.red.yogaback.model.Room;
import com.red.yogaback.model.RoomCoursePose;
import com.red.yogaback.model.User;
import com.red.yogaback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;



@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomRecordRepository roomRecordRepository;
    private final UserCourseCache userCourseCache;
    private final RoomCoursePoseRepository roomCoursePoseRepository;
    private final PoseRepository poseRepository;


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
                .isPassword(roomReq.isHasPassword())
                .createdAt(System.currentTimeMillis())
                .roomState(1L)
                .build();

        Room savedRoom = roomRepository.save(room);
        userCourseCache.storeUserCourse(savedRoom.getRoomId(), roomReq.getUserCourse());

        List<RoomCoursePose> roomCoursePoses = new ArrayList<>();
        int orderIndex = 1;
        for (RoomRequest.UserCourseRequest.PoseDetail poseDetail : roomReq.getUserCourse().getPoses()) {
            Pose findPose = poseRepository.findById(poseDetail.getPoseId()).orElseThrow(() -> new NoSuchElementException("포즈를 찾을 수 없습니다."));
            RoomCoursePose roomCoursePose = RoomCoursePose.builder()
                    .room(savedRoom)
                    .pose(findPose)
                    .roomOrderIndex(orderIndex++)
                    .createdAt(System.currentTimeMillis())
                    .build();
            roomCoursePoses.add(roomCoursePose);
        }

        roomCoursePoseRepository.saveAll(roomCoursePoses);
        roomReq.setRoomId(savedRoom.getRoomId());
        roomReq.setUserNickname(user.getUserNickname());
        return roomReq;

    }


}
