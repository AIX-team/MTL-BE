package com.example.mytravellink.domain.users.entity;

import com.example.mytravellink.domain.BaseTimeEntity;
import com.example.mytravellink.domain.url.entity.Url;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;

/**
 * 사용자 URL (UserUrl) 엔티티
 * User와 Url 간의 다대다 관계를 위한 중간 테이블 엔티티입니다.
 * 복합 키를 사용하여 User와 Url을 연결합니다.
 */
@Entity
@Table(name = "user_url")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersUrl extends BaseTimeEntity {

    @EmbeddedId
    private UsersUrlId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", insertable = false, updatable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false, insertable = false, updatable = false)
    private Url url;
    
    
    private boolean isUse;
    
    @Builder
    public UsersUrl(Users user, Url url) {
        this.id = new UsersUrlId(user.getEmail(), url.getId());
        this.user = user;
        this.url = url;
        this.isUse = true;
    }
} 