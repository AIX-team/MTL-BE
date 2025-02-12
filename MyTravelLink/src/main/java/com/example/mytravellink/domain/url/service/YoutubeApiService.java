package com.example.mytravellink.domain.url.service;

import org.springframework.stereotype.Service;

@Service
public class YoutubeApiService {
    /**
     * 주어진 유튜브 videoId에 대해 자막이 존재하는지 체크.
     * 실제 구현 시 YouTube Data API 등을 활용해 자막 정보를 조회해야 합니다.
     *
     * @param videoId 유튜브 영상 ID
     * @return 자막이 있으면 true, 없으면 false
     */
    public boolean hasSubtitles(String videoId) {
        // 예시 조건입니다.
        // 실제 환경에서는 videoId를 이용하여 API 호출 후 자막 존재 여부를 판단하세요.
        return videoId.length() == 11; // 더 현실적인 조건으로 변경 필요
    }
} 