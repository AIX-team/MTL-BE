package com.example.mytravellink.domain.user.service;

import com.example.mytravellink.domain.user.entity.User;
import com.example.mytravellink.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
//    private final ModelMapper modelMapper;
    private final UserRepository memberRepository;

//    public Member updateUserInfo(Long memberNo, String nickName) {
//        Member foundMember = memberRepository.findById(memberNo).get();
//        foundMember.setNickname(nickName);
//        memberRepository.save(foundMember);
//
//        return foundMember;
//    }

    public void deleteUserById(Long memberNo) {
        User foundMember = memberRepository.findById(memberNo).get();

        memberRepository.delete(foundMember);
    }
}
