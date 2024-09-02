package com.example.beyond.ordersystem.product.controller;

import com.example.beyond.ordersystem.common.dto.CommonErrorDto;
import com.example.beyond.ordersystem.common.dto.CommonResDto;
import com.example.beyond.ordersystem.common.dto.ProductUpdateStockDto;
import com.example.beyond.ordersystem.product.domain.Product;
import com.example.beyond.ordersystem.product.dto.ProductResDto;
import com.example.beyond.ordersystem.product.dto.ProductSaveDto;
import com.example.beyond.ordersystem.product.dto.ProductSearchDto;
import com.example.beyond.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//해당 어노테이션 사용시 아래 bean는 실시간 config 변경사항의 대상이 됨
@RefreshScope
@RestController
@RequestMapping("product")
public class ProductController {

    @Value("${message.hello}")
    private String helloWorld;

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/config/test")
    public String configTest(){
        return helloWorld;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id){
        ProductResDto dto = productService.productDetail(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "물품 목록을 조회합니다.", dto);
        return  new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //patch가 맞긴한데 나중에 추가된 설정이라 rest에서 지원안해줘서 put사용
    @PutMapping("/updatestock")
    public ResponseEntity<?> productStockUpdate(@RequestBody ProductUpdateStockDto dto){
        Product product = productService.productUpdateStock(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "update is successfulll", product.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
