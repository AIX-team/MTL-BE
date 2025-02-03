package com.example.mytravellink.domain.url.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.entity.TravelInfoUrl;
import com.example.mytravellink.domain.travel.entity.TravelInfoUrlId;

@Repository
public interface TravelInfoUrlRepository extends JpaRepository<TravelInfoUrl, TravelInfoUrlId> {

  @Query("SELECT url.id FROM TravelInfoUrl WHERE travelInfo = :travelInfo")
  List<String> findUrlIdByTravelInfoId(TravelInfo travelInfo);
  
}
