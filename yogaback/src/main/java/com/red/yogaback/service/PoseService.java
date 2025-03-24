package com.red.yogaback.service;

import com.red.yogaback.dto.respond.PoseRes;
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
    public List<PoseRes> getAllPoses() {
        List<Pose> poses = poseRepository.findAll();

        // 엔티티 목록을 DTO 목록으로 변환
        return poses.stream()
                .map(PoseRes::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID의 요가 포즈를 조회합니다.
     * @param poseId 조회할 포즈 ID
     * @return 요가 포즈 DTO
     */
    public PoseRes getPoseById(Long poseId) {
        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 포즈를 찾을 수 없습니다: " + poseId));

        return PoseRes.fromEntity(pose);
    }
}
