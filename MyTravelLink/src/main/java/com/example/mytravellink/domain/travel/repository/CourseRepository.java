package com.example.mytravellink.domain.travel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.travel.entity.Course;

public interface CourseRepository extends JpaRepository<Course, String> {
  List<Course> findByGuideId(String guideId);
}
