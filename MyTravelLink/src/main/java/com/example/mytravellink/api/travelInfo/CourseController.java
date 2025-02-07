package com.example.mytravellink.api.travelInfo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mytravellink.api.travelInfo.dto.course.CoursePlaceRequest;
import com.example.mytravellink.domain.travel.service.CourseService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PutMapping("/")
    public ResponseEntity<String> updateCoursePlace(@RequestBody CoursePlaceRequest request) {
        try {

            List<String> placeIds = request.getPlaceIds();
                
            courseService.updateCoursePlace(request.getId(), placeIds);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

}

