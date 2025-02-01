package com.example.mytravellink.user.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class UserUrlId implements Serializable {
  private static final long serialVersionUID = 1L;
  private String email;
  private String urlId;

  @Builder
  public UserUrlId(String email, String urlId) {
    this.email = email;
    this.urlId = urlId;
  }
}
