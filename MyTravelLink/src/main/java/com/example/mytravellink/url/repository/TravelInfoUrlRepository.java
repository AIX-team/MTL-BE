package com.example.mytravellink.url.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.mytravellink.travel.domain.TravelInfo;
import com.example.mytravellink.travel.domain.TravelInfoUrl;
import com.example.mytravellink.travel.domain.TravelInfoUrlId;

@Repository
public interface TravelInfoUrlRepository extends JpaRepository<TravelInfoUrl, TravelInfoUrlId> {

  @Query("SELECT url.id FROM TravelInfoUrl WHERE travelInfo = :travelInfo")
  List<String> findUrlIdByTravelInfoId(TravelInfo travelInfo);
  
}
