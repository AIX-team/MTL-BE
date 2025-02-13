package com.example.mytravellink.domain.url.repository;

import java.util.List;
import java.util.Optional;

import com.example.mytravellink.domain.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.entity.TravelInfoUrl;
import com.example.mytravellink.domain.travel.entity.TravelInfoUrlId;

@Repository
public interface TravelInfoUrlRepository extends JpaRepository<TravelInfoUrl, TravelInfoUrlId> {

  // TravelInfo에 해당하는 Url ID 목록 조회
  @Query("SELECT tu.url.id FROM TravelInfoUrl tu WHERE tu.travelInfo = :travelInfo")
  List<String> findUrlIdByTravelInfoId(@Param("travelInfo") TravelInfo travelInfo);

  @Query("SELECT tu.url.id FROM TravelInfoUrl tu WHERE tu.travelInfo.id = :travelInfoId")
  List<String> findUrlIdByTravelInfoId(@Param("travelInfoId") String travelInfoId);

  Optional<TravelInfoUrl> findByTravelInfoAndUrl(TravelInfo travelInfo, Url url);

}
