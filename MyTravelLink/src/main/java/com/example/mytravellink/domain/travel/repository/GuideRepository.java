package com.example.mytravellink.domain.travel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mytravellink.domain.travel.entity.Guide;
import com.example.mytravellink.domain.travel.repository.query.GuideQueryRepository;

public interface GuideRepository extends JpaRepository<Guide, String>, GuideQueryRepository {
  

  @Query("SELECT g FROM Guide g WHERE g.travelInfo.id IN :travelInfoIdList AND g.isDelete = false")
  List<Guide> findByTravelInfoIdList(@Param("travelInfoIdList") List<String> travelInfoIdList);
}
