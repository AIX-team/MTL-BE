package com.example.mytravellink.domain.travel.service;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;

import java.util.List;

import com.example.mytravellink.domain.travel.entity.Guide;
import com.example.mytravellink.domain.travel.entity.TravelInfo;

public interface GuideService {
  /**
   * 가이드 조회
   * @param guideId
   * @return Guide
   */
  Guide getGuide(String guideId);

  /**
   * 여행정보 조회
   * @param guideId
   * @return TravelInfo
   */
  TravelInfo getTravelInfo(String guideId);

  /**
   * 가이드 생성
   * @param guide
   * @param aiGuideCourseResponse
   */
  void createGuideAndCourses(Guide guide, AIGuideCourseResponse aiGuideCourseResponse);

  /**
   * 가이드 북 제목 수정
   * @param guideId
   * @param title
   */
  void updateGuideBookTitle(String guideId, String title);

  /**
   * 가이드 북 목록 조회
   * @param userEmail
   * @return List<Guide>
   */
  List<Guide> getGuideList(String userEmail);
}

