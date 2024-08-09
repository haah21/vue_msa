package com.example.beyond.ordersystem.product.controller;

import com.example.beyond.ordersystem.common.dto.CommonErrorDto;
import com.example.beyond.ordersystem.common.dto.CommonResDto;
import com.example.beyond.ordersystem.product.domain.Product;
import com.example.beyond.ordersystem.product.dto.ProductResDto;
import com.example.beyond.ordersystem.product.dto.ProductSaveDto;
import com.example.beyond.ordersystem.product.dto.ProductSearchDto;
import com.example.beyond.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> productCreatePost(@ModelAttribute ProductSaveDto dto, @RequestParam MultipartFile productImage){
        try {
            Product product = productService.productAwsCreate(dto);
//            Product product = productService.productCreate(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "물품등록에 성공하였습니다.", product.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/list")
    public ResponseEntity<?> productList(ProductSearchDto searchDto, Pageable pageable){
        Page<ProductResDto> productResDtos = productService.productList(searchDto, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "물품 목록을 조회합니다.", productResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
