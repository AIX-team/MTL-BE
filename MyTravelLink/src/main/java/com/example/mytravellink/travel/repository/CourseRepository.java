package com.example.mytravellink.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mytravellink.travel.domain.Course;

public interface CourseRepository extends JpaRepository<Course, String> {
  
}
