package com.example.beyond.ordersystem.ordering.service;

import com.example.beyond.ordersystem.common.service.StockInventoryService;
import com.example.beyond.ordersystem.member.domain.Member;
import com.example.beyond.ordersystem.member.repository.MemberRepository;
import com.example.beyond.ordersystem.ordering.controller.SseController;
import com.example.beyond.ordersystem.ordering.domain.OrderDetail;
import com.example.beyond.ordersystem.ordering.domain.OrderStatus;
import com.example.beyond.ordersystem.ordering.domain.Ordering;
import com.example.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.example.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.example.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import com.example.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.example.beyond.ordersystem.product.domain.Product;
import com.example.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, ProductRepository productRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, com.example.beyond.ordersystem.ordering.service.StockDecreaseEventHandler stockDecreaseEventHandler, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
        this.sseController = sseController;
    }

    @Transactional
    public Ordering orderCreate(@ModelAttribute List<OrderSaveReqDto> dtos) {

        // 방법3 : 스프링 시큐리티를 통한 주문 생성(토큰을 통한 사용자 인증), (getName = email)
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 중요 !!
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        // OrderDetail생성 : order_id, product_id, quantity
        for (OrderSaveReqDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 상품입니다."));
            int quantity = dto.getProductCount();
            if (product.getName().contains("sale")){
                //redis를 통한 재고관리 및 재고 잔량 확인
                int newQuantity = stockInventoryService.decreaseStock(dto.getProductId(), dto.getProductCount()).intValue(); //= 잔량
                //예외처리
                if (newQuantity<0){
                    throw new IllegalArgumentException("재고가 부족합니다");
                    //rdb에 재고 update -> 갱신이상 -> deadLock 발생
                    //rabbitmq를 통해 비동기적으로 이벤트 처리
                }
                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), dto.getProductCount()));
            }else {
                if(quantity > product.getStock_quantity()){
                    throw new IllegalArgumentException("재고가 부족합니다");
                }else {
                    // 변경감지로 인해 별도의 save 불필요 //동시성 이슈 발생 가능
                    // 서로 자원을 점유해서 모두 update 못하는 경우 생김
                    product.updateStockQuantity(quantity);
                }
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
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

//        //        방법1.쉬운방식
////        Ordering생성 : member_id, status
//        Member member = memberRepository.findById(dto.getMember_id()).orElseThrow(() -> new EntityNotFoundException("없음"));
//        Ordering ordering = orderingRepository.save(dto.toEntity(member));
//
////        OrderDetail생성 : order_id, product_id, quantity
//        for (OrderSaveReqDto.OrderDetailDto orderDto : dto.getOrderDetailDtoList()) {
//            Product product = productRepository.findById(orderDto.getProductId()).orElse(null);
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }
//        return ordering;
//    }

        // 방법2 : JPA 최적화된 방식
        // Ordering 생성: member_id, status
//        Member member = memberRepository.findById(dto.getMember_id())
//                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
//
//        Ordering ordering = Ordering.builder()
//                .member(member)
//                .build();
//        // OrderDetail생성 : order_id, product_id, quantity
//        for (OrderSaveReqDto.OrderDetailDto orderDto : dto.getOrderDetailDtoList()) {
//            Product product = productRepository.findById(orderDto.getProductId())
//                    .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 상품입니다."));
//            int quantity = orderDto.getProductCount();
//            if(quantity > product.getStock_quantity()){
//                throw new IllegalArgumentException("재고가 부족합니다");
//            }else {
//                // 변경감지로 인해 별도의 save 불필요
//                product.UpdatStockQuantity(quantity);
//            }
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    // orderingRepository.save(ordering);을 하지 않아,
//                    // ordering_id 는 아직 생성되지 않았지만, JPA가 자동으로 순서를 정렬하여 ordering_id 를 삽입한다.
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//        Ordering savedOreder = orderingRepository.save(ordering);
//        return savedOreder;
//    }
    }

    public List<OrderListResDto> orderList (){
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering ordering : orderings){
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }

    public List<OrderListResDto> myOders (){
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("member is not found"));
        List<Ordering> orderings = orderingRepository.findByMember(member);
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
