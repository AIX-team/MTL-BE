-- 새 데이터베이스 생성 및 사용
CREATE DATABASE IF NOT EXISTS test_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE test_db;

-- 의존관계가 있는 테이블들을 삭제 (삭제 순서는 외래키 제약을 고려)
DROP TABLE IF EXISTS user_url;
DROP TABLE IF EXISTS user_search_term;
DROP TABLE IF EXISTS url_place;
DROP TABLE IF EXISTS travel_info_url;
DROP TABLE IF EXISTS travel_info_place;
DROP TABLE IF EXISTS course_place;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS guide;
DROP TABLE IF EXISTS travel_info;
DROP TABLE IF EXISTS place;
DROP TABLE IF EXISTS url;
DROP TABLE IF EXISTS users;

-- 1. 사용자 정보 테이블 (독립 테이블)
CREATE TABLE users (
  email varchar(255) NOT NULL COMMENT '사용자 이메일 (주 식별자)',
  name varchar(50) NOT NULL COMMENT '사용자 이름',
  profile_img varchar(500) DEFAULT NULL COMMENT '프로필 이미지 URL',
  is_delete tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='사용자 정보 테이블';

-- 2. URL 정보 테이블 (독립 테이블)
CREATE TABLE url (
  id varchar(128) NOT NULL COMMENT 'URL ID (SHA-512 해시)',
  url_title varchar(500) NOT NULL COMMENT 'URL 제목',
  url_author varchar(255) DEFAULT NULL COMMENT 'URL 작성자',
  url varchar(1000) NOT NULL COMMENT 'URL 주소',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='URL 정보 테이블';

-- 3. 장소 정보 테이블 (독립 테이블)
CREATE TABLE place (
  id char(36) NOT NULL,
  address varchar(500) NOT NULL COMMENT '주소',
  title varchar(255) NOT NULL,
  description text COMMENT '설명',
  intro text,
  type varchar(50) DEFAULT NULL,
  image varchar(500) DEFAULT NULL COMMENT '이미지 URL',
  latitude decimal(10,8) DEFAULT NULL,
  longitude decimal(11,8) DEFAULT NULL,
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  open_hours text,
  phone varchar(255) DEFAULT NULL,
  rating decimal(38,2) DEFAULT NULL,
  website varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_place_location (latitude, longitude),
  KEY idx_place_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='장소 정보 테이블';

-- 4. 여행 정보 테이블 (URL, 사용자 참조)
CREATE TABLE travel_info (
  id varchar(36) NOT NULL COMMENT '여행 정보 ID (UUID)',
  email varchar(255) NOT NULL COMMENT '사용자 이메일',
  url_id varchar(128) DEFAULT NULL,
  place_count int unsigned DEFAULT NULL,
  title varchar(100) DEFAULT NULL,
  is_favorite tinyint(1) DEFAULT NULL,
  fixed tinyint(1) DEFAULT NULL,
  is_delete tinyint(1) DEFAULT NULL,
  travel_days int DEFAULT NULL COMMENT '여행 기간 (일)',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  use_count int NOT NULL DEFAULT '0',
  ext_place_list_id varchar(36) NOT NULL,
  travel_taste_id varchar(36) NOT NULL,
  PRIMARY KEY (id),
  KEY fk_travel_info_user (email),
  KEY fk_travel_info_url (url_id),
  KEY idx_travel_info_bookmark (is_favorite),
  CONSTRAINT fk_travel_info_url FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_travel_info_user FOREIGN KEY (email) REFERENCES users (email) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='여행 정보 테이블';

-- 5. 여행 가이드 테이블 (여행 정보 참조)
CREATE TABLE guide (
  id char(36) NOT NULL DEFAULT (uuid()),
  travel_info_id varchar(36) NOT NULL COMMENT '여행 정보 ID',
  course_count int unsigned DEFAULT '0' COMMENT '코스 수',
  title varchar(100) NOT NULL COMMENT '가이드 제목',
  travel_days int DEFAULT NULL COMMENT '여행 일수',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  is_favorite tinyint(1) NOT NULL DEFAULT '0' COMMENT '즐겨찾기 여부',
  fixed tinyint(1) NOT NULL DEFAULT '0' COMMENT '고정 여부',
  is_delete tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  use_count int NOT NULL DEFAULT '0',
  plan_types varchar(20) NOT NULL,
  PRIMARY KEY (id),
  KEY fk_guide_travel_info (travel_info_id),
  KEY idx_guide_bookmark (is_favorite),
  CONSTRAINT fk_guide_travel_info FOREIGN KEY (travel_info_id) REFERENCES travel_info (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='여행 가이드 테이블';

-- 6. 코스 정보 테이블 (가이드 참조)
CREATE TABLE course (
  id char(36) NOT NULL DEFAULT (uuid()),
  guide_id char(36) NOT NULL DEFAULT (uuid()),
  course_number int NOT NULL COMMENT '코스 순서',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  is_delete tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  PRIMARY KEY (id),
  UNIQUE KEY uk_guide_course (guide_id, course_number),
  CONSTRAINT fk_course_guide FOREIGN KEY (guide_id) REFERENCES guide (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='코스 정보 테이블';

-- 7. 코스-장소 연결 테이블 (코스, 장소 참조)
CREATE TABLE course_place (
  place_id char(36) NOT NULL COMMENT '장소 ID',
  course_id char(36) NOT NULL DEFAULT (uuid()),
  place_num int NOT NULL COMMENT '장소 순서',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  is_deleted tinyint(1) DEFAULT '0',
  PRIMARY KEY (place_id, course_id),
  KEY fk_course_place_course (course_id),
  CONSTRAINT fk_course_place_course FOREIGN KEY (course_id) REFERENCES course (id),
  CONSTRAINT fk_course_place_place FOREIGN KEY (place_id) REFERENCES place (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='코스-장소 연결 테이블';

-- 8. 여행 정보-장소 연결 테이블 (여행 정보, 장소 참조)
CREATE TABLE travel_info_place (
  travel_info_id varchar(36) NOT NULL COMMENT '여행 정보 ID',
  place_id char(36) NOT NULL COMMENT '장소 ID',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  is_deleted tinyint(1) DEFAULT '0' COMMENT '삭제 여부',
  PRIMARY KEY (travel_info_id, place_id),
  KEY fk_tip_place (place_id),
  CONSTRAINT fk_tip_place FOREIGN KEY (place_id) REFERENCES place (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_tip_travel_info FOREIGN KEY (travel_info_id) REFERENCES travel_info (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='여행 정보-장소 연결 테이블';

-- 9. 여행 정보-URL 연결 테이블 (여행 정보, URL 참조)
CREATE TABLE travel_info_url (
  travel_info_id varchar(36) NOT NULL COMMENT '여행 정보 ID',
  url_id varchar(128) NOT NULL COMMENT 'URL ID',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (travel_info_id, url_id),
  KEY fk_tiu_url (url_id),
  CONSTRAINT fk_tiu_travel_info FOREIGN KEY (travel_info_id) REFERENCES travel_info (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_tiu_url FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='여행 정보-URL 연결 테이블';

-- 10. URL-장소 연결 테이블 (URL, 장소 참조)
CREATE TABLE url_place (
  url_id varchar(128) NOT NULL COMMENT 'URL ID',
  place_id char(36) NOT NULL COMMENT '장소 ID',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (url_id, place_id),
  KEY fk_url_place_place (place_id),
  CONSTRAINT fk_url_place_place FOREIGN KEY (place_id) REFERENCES place (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_url_place_url FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='URL-장소 연결 테이블';

-- 11. 사용자 검색어 기록 테이블 (사용자 참조)
CREATE TABLE user_search_term (
  id varchar(36) NOT NULL COMMENT '검색어 ID (UUID)',
  email varchar(255) NOT NULL COMMENT '사용자 이메일',
  word varchar(100) NOT NULL COMMENT '검색어',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  KEY fk_search_term_user (email),
  CONSTRAINT fk_search_term_user FOREIGN KEY (email) REFERENCES users (email) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='사용자 검색어 기록 테이블';

-- 12. 사용자-URL 연결 테이블 (사용자, URL 참조)
CREATE TABLE user_url (
  email varchar(255) NOT NULL COMMENT '사용자 이메일',
  url_id varchar(128) NOT NULL COMMENT 'URL ID',
  is_use tinyint(1) NOT NULL DEFAULT '1' COMMENT '사용 여부',
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  update_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (email, url_id),
  KEY fk_user_url_url (url_id),
  CONSTRAINT fk_user_url_url FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_user_url_user FOREIGN KEY (email) REFERENCES users (email) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='사용자-URL 연결 테이블';
