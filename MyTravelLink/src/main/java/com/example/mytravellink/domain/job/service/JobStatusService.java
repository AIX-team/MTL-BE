
package com.example.mytravellink.domain.job.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Service
public class JobStatusService {
    private final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();

    @Getter
    @AllArgsConstructor
    public static class JobStatus {
        private String status;    // PENDING, PROCESSING, COMPLETED, FAILED
        private String result;
        private LocalDateTime createdAt;  // 작업 시작 시간 추가
        private LocalDateTime updatedAt;  // 마지막 업데이트 시간 추가
    }

    /**
     * 작업 상태 설정
     * @param jobId 작업 ID
     * @param status 상태
     */
    public void setStatus(String jobId, String status) {
        log.info("Setting job status. JobID: {}, Status: {}", jobId, status);
        jobs.put(jobId, new JobStatus(status, null, null, null));
    }

    /**
     * 작업 결과 설정
     * @param jobId 작업 ID
     * @param result 결과
     */
    public void setResult(String jobId, String result) {
        JobStatus current = jobs.get(jobId);
        String currentStatus = (current != null) ? current.getStatus() : null;
        jobs.put(jobId, new JobStatus(currentStatus, result, null, null));
    }

    /**
     * 작업 상태 조회
     * @param jobId 작업 ID
     * @return 상태
     */
    public String getStatus(String jobId) {
        JobStatus jobStatus = jobs.get(jobId);
        log.debug("Getting job status. JobID: {}, Status: {}", jobId, jobStatus);
        return jobStatus != null ? jobStatus.getStatus() : "Not Found";
    }

    /**
     * 작업 결과 조회
     * @param jobId 작업 ID
     * @return 결과
     */
    public String getResult(String jobId) {
        JobStatus jobStatus = jobs.get(jobId);
        log.debug("Getting job result. JobID: {}, Result: {}", jobId, jobStatus);
        return jobStatus != null ? jobStatus.getResult() : null;
    }

    /**
     * 작업 정보 삭제
     * @param jobId 작업 ID
     */
    public void removeJob(String jobId) {
        log.info("Removing job. JobID: {}", jobId);
        jobs.remove(jobId);
    }

    public void setJobStatus(String jobId, String status, String result) {
        log.info("Job Status Update - ID: {}, Status: {}", jobId, status);
        jobs.put(jobId, new JobStatus(
            status, 
            result,
            jobs.containsKey(jobId) ? jobs.get(jobId).getCreatedAt() : LocalDateTime.now(),
            LocalDateTime.now()
        ));
    }

    public JobStatus getJobStatus(String jobId) {
        return jobs.getOrDefault(jobId, 
            new JobStatus("NOT_FOUND", null, null, null));
    }

    // 오래된 작업 정리
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void cleanupOldJobs() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        jobs.entrySet().removeIf(entry -> 
            entry.getValue().getUpdatedAt().isBefore(threshold));
    }
} 