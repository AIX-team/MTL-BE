package com.example.mytravellink.domain.user.dto;

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
