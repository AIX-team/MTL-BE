package com.example.mytravellink.domain.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mytravellink.domain.users.entity.Users;
import com.example.mytravellink.domain.users.entity.UsersSearchTerm;
import com.example.mytravellink.domain.users.repository.UsersRepository;
import com.example.mytravellink.domain.users.repository.UsersSearchTermRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    
    private final UsersRepository usersRepository;
    private final UsersSearchTermRepository searchTermRepository;
    private static final int MAX_SEARCH_TERMS = 5;  // 최대 검색어 저장 개수

    @Transactional
    public void saveSearchTerm(String email, String searchTerm) {
        Users user = usersRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        // 현재 사용자의 검색어 개수 확인
        List<UsersSearchTerm> userSearchTerms = usersRepository.findSearchTermsByEmailOrderByCreateAtAsc(email);
        
        // 검색어가 5개 이상이면 가장 오래된 검색어부터 삭제
        if (userSearchTerms.size() >= MAX_SEARCH_TERMS) {
            int numberOfTermsToDelete = userSearchTerms.size() - MAX_SEARCH_TERMS + 1;
            for (int i = 0; i < numberOfTermsToDelete; i++) {
                searchTermRepository.delete(userSearchTerms.get(i));
            }
        }
        
        // 새로운 검색어 저장
        UsersSearchTerm searchTermEntity = UsersSearchTerm.builder()
            .user(user)
            .word(searchTerm)
            .build();
            
        searchTermRepository.save(searchTermEntity);
    }

    @Transactional(readOnly = true)
    public List<UsersSearchTerm> getRecentSearches(String email) {
        // Users user = usersRepository.findByEmail(email)
        //     .orElseThrow(() -> new RuntimeException("User not found"));
            
        return usersRepository.findSearchTermsByEmailOrderByCreateAtAsc(email);
    }

    @Transactional(readOnly = true)
    public List<UsersSearchTerm> getSearchTerms(String email) {
        Users user = usersRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
        return searchTermRepository.findByUser(user);
    }
}
