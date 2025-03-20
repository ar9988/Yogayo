package com.red.yogaback.service;

import com.red.yogaback.dto.respond.PoseListRes;
import com.red.yogaback.model.Pose;
import com.red.yogaback.repository.PoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoseService {

    private final PoseRepository poseRepository;

    /**
     * 모든 요가 포즈 목록을 조회합니다.
     * @return 요가 포즈 DTO 목록
     */
    public List<PoseListRes> getAllPoses() {
        List<Pose> poses = poseRepository.findAll();

        // 엔티티 목록을 DTO 목록으로 변환
        return poses.stream()
                .map(PoseListRes::fromEntity)
                .collect(Collectors.toList());
    }
}
