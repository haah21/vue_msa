package com.example.beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//일반 클래스 객체, dto/domain 어디에 넣기도 좀.. 그럼
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDecreaseEvent {
    private Long productId;
    private Integer productCount;
}
