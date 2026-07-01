package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.OrderRequest;
import com.restaurant.rest_api.dto.OrderResponse;
import com.restaurant.rest_api.entity.*;
import com.restaurant.rest_api.exception.OrderNotFoundException;
import com.restaurant.rest_api.exception.RestaurantTableNotFoundException;
import com.restaurant.rest_api.exception.TableNotAvailableException;
import com.restaurant.rest_api.exception.UserNotFoundException;
import com.restaurant.rest_api.repository.OrderRepository;
import com.restaurant.rest_api.repository.RestaurantTableRepository;
import com.restaurant.rest_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;

    private RestaurantTable findTable(Long id){
        return tableRepository.findById(id)
                .orElseThrow(() -> new RestaurantTableNotFoundException(id));
    }

    private OrderResponse toResponse(Order order){
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getRestaurantTable().getId(),
                order.getUser().getId(),
                List.of()
        );
    }

    public Page<OrderResponse> findAll(Pageable pageable){
        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public OrderResponse findById(Long id){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    public OrderResponse createOrder(OrderRequest request){
        RestaurantTable currentTable = findTable(request.tableId());

        User currentUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

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

        RestaurantTable updateTableStatus = tableRepository.save(currentTable);
        Order saved = orderRepository.save(newOrder);
        return toResponse(saved);
    }

    public OrderResponse closeOrder(Long id){
        Order toClose = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        toClose.setStatus(OrderStatus.CLOSED);
        toClose.getRestaurantTable().setStatus(TableStatus.AVAILABLE);

        tableRepository.save(toClose.getRestaurantTable());
        Order saved = orderRepository.save(toClose);
        return toResponse(saved);
    }

    public OrderResponse cancelOrder(Long id){
        Order toCancel = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        toCancel.setStatus(OrderStatus.CANCELLED);
        toCancel.getRestaurantTable().setStatus(TableStatus.AVAILABLE);

        tableRepository.save(toCancel.getRestaurantTable());
        Order saved = orderRepository.save(toCancel);
        return toResponse(saved);
    }

}
