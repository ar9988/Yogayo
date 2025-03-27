-- 배지 테이블에 데이터 삽입
INSERT INTO Badge (badge_id, badge_name, badge_max_lv) VALUES
                                                           (1, '어서오세요', 1),
                                                           (2, '꾸준함의 괴물', 3),
                                                           (3, '다 같이 요가', 1),
                                                           (4, '당신은 우승자!', 3),
                                                           (5, '나만의 길을 간다', 3),
                                                           (6, '요가의 달인', 3),
                                                           (7, '당신은 불상인가요?', 1);

-- 1. 처음 운동 (레벨 1)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('어서오세요', 'first_exercise.png', '첫 요가 달성', 1, 1, 1);

-- 2. 연속 운동 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('꾸준함의 괴물 I', 'consecutive_10days.png', '연속 운동 10일 달성', 1, 10, 2),
                                                                                                                        ('꾸준함의 괴물 II', 'consecutive_20days.png', '연속 운동 20일 달성', 2, 20, 2),
                                                                                                                        ('꾸준함의 괴물 III', 'consecutive_30days.png', '연속 운동 30일 달성', 3, 30, 2);

-- 3. 첫 멀티플레이 (레벨 1)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('다 같이 요가', 'first_multiplayer.png', '첫 멀티플레이', 1, 1, 3);

-- 4. 방 우승 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('당신은 우승자! I', 'room_win_1.png', '멀티플레이에서 첫 우승 달성', 1, 1, 4),
                                                                                                                        ('당신은 우승자! II', 'room_win_3.png', '멀티플레이서 우승 3회 달성', 2, 3, 4),
                                                                                                                        ('당신은 우승자! III', 'room_win_5.png', '멀티플레이서 우승 5회 달성', 3, 5, 4);

-- 5. 요가 코스 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('나만의 길을 간다 I', 'yoga_course_1.png', '첫 요가 코스 등록', 1, 1, 5),
                                                                                                                        ('나만의 길을 간다 II', 'yoga_course_3.png', '요가 코스 3개 등록', 2, 3, 5),
                                                                                                                        ('나만의 길을 간다 III', 'yoga_course_5.png', '요가 코스 5개 등록', 3, 5, 5);

-- 6. 요가의 달인 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('요가의 달인 I', 'yoga_master_70.png', '자세 정확도 70점 이상 달성', 1, 70, 6),
                                                                                                                        ('요가의 달인 II', 'yoga_master_80.png', '자세 정확도 80점 이상 달성', 2, 80, 6),
                                                                                                                        ('요가의 달인 III', 'yoga_master_90.png', '자세 정확도 90점 이상 달성', 3, 90, 6);

-- 7. 요가 유지 (레벨 1)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('당신은 불상인가요?', 'yoga_posetime_15.png', '한 자세 15초이상 유지', 1, 15, 7);