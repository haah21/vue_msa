package com.example.beyond.ordersystem.product.dto;

import com.example.beyond.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSaveDto {
    private String name;
    private String category;
    private Integer price;
    private Integer stock_quantity;
    private MultipartFile productImage;


    public Product toEntity(){
        return Product.builder()
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stock_quantity(this.stock_quantity)
                .build();
    }
}
