package com.example.mytravellink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true);
    }

    // SPA 라우팅을 위한 설정
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // API 요청은 제외하고 나머지만 index.html로 포워딩
        registry.addViewController("/")
               .setViewName("forward:/index.html");
        registry.addViewController("/login/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/loginSuccess/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/link/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/guide/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/mypage/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/wish/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/travel/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/travelInfos/**")
               .setViewName("forward:/index.html");
        registry.addViewController("/guidebooks/**")
               .setViewName("forward:/index.html");

        // 필요한 프론트엔드 라우트 추가
        
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
} 