package com.example.mytravellink.url.domain;

import com.example.mytravellink.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_search_term")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSearchTerm extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email")
    private User user;
    
    @Column(nullable = false)
    private String word;
    
    @Builder
    public UserSearchTerm(User user, String word) {
        this.user = user;
        this.word = word;
    }
} 