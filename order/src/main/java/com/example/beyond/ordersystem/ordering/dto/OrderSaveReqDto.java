package com.example.beyond.ordersystem.ordering.dto;

import com.example.beyond.ordersystem.member.domain.Member;
import com.example.beyond.ordersystem.ordering.domain.OrderStatus;
import com.example.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSaveReqDto {

    private Long productId;
    private Integer productCount;

//    private Long member_id;
//    private OrderStatus orderStatus;
//    private List<OrderDetailDto> orderDetailDtoList;
//
//    @Data
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class OrderDetailDto {
//        private Long productId;
//        private Integer productCount;
//    }
}
