package com.example.mytravellink.domain.travel.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.mytravellink.domain.travel.entity.TravelInfo;



public interface TravelInfoRepository extends JpaRepository<TravelInfo, String> {

  Optional<TravelInfo> findById(String id);
  
  @Modifying
  @Transactional
  @Query("UPDATE TravelInfo SET title = :title, travelDays = :travelDays WHERE id = :id AND isDelete = false")
  void updateTravelInfo(@Param("id") String id, @Param("title") String title, @Param("travelDays") Integer travelDays);

  @Query("SELECT t FROM TravelInfo t WHERE t.user.email = :userEmail AND t.isDelete = false")
  List<TravelInfo> findByUserEmail(@Param("userEmail") String userEmail);

  @Query("SELECT t.id FROM TravelInfo t WHERE t.user.email = :userEmail AND t.isDelete = false")
  List<String> findTravelInfoIdByUserEmail(@Param("userEmail") String userEmail);

  @Modifying
  @Transactional
  @Query("UPDATE TravelInfo SET isFavorite = :isFavorite WHERE id = :id")
  void updateFavorite(@Param("id") String id, @Param("isFavorite") Boolean isFavorite);

  @Modifying
  @Transactional
  @Query("UPDATE TravelInfo SET fixed = :fixed WHERE id = :id")
  void updateFixed(@Param("id") String id, @Param("fixed") Boolean fixed);

  @Modifying
  @Transactional
  @Query("UPDATE TravelInfo SET isDelete = :isDelete WHERE id = :id")
  void updateDeleted(@Param("id") String id, @Param("isDelete") Boolean isDelete);

  /**
   * EMAIL 기준 가이드 수 조회
   * travelInfo 테이블과 guide 테이블의 연관관계를 통해 가이드 수 조회  
   * @param userEmail
   * 
   * @return int
   */
  @Query("SELECT COUNT(g) FROM Guide g " +
         "JOIN g.travelInfo t " +
         "WHERE t.user.email = :userEmail " +
         "AND g.isDelete = false")
  int getGuideCount(@Param("userEmail") String userEmail);
}
