use test_db;

-- 문자셋 설정
set names utf8mb4;

-- user 테이블 생성
create table user (
                      email varchar(100) not null primary key comment '사용자 이메일',
                      name varchar(50) not null comment '사용자 이름',
                      dob date not null comment '생년월일',
                      gender tinyint not null comment '성별',
                      create_at timestamp default current_timestamp not null comment '생성일시',
                      profile_img varchar(255) comment '프로필 이미지 URL',
                      is_delete tinyint(1) default 0 not null comment '삭제여부',
                      index idx_user_email (email)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='사용자 정보';

-- user_search_term 테이블 생성
create table user_search_term (
                                  id bigint not null auto_increment primary key comment '검색어 ID',
                                  email varchar(100) not null comment '사용자 이메일',
                                  word varchar(100) not null comment '검색어',
                                  create_at timestamp default current_timestamp not null comment '생성일시',
                                  update_at timestamp default current_timestamp on update current_timestamp not null comment '수정일시',
                                  foreign key fk_search_email (email) references user(email),
                                  index idx_search_email (email)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='사용자 검색어';

-- user_url 테이블 생성
create table user_url (
                          email varchar(100) not null comment '사용자 이메일',
                          url varchar(255) not null comment 'URL',
                          create_at timestamp default current_timestamp not null comment '생성일시',
                          update_at timestamp default current_timestamp on update current_timestamp not null comment '수정일시',
                          is_use tinyint(1) default 1 not null comment '사용여부',
                          primary key (email, url),
                          foreign key fk_url_email (email) references user(email),
                          index idx_url_email (email)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='사용자 URL';

-- travel_taste 테이블 생성
create table travel_taste (
                              id varchar(36) not null primary key comment '여행취향 ID',
                              email varchar(100) not null comment '사용자 이메일',
                              landmark tinyint(1) default 0 not null comment '랜드마크 선호',
                              relax tinyint(1) default 0 not null comment '휴식 선호',
                              food tinyint(1) default 0 not null comment '음식 선호',
                              alone tinyint(1) default 0 not null comment '혼자여행 선호',
                              romance tinyint(1) default 0 not null comment '로맨스 선호',
                              friend tinyint(1) default 0 not null comment '친구여행 선호',
                              child tinyint(1) default 0 not null comment '자녀동반 선호',
                              parents tinyint(1) default 0 not null comment '부모동반 선호',
                              travel_days int not null comment '여행일수',
                              options_input varchar(255) not null comment '추가입력옵션',
                              create_at timestamp default current_timestamp not null comment '생성일시',
                              foreign key fk_taste_email (email) references user(email),
                              index idx_taste_email (email)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='여행 취향';

-- ext_place_list 테이블 생성
create table ext_place_list (
                                id varchar(36) not null primary key comment '장소목록 ID',
                                place_list text not null comment '장소목록',
                                place_count int not null default 0 comment '장소수',
                                use_count int not null default 0 comment '사용수',
                                create_at timestamp default current_timestamp not null comment '생성일시',
                                index idx_place_count (place_count)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='추출 장소 목록';

-- travel_info 테이블 생성
create table travel_info (
                             id varchar(36) not null primary key comment '여행정보 ID',
                             email varchar(100) not null comment '사용자 이메일',
                             ext_place_list_id varchar(36) not null comment '외부장소목록 ID',
                             travel_taste_id varchar(36) not null comment '여행취향 ID',
                             place_count int not null default 0 comment '장소수',
                             use_count int not null default 0 comment '사용수',
                             title varchar(100) not null comment '제목',
                             create_at timestamp default current_timestamp not null comment '생성일시',
                             bookmark tinyint(1) default 0 not null comment '북마크여부',
                             is_delete tinyint(1) default 0 not null comment '삭제여부',
                             foreign key fk_info_email (email) references user(email),
                             foreign key fk_info_place (ext_place_list_id) references ext_place_list(id),
                             foreign key fk_info_taste (travel_taste_id) references travel_taste(id),
                             index idx_info_email (email),
                             index idx_info_place (ext_place_list_id),
                             index idx_info_taste (travel_taste_id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='여행 정보';

-- guide 테이블 생성
create table guide (
                       id varchar(36) not null primary key comment '가이드 ID',
                       travel_info_id varchar(36) not null comment '여행정보 ID',
                       course_count int not null default 0 comment '코스수',
                       use_count int not null default 0 comment '사용수',
                       create_at timestamp default current_timestamp not null comment '생성일시',
                       update_at timestamp default current_timestamp on update current_timestamp not null comment '수정일시',
                       title varchar(100) not null comment '제목',
                       travel_days int not null comment '여행일수',
                       bookmark tinyint(1) default 0 not null comment '북마크여부',
                       is_delete tinyint(1) default 0 not null comment '삭제여부',
                       foreign key fk_guide_info (travel_info_id) references travel_info(id),
                       index idx_guide_info (travel_info_id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='가이드';

-- guide_course_info 테이블 생성
create table guide_course_info (
                                   id varchar(36) not null primary key comment '코스정보 ID',
                                   guide_id varchar(36) not null comment '가이드 ID',
                                   course_number int not null comment '코스번호',
                                   place_number int not null comment '장소번호',
                                   place_list text not null comment '장소목록',
                                   create_at timestamp default current_timestamp not null comment '생성일시',
                                   update_at timestamp default current_timestamp on update current_timestamp not null comment '수정일시',
                                   is_delete tinyint(1) default 0 not null comment '삭제여부',
                                   foreign key fk_course_guide (guide_id) references guide(id),
                                   index idx_course_guide (guide_id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='가이드 코스 정보';

-- user_ext_place_list 테이블 생성
create table user_ext_place_list (
                                     id bigint not null auto_increment primary key comment 'ID',
                                     email varchar(100) not null comment '사용자 이메일',
                                     ext_place_list_id varchar(36) not null comment '외부장소목록 ID',
                                     create_at timestamp default current_timestamp not null comment '생성일시',
                                     is_delete tinyint(1) default 0 not null comment '삭제여부',
                                     foreign key fk_user_place_email (email) references user(email),
                                     foreign key fk_user_place_list (ext_place_list_id) references ext_place_list(id),
                                     index idx_user_place_email (email),
                                     index idx_user_place_list (ext_place_list_id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='사용자 외부 장소 목록';
