package com.example.beyond.ordersystem.member.dto;

import com.example.beyond.ordersystem.common.domain.Address;
import com.example.beyond.ordersystem.member.domain.Member;
import com.example.beyond.ordersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSaveDto {

    private String name;
    @NotEmpty(message = "email is essential")
    private String email;
    @NotEmpty(message = "password is essential")
    @Size(min = 8, message = "비밀번호는 최소 8자리입니다.")
    private String password;
    private Long age;
    private String phone;
    private Role role = Role.USER;
    private Address address;

    public Member toEntity(String password) {
        return Member.builder()
                .password(password)
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .age(this.age)
                .role(this.role)
                .address(this.address)
                .build();
//        Member member = Member.builder()
//                .password(this.password)
//                .name(this.name)
//                .email(this.email)
//                .phone(this.phone)
//                .role(this.role != null ? this.role : Role.USER)  // 기본값 설정
//                .address(Address.builder()
//                        .city(this.city)
//                        .street(this.street)
//                        .zipcode(this.zipcode)
//                        .build())
//                .build();
//        return member;
    }
}
