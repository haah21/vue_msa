package com.example.beyond.ordersystem.common.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {
    //token 작업 공동화
    @Bean
    public RequestInterceptor requestInterceptor(){
        return request -> {
            //모든 feign 요청에 전역적으로 token을 세팅할 수 있음
            String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
            request.header(HttpHeaders.AUTHORIZATION, token);
        };
    }
}
