package com.example.mytravellink.domain.guide.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 가이드 코스 정보 (Guide_Course_Info)
 */
@Entity
@Table(name = "guide_course_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuideCourseInfo {
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    @Column(name = "course_number", nullable = false)
    private int courseNumber;

    @Column(name = "place_number", nullable = false)
    private int placeNumber;

    @Lob
    @Column(name = "place_list", nullable = false)
    private String placeList;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();

    @Column(name = "is_delete", nullable = false)
    private boolean isDelete = false;
}