package com.example.mytravellink.domain.travel.entity;

import com.example.mytravellink.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

/**
 * 코스 (Course) 엔티티
 * 여행 정보와 관련된 코스 정보를 저장합니다.
 * Guide와 일대다 관계를 가지며, 여러 코스를 가질 수 있습니다.
 */
@Entity
@Table(name = "course")
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", columnDefinition = "CHAR(36)")
    private Guide guide;
    
    private int courseNumber;
    private boolean isDelete;
    
    @Builder
    public Course(Guide guide, int courseNumber) {
        this.guide = guide;
        this.courseNumber = courseNumber;
    }
} 