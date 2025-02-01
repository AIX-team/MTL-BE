package com.example.mytravellink.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.travel.domain.Place;

public interface PlaceRepository extends JpaRepository<Place, String> {
  
}
