package com.example.mytravellink.domain.user.entity;

import com.example.mytravellink.domain.travelinfo.entity.ExtPlaceList;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 사용자 외부 장소 목록 (User_Ext_Place_List)
 */
@Entity
@Table(name = "user_ext_place_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserExtPlaceList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "ext_place_list_id", nullable = false)
    private ExtPlaceList extPlaceList;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "is_delete", nullable = false)
    private boolean isDelete = false;
}