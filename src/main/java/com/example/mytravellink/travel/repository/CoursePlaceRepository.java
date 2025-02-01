package com.example.mytravellink.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.travel.domain.CoursePlace;
import com.example.mytravellink.travel.domain.CoursePlaceId;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, CoursePlaceId> {
  
}
