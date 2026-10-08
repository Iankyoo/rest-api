package com.restaurant.rest_api.repository;

import com.restaurant.rest_api.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = "menuItem")
    List<OrderItem> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = "menuItem")
    List<OrderItem> findByOrderIdIn(Collection<Long> orderIds);
}
