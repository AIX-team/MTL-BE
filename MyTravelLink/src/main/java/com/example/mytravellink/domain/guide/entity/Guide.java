package com.example.mytravellink.domain.guide.entity;

import com.example.mytravellink.domain.travelinfo.entity.TravelInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/*
 * 가이드 (Guide)
 */
@Entity
@Table(name = "guide")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Guide {
  @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "travel_info_id", nullable = false)
    private TravelInfo travelInfo;

    @Column(name = "course_count", nullable = false)
    private int courseCount = 0;

    @Column(name = "use_count", nullable = false)
    private int useCount = 0;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "travel_days", nullable = false)
    private int travelDays = 0;

    @Column(nullable = false)
    private boolean bookmark = false;

    @Column(nullable = false)
    private boolean fixed = false;

    @Column(name = "is_delete", nullable = false)
    private boolean isDelete = false;

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuideCourseInfo> guideCourseInfos;
}
