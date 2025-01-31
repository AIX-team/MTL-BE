package com.example.mytravellink.api.travelInfo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/travel")
@RequiredArgsConstructor
@Slf4j
public class TravelInfoController {

    @GetMapping("/travel/info")
    public String travelInfo() {
        return "travel/info";
    }
}
