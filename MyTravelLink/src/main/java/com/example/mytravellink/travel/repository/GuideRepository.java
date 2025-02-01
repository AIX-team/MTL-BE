package com.example.mytravellink.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.travel.domain.Guide;

public interface GuideRepository extends JpaRepository<Guide, String> {
  
}
