package com.example.mytravellink.domain.travel.repository.query;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.mytravellink.domain.travel.entity.QCoursePlace;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CoursePlaceQueryRepositoryImpl implements CoursePlaceQueryRepository {
  
  private final JPAQueryFactory queryFactory;

  @Transactional
  @Override
  public void updateCoursePlace(String courseId, List<UUID> placeIds) {
    QCoursePlace coursePlace = new QCoursePlace("coursePlace");

    try {
      // 코스 장소 순서 수정
      for (int i = 0; i < placeIds.size(); i++) {
        queryFactory.update(coursePlace)
          .where(coursePlace.course.id.eq(courseId)
            .and(coursePlace.place.id.eq(placeIds.get(i))))
          .set(coursePlace.placeNum, i + 1)
          .execute();
      }
    } catch (Exception e) {
      throw new RuntimeException("CoursePlace 업데이트 실패", e);
    }
  }
}

