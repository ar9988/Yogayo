-- 배지 테이블에 데이터 삽입
INSERT INTO badge (badge_id, badge_name, badge_max_lv) VALUES
                                                           (1, '어서오세요', 1),
                                                           (2, '꾸준함의 괴물', 3),
                                                           (3, '다 같이 요가', 1),
                                                           (4, '당신은 우승자!', 3),
                                                           (5, '나만의 길을 간다', 3),
                                                           (6, '요가의 달인', 3),
                                                           (7, '당신은 불상인가요?', 1);

-- 1. 처음 운동 (레벨 1)
INSERT INTO badge_detail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('어서오세요', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EC%9A%94%EA%B0%80.png', '첫 요가 달성', 1, 1, 1);

-- 2. 연속 운동 (레벨 1-3)
INSERT INTO badge_detail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('꾸준함의 괴물 I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+10%EC%9D%BC.png', '연속 운동 10일 달성', 1, 10, 2),
                                                                                                                        ('꾸준함의 괴물 II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+20%EC%9D%BC.png', '연속 운동 20일 달성', 2, 20, 2),
                                                                                                                        ('꾸준함의 괴물 III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+30%EC%9D%BC.png', '연속 운동 30일 달성', 3, 30, 2);

-- 3. 첫 멀티플레이 (레벨 1)
INSERT INTO badge_detail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('다 같이 요가', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EB%A9%80%ED%8B%B0.png', '첫 멀티플레이', 1, 1, 3);

-- 4. 방 우승 (레벨 1-3)
INSERT INTO badge_detail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('당신은 우승자! I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9+%ED%9A%9F%EC%88%98+1%ED%9A%8C.png', '멀티플레이에서 첫 우승 달성', 1, 1, 4),
                                                                                                                        ('당신은 우승자! II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9%ED%9A%9F%EC%88%98+3%ED%9A%8C.png', '멀티플레이서 우승 3회 달성', 2, 3, 4),
                                                                                                                        ('당신은 우승자! III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9+%ED%9A%9F%EC%88%98+5%ED%9A%8C.png', '멀티플레이서 우승 5회 달성', 3, 5, 4);

-- 5. 요가 코스 (레벨 1-3)
INSERT INTO badge_detail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('나만의 길을 간다 I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+1%EA%B0%9C+%EC%83%9D%EC%84%B1.png', '첫 요가 코스 등록', 1, 1, 5),
                                                                                                                        ('나만의 길을 간다 II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+3%EA%B0%9C+%EC%83%9D%EC%84%B1.png', '요가 코스 3개 등록', 2, 3, 5),
                                                                                                                        ('나만의 길을 간다 III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+5%EA%B0%9C+%EC%83%9D%EC%84%B1.png', '요가 코스 5개 등록', 3, 5, 5);

-- 6. 요가의 달인 (레벨 1-3)
INSERT INTO badge_detail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('요가의 달인 I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+70%EC%9D%B4%EC%83%81.png', '자세 정확도 70점 이상 달성', 1, 70, 6),
                                                                                                                        ('요가의 달인 II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+80%EC%9D%B4%EC%83%81.png', '자세 정확도 80점 이상 달성', 2, 80, 6),
                                                                                                                        ('요가의 달인 III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+90%EC%9D%B4%EC%83%81.png', '자세 정확도 90점 이상 달성', 3, 90, 6);

-- 7. 요가 유지 (레벨 1)
INSERT INTO badge_detail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('당신은 불상인가요?', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9E%90%EC%84%B8+%EC%9C%A0%EC%A7%80+.png', '한 자세 15초이상 유지', 1, 15, 7);


-- INSERT INTO user (user_id, user_login_id, user_name, user_pwd, user_nickname, user_profile, created_at, modify_at) VALUES
-- --(1, 'user1', '박성민', 'user1', '박성민 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EB%A9%80%ED%8B%B0.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
-- --(2, 'user2', '김아름', 'user2', '김아름 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9+%ED%9A%9F%EC%88%98+1%ED%9A%8C.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
-- --(3, 'user3', '황선혁', 'user3', '황선혁 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EC%9A%94%EA%B0%80.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
-- --(4, 'user4', '황홍법', 'user4', '황홍법 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+90%EC%9D%B4%EC%83%81.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
-- --(5, 'user5', '김웅기', 'user5', '김웅기 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+10%EC%9D%BC.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
-- --(6, 'user6', '경이현', 'user6', '경이현 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+5%EA%B0%9C+%EC%83%9D%EC%84%B1.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP());


-- --INSERT INTO user_record (
-- --    user_record_id, user_id, room_win, ex_days, ex_con_days,
-- --    create_at
-- --) VALUES (
-- --    1, 6, 4, 2, 30,
-- --    UNIX_TIMESTAMP()
-- --);



INSERT INTO user_badge (user_badge_id, user_id, badge_id, is_new, created_at, progress, high_level) VALUES
-- User 1 (박성민) has 2 badges
(1, 1, 1, 1, UNIX_TIMESTAMP(), 1, 1),
(2, 1, 2, 0, UNIX_TIMESTAMP(), 20, 2),

-- User 2 (김아름) has 3 badges
(3, 2, 2, 0, UNIX_TIMESTAMP(), 30, 3),
(4, 2, 4, 1, UNIX_TIMESTAMP(), 1, 1),
(5, 2, 6, 0, UNIX_TIMESTAMP(), 70, 1),

-- User 3 (황선혁) has 1 badge
(6, 3, 1, 0, UNIX_TIMESTAMP(), 1, 1),

-- User 4 (황홍법) has 2 badges
(7, 4, 3, 0, UNIX_TIMESTAMP(), 1, 1),
(8, 4, 6, 1, UNIX_TIMESTAMP(), 90, 3),

-- User 5 (김웅기) has 4 badges
(9, 5, 2, 0, UNIX_TIMESTAMP(), 20, 2),
(10, 5, 3, 0, UNIX_TIMESTAMP(), 100, 1),
(11, 5, 5, 1, UNIX_TIMESTAMP(), 3, 2),
(12, 5, 7, 0, UNIX_TIMESTAMP(), 15, 1),

-- User 6 (경이현) has 1 badge
(13, 6, 3, 0, UNIX_TIMESTAMP(), 1, 1);


-- 포즈 넣기
INSERT INTO pose (
    pose_name,
    pose_description,
    pose_img,
    pose_level,
    pose_video,
    pose_animation,
    set_pose_id
) VALUES (
    '부장가사나',
    '엎드린 상태에서 손바닥을 어깨 아래에 두고,\n상체를 들어 올려 척추를 뒤로 젖히는 자세입니다.',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/Bhujangasana.png',
    1,
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20250331_224936484.mp4',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EB%B6%80%EC%9E%A5%EA%B0%80%EC%82%AC%EB%82%98.gif',
    null
);

INSERT INTO pose (
    pose_name,
    pose_description,
    pose_img,
    pose_level,
    pose_video,
    pose_animation,
    set_pose_id
) VALUES (
    '아도 무카 스바나아사나',
    '엎드린 상태에서 엉덩이를 들어 올려\n몸이 역 V자 모양이 되도록 하는 자세입니다.',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/AdhoMukhaSvanasana.png',
    1,
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20250331_224933984.mp4',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%95%84%EB%8F%84_%EB%AC%B4%EC%B9%B4_%EC%8A%A4%EB%B0%94%EB%82%98%EC%95%84%EC%82%AC%EB%82%98.gif',
    null
);

INSERT INTO pose (
    pose_name,
    pose_description,
    pose_img,
    pose_level,
    pose_video,
    pose_animation,
    set_pose_id
) VALUES (
    '우스트라사나',
    '무릎을 꿇고 앉은 상태에서\n상체를 뒤로 젖혀 손으로 발뒤꿈치를 잡는 자세입니다.',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/Ustrasana.png',
    2,
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20250331_225013682.mp4',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%A4%ED%8A%B8%EB%9D%BC%EC%82%AC%EB%82%98.gif',
    null
);

INSERT INTO pose (
    pose_name,
    pose_description,
    pose_img,
    pose_level,
    pose_video,
    pose_animation,
    set_pose_id
) VALUES (
    '비라바드라사나 2',
    '다리를 옆으로 벌리고 서서 한쪽 무릎을 굽히고,\n양팔을 옆으로 뻗어 시선을 앞쪽 손끝을 향하는 자세입니다.',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/Virabhadrasana_two.png',
    2,
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20250331_224959220.mp4',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EB%B9%84%EB%9D%BC%EB%B0%94%EB%93%9C%EB%9D%BC%EC%82%AC%EB%82%98_2.gif',
    null
);

INSERT INTO pose (
    pose_name,
    pose_description,
    pose_img,
    pose_level,
    pose_video,
    pose_animation,
    set_pose_id
) VALUES (
    '나바사나',
    '앉은 상태에서 다리를 들어 올리고\n상체를 뒤로 기울여 몸이 V 자 모양이 되도록 하는 자세입니다.',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/Navasana.png',
    3,
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20250331_224954611.mp4',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EB%82%98%EB%B0%94%EC%82%AC%EB%82%98.gif',
    null
);

INSERT INTO pose (
    pose_name,
    pose_description,
    pose_img,
    pose_level,
    pose_video,
    pose_animation,
    set_pose_id
) VALUES (
    '비라바드라사나 3',
    '한 다리로 서서 몸을 앞으로 기울이고,\n다른 다리를 뒤로 들어 올려\n몸이 T 자 모양이 되도록 하는 자세입니다.',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/Virabhadrasana_three.png',
    3,
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20250331_224939712.mp4',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EB%B9%84%EB%9D%BC%EB%B0%94%EB%93%9C%EB%9D%BC%EC%82%AC%EB%82%98_3.gif',
    null
);


INSERT INTO pose (
    pose_name,
    pose_description,
    pose_img,
    pose_level,
    pose_video,
    pose_animation,
    set_pose_id
) VALUES (
    '할라사나',
    '등을 대고 누운 상태에서 다리를 들어 올린 뒤,\n복부의 힘을 이용해 다리를 머리 위로 넘겨 바닥에 닿도록 합니다.\n팔은 바닥에 붙이고 손바닥을 아래로 향하게 두거나,\n손을 등 뒤로 깍지 껴 지지해도 됩니다.\n다리는 곧게 펴는 것이 이상적이며,\n발끝이 바닥에 닿지 않아도 괜찮습니다.',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/Halasana.png',
    3,
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20250331_224942626.mp4',
    'https://yogayo.s3.ap-northeast-2.amazonaws.com/%ED%95%A0%EB%9D%BC%EC%82%AC%EB%82%98.gif',
    null
);