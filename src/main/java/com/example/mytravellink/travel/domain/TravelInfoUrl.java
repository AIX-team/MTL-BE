package com.example.mytravellink.travel.domain;

import com.example.mytravellink.url.domain.Url;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class TravelInfoUrl {

  @EmbeddedId
  private TravelInfoUrlId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "travelInfoId")
  private TravelInfo travelInfo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "urlId")
  private Url url;

  @Builder
  public TravelInfoUrl(TravelInfo travelInfo, Url url) {
    this.travelInfo = travelInfo;
    this.url = url;
  }
}
