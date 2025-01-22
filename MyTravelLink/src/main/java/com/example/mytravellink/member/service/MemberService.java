package com.example.mytravellink.member.service;

import com.example.mytravellink.member.domain.entity.Member;
import com.example.mytravellink.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
//    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;

//    public Member updateUserInfo(Long memberNo, String nickName) {
//        Member foundMember = memberRepository.findById(memberNo).get();
//        foundMember.setNickname(nickName);
//        memberRepository.save(foundMember);
//
//        return foundMember;
//    }

    public void deleteUserById(Long memberNo) {
        Member foundMember = memberRepository.findById(memberNo).get();

        memberRepository.delete(foundMember);
    }
}
