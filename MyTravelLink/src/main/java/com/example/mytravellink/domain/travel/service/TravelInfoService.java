package com.example.mytravellink.domain.travel.service;

import java.util.List;

import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;

public interface TravelInfoService {

  TravelInfo getTravelInfo(String travelId);

  List<Place> getTravelInfoPlace(String travelInfoId);

  void updateTravelInfo(String travelInfoId, String travelInfoTitle, Integer travelDays);
  
}

