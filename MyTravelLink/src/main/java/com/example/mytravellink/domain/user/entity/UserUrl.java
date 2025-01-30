package com.example.mytravellink.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 회원 URL (User_Url)
 */
@Entity
@Table(name = "user_url")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email", nullable = false)
    private User user;

    @Column(name = "url", length = 255, nullable = false)
    private String url;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();

    @Column(name = "is_use", nullable = false)
    private boolean isUse = true;
}