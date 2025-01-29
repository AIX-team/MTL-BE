package com.example.mytravellink.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 회원 검색어 (User_Search_Term)
 */
@Entity
@Table(name = "user_search_term")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email", nullable = false)
    private User user;

    @Column(name = "word", length = 100, nullable = false)
    private String word;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();
}