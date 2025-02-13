package com.example.mytravellink.domain.url.repository;

import com.example.mytravellink.domain.url.entity.UrlPlace;
import com.example.mytravellink.domain.url.entity.UrlPlaceId;
import com.example.mytravellink.domain.url.entity.Url;
import com.example.mytravellink.domain.travel.entity.Place;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UrlPlaceRepository extends JpaRepository<UrlPlace, UrlPlaceId> {

  @Query("SELECT p.place.id FROM UrlPlace p WHERE p.url.id = :urlId")
  List<String> findByUrlId(@Param("urlId") String urlId);

    List<UrlPlace> findByUrl_Id(String urlId);

    Optional<UrlPlace> findByUrlAndPlace(Url url, Place place);
}
