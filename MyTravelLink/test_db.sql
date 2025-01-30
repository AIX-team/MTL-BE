USE test_db;

-- 문자셋 설정
SET NAMES utf8mb4;

-- user 테이블
CREATE TABLE user (
                      email VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '사용자 이메일',
                      name VARCHAR(50) NOT NULL COMMENT '사용자 이름',
                      dob DATE NOT NULL COMMENT '생년월일',
                      gender TINYINT NOT NULL COMMENT '성별',
                      create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                      profile_img VARCHAR(255) COMMENT '프로필 이미지 URL',
                      is_delete TINYINT(1) DEFAULT 0 NOT NULL COMMENT '삭제여부',
                      INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보';

-- user_search_term 테이블
CREATE TABLE user_search_term (
                                  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '검색어 ID',
                                  email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                                  word VARCHAR(100) NOT NULL COMMENT '검색어',
                                  create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                                  update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일시',
                                  FOREIGN KEY fk_search_email (email) REFERENCES user(email),
                                  INDEX idx_search_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 검색어';

-- user_url 테이블 (ID 컬럼 추가 후 기본 키 설정)
CREATE TABLE user_url (
                          id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'URL ID',
                          email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                          url VARCHAR(255) NOT NULL COMMENT 'URL',
                          create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                          update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일시',
                          is_use TINYINT(1) DEFAULT 1 NOT NULL COMMENT '사용여부',
                          FOREIGN KEY fk_url_email (email) REFERENCES user(email),
                          INDEX idx_url_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 URL';

-- travel_taste 테이블
CREATE TABLE travel_taste (
                              id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '여행취향 ID',
                              email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                              landmark TINYINT(1) DEFAULT 0 NOT NULL COMMENT '랜드마크 선호',
                              relax TINYINT(1) DEFAULT 0 NOT NULL COMMENT '휴식 선호',
                              food TINYINT(1) DEFAULT 0 NOT NULL COMMENT '음식 선호',
                              alone TINYINT(1) DEFAULT 0 NOT NULL COMMENT '혼자여행 선호',
                              romance TINYINT(1) DEFAULT 0 NOT NULL COMMENT '로맨스 선호',
                              friend TINYINT(1) DEFAULT 0 NOT NULL COMMENT '친구여행 선호',
                              child TINYINT(1) DEFAULT 0 NOT NULL COMMENT '자녀동반 선호',
                              parents TINYINT(1) DEFAULT 0 NOT NULL COMMENT '부모동반 선호',
                              travel_days INT NOT NULL COMMENT '여행일수',
                              options_input VARCHAR(255) NOT NULL COMMENT '추가입력옵션',
                              create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                              FOREIGN KEY fk_taste_email (email) REFERENCES user(email),
                              INDEX idx_taste_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 취향';

-- ext_place_list 테이블
CREATE TABLE ext_place_list (
                                id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '장소목록 ID',
                                place_list TEXT NOT NULL COMMENT '장소목록',
                                place_count INT NOT NULL DEFAULT 0 COMMENT '장소수',
                                use_count INT NOT NULL DEFAULT 0 COMMENT '사용수',
                                create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                                INDEX idx_place_count (place_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='추출 장소 목록';

-- travel_info 테이블 (fixed 컬럼 추가)
CREATE TABLE travel_info (
                             id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '여행정보 ID',
                             email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                             ext_place_list_id VARCHAR(36) NOT NULL COMMENT '외부장소목록 ID',
                             travel_taste_id VARCHAR(36) NOT NULL COMMENT '여행취향 ID',
                             place_count INT NOT NULL DEFAULT 0 COMMENT '장소수',
                             use_count INT NOT NULL DEFAULT 0 COMMENT '사용수',
                             title VARCHAR(100) NOT NULL COMMENT '제목',
                             create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                             bookmark TINYINT(1) DEFAULT 0 NOT NULL COMMENT '북마크여부',
                             fixed TINYINT(1) DEFAULT 0 NOT NULL COMMENT '고정 여부',
                             is_delete TINYINT(1) DEFAULT 0 NOT NULL COMMENT '삭제여부',
                             FOREIGN KEY fk_info_email (email) REFERENCES user(email),
                             FOREIGN KEY fk_info_place (ext_place_list_id) REFERENCES ext_place_list(id),
                             FOREIGN KEY fk_info_taste (travel_taste_id) REFERENCES travel_taste(id),
                             INDEX idx_info_email (email),
                             INDEX idx_info_place (ext_place_list_id),
                             INDEX idx_info_taste (travel_taste_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='여행 정보';

-- guide 테이블 (fixed 컬럼 추가)
CREATE TABLE guide (
                       id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '가이드 ID',
                       travel_info_id VARCHAR(36) NOT NULL COMMENT '여행정보 ID',
                       course_count INT NOT NULL DEFAULT 0 COMMENT '코스수',
                       use_count INT NOT NULL DEFAULT 0 COMMENT '사용수',
                       create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                       update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일시',
                       title VARCHAR(100) NOT NULL COMMENT '제목',
                       travel_days INT NOT NULL COMMENT '여행일수',
                       bookmark TINYINT(1) DEFAULT 0 NOT NULL COMMENT '북마크여부',
                       fixed TINYINT(1) DEFAULT 0 NOT NULL COMMENT '고정 여부',
                       is_delete TINYINT(1) DEFAULT 0 NOT NULL COMMENT '삭제여부',
                       FOREIGN KEY fk_guide_info (travel_info_id) REFERENCES travel_info(id),
                       INDEX idx_guide_info (travel_info_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가이드';

-- guide_course_info 테이블
CREATE TABLE guide_course_info (
                                   id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '코스정보 ID',
                                   guide_id VARCHAR(36) NOT NULL COMMENT '가이드 ID',
                                   course_number INT NOT NULL COMMENT '코스번호',
                                   place_number INT NOT NULL COMMENT '장소번호',
                                   place_list TEXT NOT NULL COMMENT '장소목록',
                                   create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                                   update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일시',
                                   is_delete TINYINT(1) DEFAULT 0 NOT NULL COMMENT '삭제여부',
                                   FOREIGN KEY fk_course_guide (guide_id) REFERENCES guide(id),
                                   INDEX idx_course_guide (guide_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가이드 코스 정보';

-- user_ext_place_list 테이블
CREATE TABLE user_ext_place_list (
                                     id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
                                     email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
                                     ext_place_list_id VARCHAR(36) NOT NULL COMMENT '외부장소목록 ID',
                                     create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시',
                                     is_delete TINYINT(1) DEFAULT 0 NOT NULL COMMENT '삭제여부',
                                     FOREIGN KEY fk_user_place_email (email) REFERENCES user(email),
                                     FOREIGN KEY fk_user_place_list (ext_place_list_id) REFERENCES ext_place_list(id),
                                     INDEX idx_user_place_email (email),
                                     INDEX idx_user_place_list (ext_place_list_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 외부 장소 목록';

#  추가 사항
ALTER TABLE user DROP gender;
ALTER TABLE user DROP dob;

ALTER TABLE user_search_term DROP FOREIGN KEY user_search_term_ibfk_1;
ALTER TABLE user_url DROP FOREIGN KEY user_url_ibfk_1;
ALTER TABLE travel_taste DROP FOREIGN KEY travel_taste_ibfk_1;
ALTER TABLE travel_info DROP FOREIGN KEY travel_info_ibfk_1;
ALTER TABLE user_ext_place_list DROP FOREIGN KEY user_ext_place_list_ibfk_1;


ALTER TABLE user MODIFY COLUMN email VARCHAR(100) NOT NULL;
ALTER TABLE user_search_term
    ADD CONSTRAINT user_search_term_ibfk_1 FOREIGN KEY (email) REFERENCES user(email);
