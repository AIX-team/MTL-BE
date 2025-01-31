package com.example.mytravellink.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.travel.domain.TravelInfo;

public interface TravelInfoRepository extends JpaRepository<TravelInfo, String> {
  
}
