package com.example.mytravellink.domain.url.service;

import java.util.List;

import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.url.entity.Url;

public interface UrlService {

  List<Place> findPlaceByUrlId(String urlId);

  List<Url> findUrlByTravelInfoId(TravelInfo travelInfo);
}
