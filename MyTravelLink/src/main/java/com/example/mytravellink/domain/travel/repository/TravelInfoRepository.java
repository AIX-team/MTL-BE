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
  @Query("UPDATE TravelInfo SET title = :title, travelDays = :travelDays WHERE id = :id")
  void updateTravelInfo(@Param("id") String id, @Param("title") String title, @Param("travelDays") Integer travelDays);

  @Query("SELECT t FROM TravelInfo t WHERE t.user.email = :userEmail")
  List<TravelInfo> findByUserEmail(@Param("userEmail") String userEmail);

  @Modifying
  @Transactional
  @Query("UPDATE TravelInfo SET isFavorite = :isFavorite WHERE id = :id")
  void updateFavorite(@Param("id") String id, @Param("isFavorite") Boolean isFavorite);

  @Modifying
  @Transactional
  @Query("UPDATE TravelInfo SET fixed = :fixed WHERE id = :id")
  void updateFixed(@Param("id") String id, @Param("fixed") Boolean fixed);
}
