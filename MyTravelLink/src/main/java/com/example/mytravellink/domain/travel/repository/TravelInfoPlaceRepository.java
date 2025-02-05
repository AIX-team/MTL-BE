package com.example.mytravellink.domain.travel.repository;

import com.example.mytravellink.domain.travel.entity.TravelInfoPlace;
import com.example.mytravellink.domain.travel.entity.TravelInfoPlaceId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TravelInfoPlaceRepository extends JpaRepository<TravelInfoPlace, TravelInfoPlaceId> {

  @Query("SELECT tip.place.id FROM TravelInfoPlace tip WHERE tip.travelInfo.id = :travelInfoId")
  List<String> findByTravelInfoId(String travelInfoId);
  
}