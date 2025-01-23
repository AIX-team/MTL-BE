package com.example.mytravellink.user.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class UpdateUserDTO {

    private Long memberNo;
    private String nickname;
}
