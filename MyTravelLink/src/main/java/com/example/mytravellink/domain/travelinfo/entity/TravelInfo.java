package com.example.mytravellink.domain.travelinfo.entity;

import com.example.mytravellink.domain.guide.entity.Guide;
import com.example.mytravellink.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/*
 * 여행 정보 (Travel_Info)
 */
@Entity
@Table(name = "travel_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TravelInfo {
  
  @Id
  @Column(name = "id", length = 36, nullable = false)
  private String id;

  @ManyToOne
  @JoinColumn(name = "email", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "ext_place_list_id", nullable = false)
  private ExtPlaceList extPlaceList;

  @ManyToOne
  @JoinColumn(name = "travel_taste_id", nullable = false)
  private TravelTaste travelTaste;

  @Column(name = "place_count", nullable = false)
  private int placeCount = 0;

  @Column(name = "use_count", nullable = false)
  private int useCount = 0;

  @Column(length = 100, nullable = false)
  private String title;

  @Column(name = "create_at", nullable = false)
  private LocalDateTime createAt = LocalDateTime.now();

  @Column(nullable = false)
  private boolean bookmark = false;

  @Column(nullable = false)
  private boolean fixed = false;

  @Column(name = "is_delete", nullable = false)
  private boolean isDelete = false;

  @OneToMany(mappedBy = "travelInfo", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Guide> guides;
}
