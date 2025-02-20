package com.example.mytravellink.domain.job.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobStatusService {
    private final Map<String, String> jobStatuses = new ConcurrentHashMap<>();
    private final Map<String, String> jobResults = new ConcurrentHashMap<>();

    /**
     * 작업 상태 설정
     * @param jobId 작업 ID
     * @param status 상태
     */
    public void setStatus(String jobId, String status) {
        log.info("Setting job status. JobID: {}, Status: {}", jobId, status);
        jobStatuses.put(jobId, status);
    }

    /**
     * 작업 결과 설정
     * @param jobId 작업 ID
     * @param result 결과
     */
    public void setResult(String jobId, String result) {
        log.info("Setting job result. JobID: {}, Result: {}", jobId, result);
        jobResults.put(jobId, result);
    }

    /**
     * 작업 상태 조회
     * @param jobId 작업 ID
     * @return 상태
     */
    public String getStatus(String jobId) {
        String status = jobStatuses.get(jobId);
        log.debug("Getting job status. JobID: {}, Status: {}", jobId, status);
        return status != null ? status : "Not Found";
    }

    /**
     * 작업 결과 조회
     * @param jobId 작업 ID
     * @return 결과
     */
    public String getResult(String jobId) {
        String result = jobResults.get(jobId);
        log.debug("Getting job result. JobID: {}, Result: {}", jobId, result);
        return result;
    }

    /**
     * 작업 정보 삭제
     * @param jobId 작업 ID
     */
    public void removeJob(String jobId) {
        log.info("Removing job. JobID: {}", jobId);
        jobStatuses.remove(jobId);
        jobResults.remove(jobId);
    }
} 