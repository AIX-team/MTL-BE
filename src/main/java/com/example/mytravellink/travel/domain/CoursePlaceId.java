package com.example.mytravellink.travel.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class CoursePlaceId {
    private String placeId;
    private String courseId;

    @Builder
    public CoursePlaceId(String placeId, String courseId) {
        this.placeId = placeId;
        this.courseId = courseId;
    }
}