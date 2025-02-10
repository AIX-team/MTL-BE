package com.example.mytravellink.domain.travel.repository.query;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.mytravellink.domain.travel.entity.CoursePlace;
import com.example.mytravellink.domain.travel.entity.QCoursePlace;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CoursePlaceQueryRepositoryImpl implements CoursePlaceQueryRepository {
  
  private final JPAQueryFactory queryFactory;
  
  private final QCoursePlace coursePlace = new QCoursePlace("coursePlace");

  @Transactional
  @Override
  public void updateCoursePlace(String courseId, List<String> placeIds) {

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

  @Transactional
  @Override
  public void updatePlaceNum(String courseId) {
    try {
      List<CoursePlace> coursePlaceList = queryFactory.selectFrom(coursePlace)
        .where(coursePlace.course.id.eq(courseId)
          .and(coursePlace.isDeleted.eq(false)))
      .orderBy(coursePlace.placeNum.asc())
      .fetch();

    for (int i = 0; i < coursePlaceList.size(); i++) {
      queryFactory.update(coursePlace)
        .where(coursePlace.course.id.eq(courseId)
          .and(coursePlace.placeNum.eq(i + 1)))
          .set(coursePlace.placeNum, i + 1)
          .execute();
      }
    } catch (Exception e) {
      throw new RuntimeException("CoursePlace 업데이트 실패", e);
    }
  }

  @Transactional
  @Override
  public void updateIsDeleted(String courseId, String placeId, boolean isDeleted) {
    try {
      queryFactory.update(coursePlace)
        .where(coursePlace.course.id.eq(courseId)
          .and(coursePlace.place.id.eq(placeId)))
        .set(coursePlace.isDeleted, isDeleted)
        .execute();
    } catch (Exception e) {
      throw new RuntimeException("CoursePlace 업데이트 실패", e);
    }
  }

  @Transactional
  @Override
  public void updateDeleted(String courseId, String placeId, boolean isDeleted, int placeNum) {
    try {
      queryFactory.update(coursePlace)
        .where(coursePlace.course.id.eq(courseId)
          .and(coursePlace.place.id.eq(placeId)))
      .set(coursePlace.isDeleted, isDeleted)
        .set(coursePlace.placeNum, placeNum)
        .execute();
    } catch (Exception e) {
      throw new RuntimeException("CoursePlace 업데이트 실패", e);
    }
  }
}

