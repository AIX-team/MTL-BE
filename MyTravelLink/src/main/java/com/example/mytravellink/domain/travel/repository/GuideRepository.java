package com.example.mytravellink.domain.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.travel.entity.Guide;
import com.example.mytravellink.domain.travel.repository.query.GuideQueryRepository;

public interface GuideRepository extends JpaRepository<Guide, String>, GuideQueryRepository {
  
}
