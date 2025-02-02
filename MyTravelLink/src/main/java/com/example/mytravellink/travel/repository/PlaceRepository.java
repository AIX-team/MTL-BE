package com.example.mytravellink.travel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.travel.domain.Place;

public interface PlaceRepository extends JpaRepository<Place, String> {

  Optional<Place> findById(String placeId);

  List<Place> findByIds(List<String> placeIdList);
}
