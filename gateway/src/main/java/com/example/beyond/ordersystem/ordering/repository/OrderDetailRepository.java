package com.example.beyond.ordersystem.ordering.repository;

import com.example.beyond.ordersystem.ordering.domain.OrderDetail;
import com.example.beyond.ordersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
