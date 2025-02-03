package com.example.mytravellink.domain.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.domain.travel.entity.Course;

public interface CourseRepository extends JpaRepository<Course, String> {
  
}
