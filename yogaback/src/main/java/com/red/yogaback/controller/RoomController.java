package com.red.yogaback.controller;


import com.red.yogaback.dto.request.RoomRequest;
import com.red.yogaback.security.SecurityUtil;
import com.red.yogaback.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/multi")
@Tag(name = "방 관련 API", description = "방 생성, 조회")
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/lobby")
    @Operation(summary = "방 생성")
    public ResponseEntity<RoomRequest> createRooms(@RequestBody RoomRequest roomReq) {
        Long userId = SecurityUtil.getCurrentMemberId();
        log.info("request : {}", roomReq);
        return ResponseEntity.ok(roomService.createRooms(roomReq, userId));
    }



}
