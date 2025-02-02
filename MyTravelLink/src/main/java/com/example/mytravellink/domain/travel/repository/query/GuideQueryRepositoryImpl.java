package com.example.mytravellink.domain.travel.repository.query;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.mytravellink.domain.travel.entity.Guide;
import com.example.mytravellink.domain.travel.entity.QGuide;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class GuideQueryRepositoryImpl implements GuideQueryRepository {
  
  private final JPAQueryFactory queryFactory;

  @Override
  public List<Guide> findAllByTravelInfoId(String travelInfoId) {
    QGuide guide = new QGuide("guide");
    return queryFactory.selectFrom(guide)
      .where(guide.travelInfo.id.eq(travelInfoId))
      .fetch();
  }
}
