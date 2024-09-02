package com.example.beyond.ordersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {
    private String name;
    private String category;
    private Integer price;
    private Integer stock_quantity;
    private String imagePath;


}
