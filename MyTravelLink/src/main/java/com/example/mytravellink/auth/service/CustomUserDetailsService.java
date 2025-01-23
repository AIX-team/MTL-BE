package com.example.mytravellink.auth.service;

import com.example.mytravellink.member.domain.entity.Member;
import com.example.mytravellink.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository; // 사용자 정보를 가져오는 레포지토리

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

       Member member = memberRepository.findByEmail(email)
               .orElseThrow(()-> new UsernameNotFoundException("가입 되지 않은 회원입니다."));

       return new CustomUserDetails(member);
    }
}
