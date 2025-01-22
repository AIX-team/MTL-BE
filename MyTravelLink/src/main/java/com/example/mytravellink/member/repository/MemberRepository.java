package com.example.mytravellink.member.repository;

import com.example.mytravellink.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByGoogleId(String googleId);


    Optional<Member> findByEmail(String email);

}
