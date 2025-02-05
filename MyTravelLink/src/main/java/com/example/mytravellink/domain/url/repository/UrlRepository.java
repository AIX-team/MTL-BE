package com.example.mytravellink.domain.url.repository;

import com.example.mytravellink.domain.url.entity.Url;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<Url, String> {
  Optional<Url> findById(String id);

  Optional<Url> findByUrl(String url);
}
