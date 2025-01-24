package com.example.mytravellink.user.domain.entity;

import com.nimbusds.openid.connect.sdk.claims.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class User {

    @Id
    @Column(name= "email", nullable = false)
    private String email;

    @Column(name= "name", nullable = false)
    private String name;

    @Column(name = "dob")
    private Date dob;

    @Column(name = "gender")
    private Gender gender;

    @Column(name = "create_at")
    private Date createAt = new Date();

    @Column(name = "profile_img", nullable = true)
    private String profileImg;

    @Column(name = "is_delete")
    private Boolean isDelete=false;

}

