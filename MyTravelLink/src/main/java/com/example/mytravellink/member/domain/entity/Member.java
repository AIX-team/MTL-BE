package com.example.mytravellink.member.domain.entity;

import com.nimbusds.openid.connect.sdk.claims.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Member {

    @Id
    @Column(name= "email", nullable = false)
    private String email;

    @Column(name= "name", nullable = false)
    private String name;

    @Column(name = "dob", nullable = false)
    private Date dob;

    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "create_at", nullable = false)
    private Date createAt;

    @Column(name = "profile_img", nullable = true)
    private String profileImg;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete;

}

