package com.example.mytravellink.domain.url.repository;

import com.example.mytravellink.domain.url.entity.Url;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UrlRepository extends JpaRepository<Url, String> {
  Optional<Url> findById(String id);

  Optional<Url> findByUrl(String url);

  // Url ID 목록을 이용해 Url 엔티티 리스트 조회
  List<Url> findByIdIn(List<String> urlIds);

  // URL ID와 사용자 이메일이 Url 테이블에 존재하는지 여부를 반환
  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Url u WHERE u.id = :urlId AND u.user.email = :userEmail")
  boolean existsByIdAndUserEmail(@Param("urlId") String urlId, @Param("userEmail") String userEmail);
}
