package com.example.mytravellink.domain.travel.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * 복합키
 * CoursePlace 엔티티의 복합키를 정의하는 클래스입니다.
 * courseId와 placeId를 합쳐서 하나의 복합키로 사용합니다.
 */
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CoursePlaceId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "course_id")
    private String courseId;
    
    @Column(name = "place_id")
    private String placeId;

    @Builder
    public CoursePlaceId(String courseId, String placeId) {
        this.courseId = courseId;
        this.placeId = placeId;
    }
}
