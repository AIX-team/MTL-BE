package com.example.mytravellink.url.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class UserUrlId implements Serializable {
    private String email;
    private String urlId;

    @Builder
    public UserUrlId(String email, String urlId) {
        this.email = email;
        this.urlId = urlId;
    }
} 