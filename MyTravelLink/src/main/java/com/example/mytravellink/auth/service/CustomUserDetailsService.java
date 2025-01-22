package com.example.mytravellink.auth.service;

import com.example.mytravellink.member.domain.entity.Member;
import com.example.mytravellink.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository; // 사용자 정보를 가져오는 레포지토리

    @Override
    public UserDetails loadUserByUsername(String memberNo) throws UsernameNotFoundException {

        try {
            // memberNo를 long 타입으로 변환
            long id = Long.parseLong(memberNo);

            // memberRepository에서 회원 조회
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new AuthenticationServiceException("가입되지 않은 회원입니다."));

            return new CustomUserDetails(member);
        } catch (NumberFormatException e) {
            throw new AuthenticationServiceException("회원 번호 형식이 올바르지 않습니다.", e);
        }
    }

}
