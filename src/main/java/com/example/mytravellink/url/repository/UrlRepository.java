package com.example.mytravellink.url.repository;

import java.util.List;

import com.example.mytravellink.url.domain.Url;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<Url, String> {

  List<Url> findByTravelId(String travelId);
}
