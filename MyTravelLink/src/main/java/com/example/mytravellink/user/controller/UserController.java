package com.example.mytravellink.user.controller;

import com.example.mytravellink.common.ResponseMessage;

import com.example.mytravellink.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Member")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class UserController {
    private final UserService memberService;

    // 유저 삭제
    @Operation(summary = "유저 삭제")
    @DeleteMapping("/{memberNo}")
    public ResponseEntity<ResponseMessage> deleteUser(@PathVariable Long memberNo) {

        memberService.deleteUserById(memberNo);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "유저 삭제 성공");

        return ResponseEntity.ok()
                .body(new ResponseMessage(HttpStatus.OK, "유저 삭제 성공", responseMap));

    }

}