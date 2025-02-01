package com.example.mytravellink.travel.domain;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class TravelInfoUrlId implements Serializable {
  private static final long serialVersionUID = 1L;

  private String travelInfoId;
  private String urlId;

  @Builder
  public TravelInfoUrlId(String travelInfoId, String urlId) {
    this.travelInfoId = travelInfoId;
    this.urlId = urlId;
  }
}
