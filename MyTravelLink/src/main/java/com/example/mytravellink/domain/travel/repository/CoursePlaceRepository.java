package com.example.mytravellink.domain.travel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mytravellink.domain.travel.entity.CoursePlace;
import com.example.mytravellink.domain.travel.entity.CoursePlaceId;
import com.example.mytravellink.domain.travel.repository.query.CoursePlaceQueryRepository;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, CoursePlaceId>, CoursePlaceQueryRepository {
  @Query("SELECT cp FROM CoursePlace cp WHERE cp.course.id = :courseId")
  List<CoursePlace> findByCourseId(@Param("courseId") String courseId);

}
