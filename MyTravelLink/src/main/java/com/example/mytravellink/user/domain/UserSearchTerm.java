package com.example.mytravellink.user.domain;

import com.example.mytravellink.domain.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;

/**
 * 사용자 검색 키워드 (UserSearchTerm) 엔티티
 * 사용자가 검색한 키워드를 저장합니다.
 * User와 알대일 관계를 가지며, 여러 검색 키워드를 가질 수 있습니다.
 */
@Entity
@Table(name = "user_search_term")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSearchTerm extends BaseTimeEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private String id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email")
    private User user;
    
    private String word;
    
    @Builder
    public UserSearchTerm(User user, String word) {
        this.user = user;
        this.word = word;
    }
} 