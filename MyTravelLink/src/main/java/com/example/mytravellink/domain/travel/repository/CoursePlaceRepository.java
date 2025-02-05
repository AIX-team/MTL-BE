package com.example.mytravellink.domain.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.travel.entity.CoursePlace;
import com.example.mytravellink.domain.travel.entity.CoursePlaceId;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, CoursePlaceId> {
  
}
