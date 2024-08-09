package com.example.beyond.ordersystem.common.service;

import com.example.beyond.ordersystem.member.domain.Role;
import com.example.beyond.ordersystem.member.dto.MemberSaveDto;
import com.example.beyond.ordersystem.member.repository.MemberRepository;
import com.example.beyond.ordersystem.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//CommandLineRunner를 상속함으로서 해당 컴포넌트가 스프링빈으로 등록되는 시점(스프링이 시작될때)에 run 메소드 실행
@Component
public class InitialDataLoader implements CommandLineRunner {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Override
    public void run(String... args) throws Exception {
        //admin 계정 서버 실행만 하면 만들어짐
        if (memberRepository.findByEmail("admin@test.com").isEmpty()){
            memberService.memberCreate(MemberSaveDto.builder()
                    .name("admin")
                    .email("admin@test.com") //같은 이메일 있으면 충돌
                    .password("12341234")
                    .role(Role.ADMIN)
                    .build());
        }
    }
}
