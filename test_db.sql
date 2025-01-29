-- 데이터베이스 생성
CREATE DATABASE test_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE test_db;

-- user 테이블 생성
CREATE TABLE user (
                      email VARCHAR(100) NOT NULL COMMENT '사용자 이메일 (주 식별자)',
                      name VARCHAR(50) NOT NULL COMMENT '사용자 이름',
                      create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                      profile_img VARCHAR(200) COMMENT '프로필 이미지 URL',
                      is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
                      PRIMARY KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 정보 테이블';

-- user_search_term 테이블 생성
CREATE TABLE user_search_term (
                                  id VARCHAR(36) NOT NULL COMMENT '검색어 ID (UUID)',
                                  email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                                  word VARCHAR(100) NOT NULL COMMENT '검색어',
                                  create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                  update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                                  PRIMARY KEY (id),
                                  FOREIGN KEY (email) REFERENCES user(email) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 검색어 기록 테이블';

-- travel_taste 테이블 생성
CREATE TABLE travel_taste (
                              id VARCHAR(36) NOT NULL COMMENT '여행 취향 ID (UUID)',
                              email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                              landmark BOOLEAN NOT NULL DEFAULT FALSE COMMENT '랜드마크 선호',
                              relax BOOLEAN NOT NULL DEFAULT FALSE COMMENT '휴양 선호',
                              food BOOLEAN NOT NULL DEFAULT FALSE COMMENT '맛집 선호',
                              alone BOOLEAN NOT NULL DEFAULT FALSE COMMENT '혼자 여행',
                              romance BOOLEAN NOT NULL DEFAULT FALSE COMMENT '연인 여행',
                              friend BOOLEAN NOT NULL DEFAULT FALSE COMMENT '친구 여행',
                              child BOOLEAN NOT NULL DEFAULT FALSE COMMENT '자녀 동반',
                              parents BOOLEAN NOT NULL DEFAULT FALSE COMMENT '부모님 동반',
                              travel_days TINYINT UNSIGNED COMMENT '여행 일수',
                              options_input VARCHAR(500) COMMENT '추가 옵션',
                              create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                              PRIMARY KEY (id),
                              FOREIGN KEY (email) REFERENCES user(email) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 취향 정보 테이블';

-- url 테이블 생성
CREATE TABLE url (
                     id VARCHAR(36) NOT NULL COMMENT 'URL ID (UUID)',
                     ext_url_id VARCHAR(100) COMMENT '외부 URL ID',
                     url_title VARCHAR(200) COMMENT 'URL 제목',
                     url_author VARCHAR(100) COMMENT 'URL 작성자',
                     url VARCHAR(500) NOT NULL COMMENT 'URL 주소',
                     create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                     PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='URL 정보 테이블';

-- user_url 테이블 생성
CREATE TABLE user_url (
                          email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                          url_id VARCHAR(36) NOT NULL COMMENT 'URL ID',
                          is_use BOOLEAN NOT NULL DEFAULT TRUE COMMENT '사용 여부',
                          create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                          update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                          PRIMARY KEY (email, url_id),
                          FOREIGN KEY (email) REFERENCES user(email) ON DELETE CASCADE ON UPDATE CASCADE,
                          FOREIGN KEY (url_id) REFERENCES url(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자-URL 연결 테이블';

-- travel_info 테이블 생성
CREATE TABLE travel_info (
                             id VARCHAR(36) NOT NULL COMMENT '여행 정보 ID (UUID)',
                             email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                             travel_taste_id VARCHAR(36) COMMENT '여행 취향 ID',
                             place_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '장소 수',
                             use_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '사용 횟수',
                             title VARCHAR(100) NOT NULL COMMENT '여행 제목',
                             create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                             bookmark BOOLEAN NOT NULL DEFAULT FALSE COMMENT '북마크 여부',
                             fixed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '고정 여부',
                             is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
                             PRIMARY KEY (id),
                             FOREIGN KEY (email) REFERENCES user(email) ON DELETE CASCADE ON UPDATE CASCADE,
                             FOREIGN KEY (travel_taste_id) REFERENCES travel_taste(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 정보 테이블';

-- place 테이블 생성
CREATE TABLE place (
                       id VARCHAR(36) NOT NULL COMMENT '장소 ID (UUID)',
                       address VARCHAR(200) COMMENT '주소',
                       title VARCHAR(100) NOT NULL COMMENT '장소명',
                       description TEXT COMMENT '설명',
                       type VARCHAR(50) COMMENT '장소 유형',
                       image VARCHAR(200) COMMENT '이미지 URL',
                       score DECIMAL(3,1) CHECK (score >= 0 AND score <= 5) COMMENT '평점',
                       review_cnt INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '리뷰 수',
                       create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                       update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장소 정보 테이블';

-- guide 테이블 생성
CREATE TABLE guide (
                       id VARCHAR(36) NOT NULL COMMENT '가이드 ID (UUID)',
                       travel_info_id VARCHAR(36) NOT NULL COMMENT '여행 정보 ID',
                       course_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '코스 수',
                       use_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '사용 횟수',
                       title VARCHAR(100) NOT NULL COMMENT '가이드 제목',
                       travel_days TINYINT UNSIGNED COMMENT '여행 일수',
                       create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                       update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                       bookmark BOOLEAN NOT NULL DEFAULT FALSE COMMENT '북마크 여부',
                       fixed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '고정 여부',
                       is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
                       PRIMARY KEY (id),
                       FOREIGN KEY (travel_info_id) REFERENCES travel_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 가이드 테이블';

-- course 테이블 생성
CREATE TABLE course (
                        id VARCHAR(36) NOT NULL COMMENT '코스 ID (UUID)',
                        guide_id VARCHAR(36) NOT NULL COMMENT '가이드 ID',
                        course_number TINYINT UNSIGNED NOT NULL COMMENT '코스 순서',
                        create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                        update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                        is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
                        PRIMARY KEY (id),
                        FOREIGN KEY (guide_id) REFERENCES guide(id) ON DELETE CASCADE,
                        UNIQUE KEY uk_guide_course (guide_id, course_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='코스 정보 테이블';

-- travel_info_place 테이블 생성
CREATE TABLE travel_info_place (
                                   travel_info_id VARCHAR(36) NOT NULL COMMENT '여행 정보 ID',
                                   place_id VARCHAR(36) NOT NULL COMMENT '장소 ID',
                                   create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                   PRIMARY KEY (travel_info_id, place_id),
                                   FOREIGN KEY (travel_info_id) REFERENCES travel_info(id) ON DELETE CASCADE,
                                   FOREIGN KEY (place_id) REFERENCES place(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 정보-장소 연결 테이블';

-- url_place 테이블 생성
CREATE TABLE url_place (
                           url_id VARCHAR(36) NOT NULL COMMENT 'URL ID',
                           place_id VARCHAR(36) NOT NULL COMMENT '장소 ID',
                           create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                           PRIMARY KEY (url_id, place_id),
                           FOREIGN KEY (url_id) REFERENCES url(id) ON DELETE CASCADE,
                           FOREIGN KEY (place_id) REFERENCES place(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='URL-장소 연결 테이블';

-- course_place 테이블 생성
CREATE TABLE course_place (
                              place_id VARCHAR(36) NOT NULL COMMENT '장소 ID',
                              guide_id VARCHAR(36) NOT NULL COMMENT '가이드 ID',
                              place_num TINYINT UNSIGNED NOT NULL COMMENT '장소 순서',
                              create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                              PRIMARY KEY (place_id, guide_id),
                              FOREIGN KEY (place_id) REFERENCES place(id) ON DELETE CASCADE,
                              FOREIGN KEY (guide_id) REFERENCES guide(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='코스-장소 연결 테이블';

-- 인덱스 생성
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_travel_info_email ON travel_info(email);
CREATE INDEX idx_travel_taste_email ON travel_taste(email);
CREATE INDEX idx_guide_travel_info ON guide(travel_info_id);
CREATE INDEX idx_course_guide ON course(guide_id);
CREATE INDEX idx_place_title ON place(title);
CREATE INDEX idx_url_ext_url ON url(ext_url_id);
