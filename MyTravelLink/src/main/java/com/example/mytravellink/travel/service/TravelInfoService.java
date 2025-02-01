package com.example.mytravellink.travel.service;

import java.util.List;

import com.example.mytravellink.travel.domain.Place;
import com.example.mytravellink.travel.domain.TravelInfo;

public interface TravelInfoService {

  TravelInfo getTravelInfo(String travelId);

  List<Place> getTravelInfoPlace(String travelInfoId);
  
}

