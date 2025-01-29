package com.example.mytravellink.url.domain;

import com.example.mytravellink.domain.BaseTimeEntity;
import com.example.mytravellink.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.EmbeddedId;

/**
 * 사용자 URL (UserUrl) 엔티티
 * User와 Url 간의 다대다 관계를 위한 중간 테이블 엔티티입니다.
 * 복합 키를 사용하여 User와 Url을 연결합니다.
 */
@Entity
@Table(name = "user_url")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUrl extends BaseTimeEntity {

    @EmbeddedId
    private UserUrlId id;

    @MapsId("email")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email")
    private User user;
    
    @MapsId("urlId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id")
    private Url url;
    
    private boolean isUse;
    
    @Builder
    public UserUrl(UserUrlId id, User user, Url url) {
        this.id = id;
        this.user = user;
        this.url = url;
        this.isUse = true;
    }
} 