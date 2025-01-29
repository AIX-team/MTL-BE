package com.example.mytravellink.domain.user.entity;

import com.example.mytravellink.domain.travelinfo.entity.TravelInfo;
import com.example.mytravellink.domain.travelinfo.entity.TravelTaste;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원 (User)
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "profile_img", length = 255)
    private String profileImg;

    @Column(name = "is_delete", nullable = false)
    private boolean isDelete = false;

    // 회원 검색어
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSearchTerm> searchTerms;

    // 회원 URL
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserUrl> userUrls;

    // 여행 취향
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelTaste> travelTastes;

    // 외부 장소 목록 연결
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserExtPlaceList> userExtPlaceLists;

    // 여행 정보
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelInfo> travelInfos;
}