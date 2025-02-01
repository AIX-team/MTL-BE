package com.example.mytravellink.url.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class UrlPlaceId {
  private String urlId;
  private String placeId;

  @Builder
  public UrlPlaceId(String urlId, String placeId) {
    this.urlId = urlId;
    this.placeId = placeId;
  }
}
