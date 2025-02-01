package com.example.mytravellink.url.repository;

import com.example.mytravellink.url.domain.UrlPlace;
import com.example.mytravellink.url.domain.UrlPlaceId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UrlPlaceRepository extends JpaRepository<UrlPlace, UrlPlaceId> {

  @Query("SELECT p.placeId FROM UrlPlace p WHERE p.url.id = :urlId")
  List<String> findByUrlId(@Param("urlId") String urlId);
}
