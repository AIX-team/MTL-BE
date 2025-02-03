package com.example.mytravellink.domain.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.travel.entity.Guide;

public interface GuideRepository extends JpaRepository<Guide, String> {
  
}
