package com.example.mytravellink.url.service;

import java.util.List;

import com.example.mytravellink.travel.domain.Place;
import com.example.mytravellink.travel.domain.TravelInfo;
import com.example.mytravellink.url.domain.Url;

public interface UrlService {

  List<Place> findPlaceByUrlId(String urlId);

  List<Url> findUrlByTravelInfoId(TravelInfo travelInfo);
}
