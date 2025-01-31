package com.example.mytravellink.travel.repository;

import com.example.mytravellink.travel.domain.TravelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelInfoPlaceRepository extends JpaRepository<TravelInfo, String> {
  
}