package com.example.beyond.ordersystem.member.domain;

import com.example.beyond.ordersystem.common.domain.Address;
import com.example.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.example.beyond.ordersystem.member.dto.MemberDetailDto;
import com.example.beyond.ordersystem.member.dto.MemberListDto;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    @Embedded
    private Address address;
    private String name;
    private Long age;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public MemberListDto listFromEntity() {
        return MemberListDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .build();
    }
    public MemberDetailDto detailFromEntity() {
        return MemberDetailDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .age(this.age)
                .build();
    }

    public void updatePassword(String password){
        this.password = password;
    }
}

