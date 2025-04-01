package com.red.yogaback.service;

import com.red.yogaback.dto.request.RoomRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//@Service
//public class UserCourseCache {
//
//    private final Map<Long, RoomRequest.PoseDetail> roomUserCourseMap = new ConcurrentHashMap<>();
//
//    public void storeUserCourse(Long roomId, RoomRequest.PoseDetail poseDetail){
//        roomUserCourseMap.put(roomId,poseDetail);
//    }
//
//    public RoomRequest.PoseDetail getPoseDetail(Long roomId) {
//        return roomUserCourseMap.get(roomId);
//    }
//
//    public void removeUserCourse(Long roomId){
//        roomUserCourseMap.remove(roomId);
//    }
//}
