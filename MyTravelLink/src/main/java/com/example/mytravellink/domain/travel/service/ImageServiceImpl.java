package com.example.mytravellink.domain.travel.service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mytravellink.api.travelInfo.dto.travel.GuideBookResponse;
import com.example.mytravellink.domain.travel.entity.Place;

@Service
public class ImageServiceImpl implements ImageService  {


  /**
   * 코스 이미지 URL 리다이렉션
   * @param courseList
   * @return 리다이렉션된 코스 리스트
   */
  @Override
  public List<GuideBookResponse.CourseList> redirectImageUrl(List<GuideBookResponse.CourseList> courseList) {
    
    for(GuideBookResponse.CourseList course : courseList) {
      for(GuideBookResponse.CoursePlaceResp place : course.getCoursePlaces()) {
        String imageUrl = place.getImage();
        String redirectImageUrl = redirectImageUrl(imageUrl);
        place.setImage(redirectImageUrl);
      }
    }
    return courseList;
  }

  /**
   * 장소 이미지 URL 리다이렉션
   * @param placeList
   * @return 리다이렉션된 장소 리스트
   */
  @Override
  public List<Place> redirectImageUrlPlace(List<Place> placeList) {
    try {
      for(Place place : placeList) {
        String imageUrl = place.getImage();
        String redirectImageUrl = redirectImageUrl(imageUrl);
        place.setImage(redirectImageUrl);
      }
    } catch (Exception e) {
      throw new RuntimeException("이미지 URL 변환 실패", e);
    }
    return placeList;
  }


  /**
   * 이미지 URL 리다이렉션
   * @param imageUrl
   * @return 리다이렉션된 이미지 URL
   */
  public String redirectImageUrl(String imageUrl) {
    try {
      if(imageUrl == null) {
        throw new RuntimeException("이미지 URL은 필수 입력값입니다");
      }
      imageUrl = imageUrl.substring(16, imageUrl.length()-2);

      URI uri = new URI(imageUrl);
      URL originUrl = uri.toURL();

      HttpURLConnection conn = (HttpURLConnection) originUrl.openConnection();
      conn.setInstanceFollowRedirects(false);  // 리다이렉트를 자동으로 따라가지 않음

      // 리다이렉션 URL 가져오기
      String redirectUrl = conn.getHeaderField("Location");

      if (redirectUrl != null) return redirectUrl;

      return null;
    } catch (Exception e) {
      throw new RuntimeException("URL 변환 실패", e);
    }
  }
}
