package com.example.mytravellink.url.repository;

import com.example.mytravellink.url.domain.UrlPlace;
import com.example.mytravellink.url.domain.UrlPlaceId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlPlaceRepository extends JpaRepository<UrlPlace, UrlPlaceId> {

}
