package com.example.mytravellink.domain.travel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mytravellink.domain.travel.entity.CoursePlace;
import com.example.mytravellink.domain.travel.entity.CoursePlaceId;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, CoursePlaceId> {
  @Query("SELECT cp FROM CoursePlace cp WHERE cp.course.id = :courseId")
  List<CoursePlace> findByCourseId(@Param("courseId") String courseId);

  @Modifying
  @Query("UPDATE CoursePlace cp SET cp.placeNum = :num WHERE cp.course.id = :courseId AND cp.place.id = :placeId")
  void updateCoursePlace(@Param("courseId") String courseId, @Param("placeId") String placeId, @Param("num") int num);
}
