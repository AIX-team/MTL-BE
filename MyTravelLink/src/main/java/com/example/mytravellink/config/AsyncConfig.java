package com.example.mytravellink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 최소 10개의 스레드
        executor.setMaxPoolSize(50);  // 최대 50개의 스레드
        executor.setQueueCapacity(100); // 대기 큐 용량
        executor.setThreadNamePrefix("Async-"); // 스레드 이름 prefix
        executor.initialize();
        return executor;
    }
}
