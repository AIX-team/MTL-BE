package com.example.mytravellink.travel.repository.query;

import java.util.List;

import com.example.mytravellink.travel.domain.Guide;

public interface GuideQueryRepository  {
  public List<Guide> findAllByTravelInfoId(String travelInfoId);
}
