package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.OrderItemRequest;
import com.restaurant.rest_api.dto.OrderItemResponse;
import com.restaurant.rest_api.entity.*;
import com.restaurant.rest_api.exception.*;
import com.restaurant.rest_api.repository.MenuItemRepository;
import com.restaurant.rest_api.repository.OrderItemRepository;
import com.restaurant.rest_api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    private OrderItemResponse toResponse(OrderItem orderItem){
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

    private OrderItem findOrderItem(Long itemId){
        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderItemNotFoundException(itemId));
        return orderItem;
    }

    public OrderItemResponse createOrderItem(Long id,OrderItemRequest request){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.OPEN){
            throw new OrderNotOpenException(id);
        }

        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .orElseThrow(() -> new MenuItemNotFoundException(request.menuItemId()));

        if (!menuItem.getAvailable()){
            throw new MenuItemNotAvailableException(request.menuItemId());
        }

        OrderItem newOrderItem = OrderItem.builder()
                .order(order)
                .menuItem(menuItem)
                .unitPrice(menuItem.getPrice())
                .quantity(request.quantity())
                .orderItemStatus(OrderItemStatus.PENDING)
                .build();

        if (request.observation() != null){
            newOrderItem.setObservation(request.observation());
        }

        BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
        order.setTotalPrice(order.getTotalPrice().add(subtotal));

        orderRepository.save(order);
        OrderItem saved = orderItemRepository.save(newOrderItem);

        return toResponse(saved);
    }

    public OrderItemResponse updateItemStatus(OrderItemStatus newStatus, Long itemId){
        OrderItem orderItem = findOrderItem(itemId);

        orderItem.setOrderItemStatus(newStatus);
        OrderItem saved = orderItemRepository.save(orderItem);
        return toResponse(saved);
    }

    public void removeItem(Long itemId){
        OrderItem toRemove = findOrderItem(itemId);

        if (toRemove.getOrder().getStatus() == OrderStatus.OPEN){
            BigDecimal subtotal = toRemove.getUnitPrice().multiply(BigDecimal.valueOf(toRemove.getQuantity()));

            toRemove.getOrder().setTotalPrice(
                    toRemove.getOrder().getTotalPrice().subtract(subtotal)
            );
            orderRepository.save(toRemove.getOrder());
        }
        orderItemRepository.delete(toRemove);
    }
}
