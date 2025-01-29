-- 데이터베이스 생성
CREATE DATABASE test_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 데이터베이스 선택
USE test_db;

-- User 테이블 생성
CREATE TABLE User (
                      email VARCHAR(255) PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      profile_img VARCHAR(255),
                      is_delete BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User_Search_Term 테이블 생성
CREATE TABLE User_Search_Term (
                                  id VARCHAR(255) PRIMARY KEY,
                                  email VARCHAR(255) NOT NULL,
                                  word VARCHAR(255) NOT NULL,
                                  create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                                  FOREIGN KEY (email) REFERENCES User(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Travel_Taste 테이블 생성
CREATE TABLE Travel_Taste (
                              id VARCHAR(255) PRIMARY KEY,
                              email VARCHAR(255) NOT NULL,
                              landmark BOOLEAN DEFAULT FALSE,
                              relax BOOLEAN DEFAULT FALSE,
                              food BOOLEAN DEFAULT FALSE,
                              alone BOOLEAN DEFAULT FALSE,
                              romance BOOLEAN DEFAULT FALSE,
                              friend BOOLEAN DEFAULT FALSE,
                              child BOOLEAN DEFAULT FALSE,
                              parents BOOLEAN DEFAULT FALSE,
                              travel_days INT,
                              options_input VARCHAR(255),
                              create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (email) REFERENCES User(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- URL 테이블 생성
CREATE TABLE URL (
                     id VARCHAR(255) PRIMARY KEY,
                     ext_url_id VARCHAR(255),
                     url_title VARCHAR(255),
                     url_author VARCHAR(255),
                     create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     url VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User_URL 테이블 생성
CREATE TABLE User_URL (
                          email VARCHAR(255) NOT NULL,
                          url_id VARCHAR(255) NOT NULL,
                          create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                          is_use BOOLEAN DEFAULT TRUE,
                          PRIMARY KEY (email, url_id),
                          FOREIGN KEY (email) REFERENCES User(email),
                          FOREIGN KEY (url_id) REFERENCES URL(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Travel_Info 테이블 생성
CREATE TABLE Travel_Info (
                             id VARCHAR(255) PRIMARY KEY,
                             email VARCHAR(255) NOT NULL,
                             travel_taste_id VARCHAR(255),
                             place_count INT DEFAULT 0,
                             use_count INT DEFAULT 0,
                             title VARCHAR(255) NOT NULL,
                             create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             bookmark BOOLEAN DEFAULT FALSE,
                             fixed BOOLEAN DEFAULT FALSE,
                             is_delete BOOLEAN DEFAULT FALSE,
                             FOREIGN KEY (email) REFERENCES User(email),
                             FOREIGN KEY (travel_taste_id) REFERENCES Travel_Taste(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Place 테이블 생성
CREATE TABLE Place (
                       id VARCHAR(255) PRIMARY KEY,
                       address VARCHAR(255),
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       type VARCHAR(255),
                       image VARCHAR(255),
                       score DECIMAL(3,1),
                       review_cnt INT DEFAULT 0,
                       create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Guide 테이블 생성
CREATE TABLE Guide (
                       id VARCHAR(255) PRIMARY KEY,
                       travel_info_id VARCHAR(255) NOT NULL,
                       course_count INT DEFAULT 0,
                       use_count INT DEFAULT 0,
                       title VARCHAR(255) NOT NULL,
                       travel_days INT,
                       create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                       bookmark BOOLEAN DEFAULT FALSE,
                       fixed BOOLEAN DEFAULT FALSE,
                       is_delete BOOLEAN DEFAULT FALSE,
                       FOREIGN KEY (travel_info_id) REFERENCES Travel_Info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Course 테이블 생성
CREATE TABLE Course (
                        id VARCHAR(255) PRIMARY KEY,
                        guide_id VARCHAR(255) NOT NULL,
                        course_number INT NOT NULL,
                        create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                        is_delete BOOLEAN DEFAULT FALSE,
                        FOREIGN KEY (guide_id) REFERENCES Guide(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Travel_Info_Place 테이블 생성
CREATE TABLE Travel_Info_Place (
                                   travel_info_id VARCHAR(255) NOT NULL,
                                   place_id VARCHAR(255) NOT NULL,
                                   create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (travel_info_id, place_id),
                                   FOREIGN KEY (travel_info_id) REFERENCES Travel_Info(id),
                                   FOREIGN KEY (place_id) REFERENCES Place(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- URL_Place 테이블 생성
CREATE TABLE URL_Place (
                           url_id VARCHAR(255) NOT NULL,
                           place_id VARCHAR(255) NOT NULL,
                           create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (url_id, place_id),
                           FOREIGN KEY (url_id) REFERENCES URL(id),
                           FOREIGN KEY (place_id) REFERENCES Place(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Course_Place 테이블 생성
CREATE TABLE Course_Place (
                              place_id VARCHAR(255) NOT NULL,
                              guide_id VARCHAR(255) NOT NULL,
                              place_num INT NOT NULL,
                              create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (place_id, guide_id),
                              FOREIGN KEY (place_id) REFERENCES Place(id),
                              FOREIGN KEY (guide_id) REFERENCES Guide(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 인덱스 생성
CREATE INDEX idx_user_email ON User(email);
CREATE INDEX idx_travel_info_email ON Travel_Info(email);
CREATE INDEX idx_travel_taste_email ON Travel_Taste(email);
CREATE INDEX idx_guide_travel_info ON Guide(travel_info_id);
CREATE INDEX idx_course_guide ON Course(guide_id);
CREATE INDEX idx_place_title ON Place(title);
CREATE INDEX idx_url_ext_url ON URL(ext_url_id);

-- 외래키 체크 다시 설정
SET FOREIGN_KEY_CHECKS = 1;
