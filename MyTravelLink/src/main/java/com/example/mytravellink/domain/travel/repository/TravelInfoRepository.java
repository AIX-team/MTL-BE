package com.example.mytravellink.domain.travel.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.travel.entity.TravelInfo;

public interface TravelInfoRepository extends JpaRepository<TravelInfo, String> {

  Optional<TravelInfo> findById(String id);
  
}
