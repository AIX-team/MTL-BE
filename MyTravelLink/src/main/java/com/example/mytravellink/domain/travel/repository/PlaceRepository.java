package com.example.mytravellink.domain.travel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mytravellink.domain.travel.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, String> {

  Optional<Place> findByTitle(String title);

  Optional<Place> findById(String placeId);

  @Query("SELECT p FROM Place p WHERE p.id IN :ids")
  List<Place> findByIdIn(@Param("ids") List<String> ids);
}
