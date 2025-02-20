package com.example.mytravellink.domain.job.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobStatusService {
    private final ConcurrentHashMap<String, String> jobStatuses = new ConcurrentHashMap<>();

    public void setStatus(String jobId, String status) {
        jobStatuses.put(jobId, status);
    }

    public String getStatus(String jobId) {
        return jobStatuses.getOrDefault(jobId, "Not Found");
    }

    public void removeStatus(String jobId) {
        jobStatuses.remove(jobId);
    }
} 