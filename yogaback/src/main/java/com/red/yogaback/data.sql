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
    ('어서오세요', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EC%9A%94%EA%B0%80.png', '첫 요가 달성', 1, 1, 1);

-- 2. 연속 운동 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('꾸준함의 괴물 I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+10%EC%9D%BC.png', '연속 운동 10일 달성', 1, 10, 2),
                                                                                                                        ('꾸준함의 괴물 II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+20%EC%9D%BC.png', '연속 운동 20일 달성', 2, 20, 2),
                                                                                                                        ('꾸준함의 괴물 III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+30%EC%9D%BC.png', '연속 운동 30일 달성', 3, 30, 2);

-- 3. 첫 멀티플레이 (레벨 1)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('다 같이 요가', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EB%A9%80%ED%8B%B0.png', '첫 멀티플레이', 1, 1, 3);

-- 4. 방 우승 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('당신은 우승자! I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9+%ED%9A%9F%EC%88%98+1%ED%9A%8C.png', '멀티플레이에서 첫 우승 달성', 1, 1, 4),
                                                                                                                        ('당신은 우승자! II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9%ED%9A%9F%EC%88%98+3%ED%9A%8C.png', '멀티플레이서 우승 3회 달성', 2, 3, 4),
                                                                                                                        ('당신은 우승자! III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9+%ED%9A%9F%EC%88%98+5%ED%9A%8C.png', '멀티플레이서 우승 5회 달성', 3, 5, 4);

-- 5. 요가 코스 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('나만의 길을 간다 I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+1%EA%B0%9C+%EC%83%9D%EC%84%B1.png', '첫 요가 코스 등록', 1, 1, 5),
                                                                                                                        ('나만의 길을 간다 II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+3%EA%B0%9C+%EC%83%9D%EC%84%B1.png', '요가 코스 3개 등록', 2, 3, 5),
                                                                                                                        ('나만의 길을 간다 III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+5%EA%B0%9C+%EC%83%9D%EC%84%B1.png', '요가 코스 5개 등록', 3, 5, 5);

-- 6. 요가의 달인 (레벨 1-3)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
                                                                                                                        ('요가의 달인 I', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+70%EC%9D%B4%EC%83%81.png', '자세 정확도 70점 이상 달성', 1, 70, 6),
                                                                                                                        ('요가의 달인 II', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+80%EC%9D%B4%EC%83%81.png', '자세 정확도 80점 이상 달성', 2, 80, 6),
                                                                                                                        ('요가의 달인 III', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+90%EC%9D%B4%EC%83%81.png', '자세 정확도 90점 이상 달성', 3, 90, 6);

-- 7. 요가 유지 (레벨 1)
INSERT INTO BadgeDetail (badge_detail_name, badge_detail_img, badge_description, badge_level, badge_goal, badge_id) VALUES
    ('당신은 불상인가요?', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9E%90%EC%84%B8+%EC%9C%A0%EC%A7%80+.png', '한 자세 15초이상 유지', 1, 15, 7);


INSERT INTO user (user_id, user_login_id, user_name, user_pwd, user_nickname, user_profile, created_at, modify_at) VALUES
(1, 'user1', '박성민', 'user1', '박성민 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EB%A9%80%ED%8B%B0.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
(2, 'user2', '김아름', 'user2', '김아름 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%B0%EC%8A%B9+%ED%9A%9F%EC%88%98+1%ED%9A%8C.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
(3, 'user3', '황선혁', 'user3', '황선혁 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%B2%AB+%EC%9A%94%EA%B0%80.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
(4, 'user4', '황홍법', 'user4', '황홍법 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%A0%95%ED%99%95%EB%8F%84+90%EC%9D%B4%EC%83%81.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
(5, 'user5', '김웅기', 'user5', '김웅기 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%97%B0%EC%86%8D%EC%9A%B4%EB%8F%99+10%EC%9D%BC.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()),
(6, 'user6', '경이현', 'user6', '경이현 닉', 'https://yogayo.s3.ap-northeast-2.amazonaws.com/%EC%9A%94%EA%B0%80+%EC%BD%94%EC%8A%A4+5%EA%B0%9C+%EC%83%9D%EC%84%B1.png', UNIX_TIMESTAMP(), UNIX_TIMESTAMP());



INSERT INTO UserBadge (user_badge_id, user_id, badge_id, is_new, created_at, progress, high_level) VALUES
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

