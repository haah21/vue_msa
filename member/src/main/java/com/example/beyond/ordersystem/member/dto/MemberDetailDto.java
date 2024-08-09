package com.example.beyond.ordersystem.member.dto;

import com.example.beyond.ordersystem.common.domain.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberDetailDto {
    private Long id;
    private String email;
    private String name;
    private Long age;
    private String phone;
    private Address address;
}
