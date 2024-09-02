package com.example.beyond.ordersystem.ordering.service;

import com.example.beyond.ordersystem.common.dto.CommonResDto;
import com.example.beyond.ordersystem.common.service.StockInventoryService;
import com.example.beyond.ordersystem.ordering.controller.SseController;
import com.example.beyond.ordersystem.ordering.domain.OrderDetail;
import com.example.beyond.ordersystem.ordering.domain.OrderStatus;
import com.example.beyond.ordersystem.ordering.domain.Ordering;
import com.example.beyond.ordersystem.ordering.dto.*;
import com.example.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
//    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, SseController sseController, RestTemplate restTemplate, ProductFeign productFeign) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
//        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
        this.sseController = sseController;
        this.restTemplate = restTemplate;
        this.productFeign = productFeign;
//        this.kafkaTemplate = kafkaTemplate;
    }

    // 1. 조회/변경시 restTemplate 사용 -> 동기
    // 2. 조회/변경시 feignclient 사용 -> 동기
    // 3. 조회 : feignclient(동기), 변경 : kafka(비동기)
    //동기 비동기적으로 서버간에 어떻게 실행하는지
    @Transactional
    public Ordering orderRestTemplateCreate(@ModelAttribute List<OrderSaveReqDto> dtos) {
        // 방법3 : 스프링 시큐리티를 통한 주문 생성(토큰을 통한 사용자 인증), (getName = email)
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 중요 !!
        Ordering ordering = Ordering.builder()
                .memberEmail(memberEmail)
                .build();
//         OrderDetail생성 : order_id, product_id, quantity
        for (OrderSaveReqDto dto : dtos) {
            int quantity = dto.getProductCount();
            //Product Dto 형태로 받아옴
            String productGetUrl = "http://product-service/product/"+dto.getProductId();
            HttpHeaders httpHeaders = new HttpHeaders();
            String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
            httpHeaders.set("Authorization",token);
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<CommonResDto> productEntity = restTemplate.exchange(productGetUrl, HttpMethod.GET, entity,CommonResDto.class);
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(productEntity.getBody().getResult(), ProductDto.class);
            System.out.println(productDto);
            if (productDto.getName().contains("sale")){
                //redis를 통한 재고관리 및 재고 잔량 확인
                int newQuantity = stockInventoryService.decreaseStock(dto.getProductId(), dto.getProductCount()).intValue(); //= 잔량
                //예외처리
                if (newQuantity<0){
                    throw new IllegalArgumentException("재고가 부족합니다");
                    //rdb에 재고 update -> 갱신이상 -> deadLock 발생
                    //rabbitmq를 통해 비동기적으로 이벤트 처리
                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), dto.getProductCount()));
            }else {
                if(quantity > productDto.getStock_quantity()){
                    throw new IllegalArgumentException("재고가 부족합니다");
                }else {
                    // restTemplate를 통항 update 요청
//                    product.updateStockQuantity(quantity);
                    String updateUrl = "http://product-service/product/updatestock";
                    //동기적 호출 -> data를 기다려야하므로 속도가 느림 => kafka로 해결가능
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(new ProductUpdateStockDto(dto.getProductId(), dto.getProductCount()), httpHeaders);
                    restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, Void.class);
                }
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(productDto.getId())
                    .quantity(quantity)
                    .ordering(ordering)
                    // orderingRepository.save(ordering);을 하지 않아,
                    // ordering_id 는 아직 생성되지 않았지만, JPA가 자동으로 순서를 정렬하여 ordering_id 를 삽입한다.
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering saved = orderingRepository.save(ordering);
        sseController.publishMessage(saved.fromEntity(), "admin@test.com");
        //사용자끼리 소통했을때 사용자가 알림 받으려면
        //sseController.publishMessage(saved.fromEntity(), 사용자 이메일);
        return saved;
    }

    //이거 쓰게 될거임 이게 최고 최고 최최고
    public Ordering orderFeignClientCreate(@ModelAttribute List<OrderSaveReqDto> dtos) {

        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 중요 !!
        Ordering ordering = Ordering.builder()
                .memberEmail(memberEmail)
                .build();
        for (OrderSaveReqDto dto : dtos) {
            int quantity = dto.getProductCount();
            //ResponseEntity가 기본 응답값이므로 바로 CommonResDto로 매핑
            CommonResDto commonResDto = productFeign.getProductById(dto.getProductId());
            ObjectMapper objectMapper = new ObjectMapper();
            //ProductDto.class 로 자동 형변환
            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(), ProductDto.class);
            System.out.println(productDto);
            if (productDto.getName().contains("sale")){
                int newQuantity = stockInventoryService.decreaseStock(dto.getProductId(), dto.getProductCount()).intValue(); //= 잔량
                if (newQuantity<0){
                    throw new IllegalArgumentException("재고가 부족합니다");
                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), dto.getProductCount()));
            }else {
                if(quantity > productDto.getStock_quantity()){
                    throw new IllegalArgumentException("재고가 부족합니다");
                }else {
                    productFeign.updateProductStock(ProductUpdateStockDto.builder()
                            .productId(dto.getProductId())
                            .productQuantity(dto.getProductCount())
                            .build());
                }
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(productDto.getId())
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering saved = orderingRepository.save(ordering);
        sseController.publishMessage(saved.fromEntity(), "admin@test.com");
        return saved;
    }

    //조회는 kafka 못씀

//    public Ordering orderFeignKafkaCreate(@ModelAttribute List<OrderSaveReqDto> dtos) {
//        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 중요 !!
//        Ordering ordering = Ordering.builder()
//                .memberEmail(memberEmail)
//                .build();
//        for (OrderSaveReqDto dto : dtos) {
//            int quantity = dto.getProductCount();
//            //ResponseEntity가 기본 응답값이므로 바로 CommonResDto로 매핑
//            CommonResDto commonResDto = productFeign.getProductById(dto.getProductId());
//            ObjectMapper objectMapper = new ObjectMapper();
//            //ProductDto.class 로 자동 형변환
//            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(), ProductDto.class);
//            System.out.println(productDto);
//            if (productDto.getName().contains("sale")){
//                int newQuantity = stockInventoryService.decreaseStock(dto.getProductId(), dto.getProductCount()).intValue(); //= 잔량
//                if (newQuantity<0){
//                    throw new IllegalArgumentException("재고가 부족합니다");
//                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), dto.getProductCount()));
//            }else {
//                if(quantity > productDto.getStock_quantity()){
//                    throw new IllegalArgumentException("재고가 부족합니다");
//                }else {
//                    //여기만 수정
//                    ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(dto.getProductId(), dto.getProductCount());
//                    kafkaTemplate.send("product-update-topic", productUpdateStockDto);
//
//                    productFeign.updateProductStock(ProductUpdateStockDto.builder()
//                            .productId(dto.getProductId())
//                            .productQuantity(dto.getProductCount())
//                            .build());
//                }
//            }
//
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .productId(productDto.getId())
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//        Ordering saved = orderingRepository.save(ordering);
//        sseController.publishMessage(saved.fromEntity(), "admin@test.com");
//        return saved;
//    }

    public List<OrderListResDto> orderList (){
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering ordering : orderings){
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }

    public List<OrderListResDto> myOders (){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Ordering> orderings = orderingRepository.findByMemberEmail(email);
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering ordering : orderings){
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("not found"));

        ordering.updateStatus(OrderStatus.CANCELED);
        return ordering;
    }
}
