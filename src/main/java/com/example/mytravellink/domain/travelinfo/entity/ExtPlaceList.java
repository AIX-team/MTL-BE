package com.example.mytravellink.domain.travelinfo.entity;

import com.example.mytravellink.domain.user.entity.UserExtPlaceList;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 추출 장소 목록 (Ext_Place_List)
 */
@Entity
@Table(name = "ext_place_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtPlaceList {
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Lob
    @Column(name = "place_list", nullable = false)
    private String placeList;

    @Column(name = "place_count", nullable = false)
    private int placeCount = 0;

    @Column(name = "use_count", nullable = false)
    private int useCount = 0;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    // 사용자 외부 장소 목록
    @OneToMany(mappedBy = "extPlaceList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserExtPlaceList> userExtPlaceLists;

    // 여행 정보
    @OneToMany(mappedBy = "extPlaceList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelInfo> travelInfos;
}