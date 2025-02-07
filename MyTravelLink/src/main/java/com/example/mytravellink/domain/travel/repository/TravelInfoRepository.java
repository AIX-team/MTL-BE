package com.example.mytravellink.domain.travel.repository;

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
}
