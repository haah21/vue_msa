//package com.example.beyond.ordersystem.ordering.service;
//
//import com.example.beyond.ordersystem.common.config.RabbitMqConfig;
//import com.example.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.EntityNotFoundException;
//
//@Component
//public class StockDecreaseEventHandler {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    public void publish(StockDecreaseEvent event){
//        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
//    }
//
//    //transaction이 완료된 이후에 그다음에 메세지 수신하므로, 동시성 이슈 발생 x
//    @Transactional //component 붙어있으면 Transactional가능 (error 발생시 -> rollback 가능)
//    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
//    public void listen(Message message){
//        //여기서 가져와서 소모
//        String messageBody = new String(message.getBody());
//        System.out.println(messageBody);
//        //여기 코드 외우기
//        //json 메세지를 parsing(Object Mapper로 직접 파싱) - stockDecreaeseEvent 객체로 만든 다음에
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            StockDecreaseEvent stockDecreaseEvent = objectMapper.readValue(messageBody, StockDecreaseEvent.class);
//            //ProductRepository 필요
//            //재고 update
////            Product product = productRepository.findById(stockDecreaseEvent.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
////            product.updateStockQuantity(stockDecreaseEvent.getProductCount());
//        } catch (JsonProcessingException e) { //checked
//            throw new RuntimeException(e);  //unchecked -> 트랜잭션 처리됨 -> rollback 가능
//        }
//
//
//    }
//}
