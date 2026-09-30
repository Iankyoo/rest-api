package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.OrderItemResponse;
import com.restaurant.rest_api.dto.OrderRequest;
import com.restaurant.rest_api.dto.OrderResponse;
import com.restaurant.rest_api.entity.*;
import com.restaurant.rest_api.exception.OrderNotFoundException;
import com.restaurant.rest_api.exception.OrderNotOpenException;
import com.restaurant.rest_api.exception.RestaurantTableNotFoundException;
import com.restaurant.rest_api.exception.TableNotAvailableException;
import com.restaurant.rest_api.repository.OrderItemRepository;
import com.restaurant.rest_api.repository.OrderRepository;
import com.restaurant.rest_api.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;
    private final OrderItemRepository orderItemRepository;

    private RestaurantTable findTable(Long id){
        return tableRepository.findById(id)
                .orElseThrow(() -> new RestaurantTableNotFoundException(id));
    }

    private Order findOrder(Long id){
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void validateOrderIsOpen(Order order){
        if (order.getStatus() != OrderStatus.OPEN){
            throw new OrderNotOpenException(order.getId());
        }
    }

    private OrderItemResponse toItemResponse(OrderItem orderItem){
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getMenuItem().getId(),
                orderItem.getMenuItem().getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getObservation(),
                orderItem.getOrderItemStatus()
        );
    }

    private OrderResponse toResponse(Order order){
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getRestaurantTable().getId(),
                order.getUser().getId(),
                items
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable){
        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id){
        Order order = findOrder(id);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request){
        RestaurantTable currentTable = findTable(request.tableId());

        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (currentTable.getStatus() != TableStatus.AVAILABLE){
            throw new TableNotAvailableException(request.tableId());
        }

        Order newOrder = Order.builder()
                .restaurantTable(currentTable)
                .user(currentUser)
                .status(OrderStatus.OPEN)
                .totalPrice(BigDecimal.ZERO)
                .build();

        currentTable.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(currentTable);

        Order saved = orderRepository.save(newOrder);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse closeOrder(Long id){
        Order toClose = findOrder(id);
        validateOrderIsOpen(toClose);

        toClose.setStatus(OrderStatus.CLOSED);
        toClose.getRestaurantTable().setStatus(TableStatus.AVAILABLE);

        tableRepository.save(toClose.getRestaurantTable());
        Order saved = orderRepository.save(toClose);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id){
        Order toCancel = findOrder(id);
        validateOrderIsOpen(toCancel);

        toCancel.setStatus(OrderStatus.CANCELLED);
        toCancel.getRestaurantTable().setStatus(TableStatus.AVAILABLE);

        tableRepository.save(toCancel.getRestaurantTable());
        Order saved = orderRepository.save(toCancel);
        return toResponse(saved);
    }

}
