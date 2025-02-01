package com.example.mytravellink.users.domain;

import jakarta.persistence.Column;
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
public class UsersUrlId implements Serializable {
  private static final long serialVersionUID = 1L;

  @Column(name = "email")
  private String eid;

  @Column(name = "url_id", columnDefinition = "VARCHAR(128)")
  private String uid;

  @Builder
  public UsersUrlId(String email, String urlId) {
    this.eid = email;
    this.uid = urlId;
  }
}
