-- 데이터베이스 생성
CREATE DATABASE travel_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE travel_db;

-- user 테이블 생성
CREATE TABLE user (
    email VARCHAR(100) NOT NULL COMMENT '사용자 이메일 (주 식별자)',
    name VARCHAR(50) NOT NULL COMMENT '사용자 이름',
    profile_img VARCHAR(200) COMMENT '프로필 이미지 URL',
    is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
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

-- url 테이블 생성
CREATE TABLE url (
    url_id VARCHAR(36) NOT NULL COMMENT 'URL ID (UUID)',
    ext_url_id VARCHAR(100) COMMENT '외부 URL ID',
    url_title VARCHAR(200) NOT NULL COMMENT 'URL 제목',
    url_author VARCHAR(100) COMMENT 'URL 작성자',
    url VARCHAR(500) NOT NULL COMMENT 'URL 주소',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (url_id)
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
    FOREIGN KEY (url_id) REFERENCES url(url_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자-URL 연결 테이블';

-- travel_info 테이블 생성
CREATE TABLE travel_info (
    id VARCHAR(36) NOT NULL COMMENT '여행 정보 ID (UUID)',
    email VARCHAR(100) NOT NULL COMMENT '사용자 이메일',
    url_id VARCHAR(36) NOT NULL COMMENT '관련 URL ID',
    place_count INT UNSIGNED DEFAULT 0 COMMENT '장소 수',
    title VARCHAR(100) NOT NULL COMMENT '여행 제목',
    bookmark BOOLEAN NOT NULL DEFAULT FALSE COMMENT '북마크 여부',
    fixed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '고정 여부',
    is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
    travel_days TINYINT UNSIGNED DEFAULT 0 COMMENT '여행 기간 (일)',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (id),
    FOREIGN KEY (email) REFERENCES user(email) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (url_id) REFERENCES url(url_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 정보 테이블';

-- travel_info_url 테이블 생성
CREATE TABLE travel_info_url (
    travel_info_id VARCHAR(36) NOT NULL COMMENT '여행 정보 ID',
    url_id VARCHAR(36) NOT NULL COMMENT 'URL ID',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (travel_info_id, url_id),
    FOREIGN KEY (travel_info_id) REFERENCES travel_info(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (url_id) REFERENCES url(url_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 정보-URL 연결 테이블';

-- place 테이블 생성
CREATE TABLE place (
    id VARCHAR(36) NOT NULL COMMENT '장소 ID (UUID)',
    address VARCHAR(200) NOT NULL COMMENT '주소',
    title VARCHAR(100) NOT NULL COMMENT '장소명',
    description TEXT COMMENT '설명',
    intro VARCHAR(200) COMMENT '요약',
    type VARCHAR(50) NOT NULL COMMENT '장소 유형',
    image VARCHAR(200) COMMENT '이미지 URL',
    latitude DECIMAL(10, 8) NOT NULL COMMENT '위도 좌표값',
    longitude DECIMAL(11, 8) NOT NULL COMMENT '경도 좌표값',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장소 정보 테이블';

-- travel_info_place 테이블 생성
CREATE TABLE travel_info_place (
    travel_info_id VARCHAR(36) NOT NULL COMMENT '여행 정보 ID',
    place_id VARCHAR(36) NOT NULL COMMENT '장소 ID',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (travel_info_id, place_id),
    FOREIGN KEY (travel_info_id) REFERENCES travel_info(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (place_id) REFERENCES place(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 정보-장소 연결 테이블';

-- url_place 테이블 생성
CREATE TABLE url_place (
    url_id VARCHAR(36) NOT NULL COMMENT 'URL ID',
    place_id VARCHAR(36) NOT NULL COMMENT '장소 ID',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY(url_id, place_id),
    FOREIGN KEY(url_id) REFERENCES url(url_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(place_id) REFERENCES place(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='URL-장소 연결 테이블';

-- guide 테이블 생성
CREATE TABLE guide (
    id VARCHAR(36) NOT NULL COMMENT '가이드 ID',
    travel_info_id VARCHAR(36) NOT NULL COMMENT '여행 정보 ID',
    course_count INT UNSIGNED DEFAULT 0 COMMENT '코스 수',
    use_count INT UNSIGNED DEFAULT 0 COMMENT '사용 수',
    title VARCHAR(100) NOT NULL COMMENT '가이드 제목',
    travel_days TINYINT UNSIGNED COMMENT '여행 일수',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    bookmark BOOLEAN NOT NULL DEFAULT FALSE COMMENT '북마크 여부',
    fixed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '고정 여부',
    is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
    PRIMARY KEY (id),
    FOREIGN KEY (travel_info_id) REFERENCES travel_info(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 가이드 테이블';

-- course 테이블 생성
CREATE TABLE course (
    id VARCHAR(36) NOT NULL COMMENT '코스 ID',
    guide_id VARCHAR(36) NOT NULL COMMENT '가이드 ID',
    course_number TINYINT UNSIGNED NOT NULL COMMENT '코스 순서',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    is_delete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
    PRIMARY KEY(id),
    FOREIGN KEY (guide_id) REFERENCES guide(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_guide_course (guide_id, course_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='코스 정보 테이블';

-- course_place 테이블 생성
CREATE TABLE course_place (
    place_id VARCHAR(36) NOT NULL COMMENT '장소 ID',
    course_id VARCHAR(36) NOT NULL COMMENT '코스 ID',
    place_num TINYINT UNSIGNED NOT NULL COMMENT '장소 순서',
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY(place_id, course_id),
    FOREIGN KEY(place_id) REFERENCES place(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(course_id) REFERENCES course(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='코스-장소 연결 테이블';

-- 인덱스 생성
CREATE INDEX idx_place_location ON place(latitude, longitude);
CREATE INDEX idx_travel_info_bookmark ON travel_info(bookmark);
CREATE INDEX idx_guide_bookmark ON guide(bookmark);
CREATE INDEX idx_place_title ON place(title);