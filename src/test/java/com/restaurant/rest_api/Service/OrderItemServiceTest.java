package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.OrderItemRequest;
import com.restaurant.rest_api.dto.OrderItemResponse;
import com.restaurant.rest_api.entity.*;
import com.restaurant.rest_api.exception.OrderItemNotFoundException;
import com.restaurant.rest_api.fixtures.*;
import com.restaurant.rest_api.repository.MenuItemRepository;
import com.restaurant.rest_api.repository.OrderItemRepository;
import com.restaurant.rest_api.repository.OrderRepository;
import com.restaurant.rest_api.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderItemServiceTest {

    @Mock
    public OrderItemRepository orderItemRepository;

    @Mock
    public OrderRepository orderRepository;

    @Mock
    public MenuItemRepository menuItemRepository;

    @InjectMocks
    public OrderItemService orderItemService;


    @Test
    public void createOrderItem(){
        Order order = OrderFixture.buildOrder();
        OrderItem orderItem = OrderItemFixture.buildOrderItem();
        MenuItem menuItem = MenuItemFixture.buildMenuItem();
        OrderItemRequest request = new OrderItemRequest(menuItem.getId(), 1,"test");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(menuItem.getId())).thenReturn(Optional.of(menuItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        OrderItemResponse result = orderItemService.createOrderItem(order.getId(), request);

        assertEquals(menuItem.getId(), result.menuItemId());
        assertEquals(orderItem.getUnitPrice(), result.unitPrice());
        assertEquals(orderItem.getObservation(), result.observation());
        assertEquals(1, result.quantity());
        assertEquals(OrderItemStatus.READY, result.orderItemStatus());
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(orderRepository).save(order);
    }

    @Test
    public void updateOrderItemStatus(){
        OrderItem orderItem = OrderItemFixture.buildOrderItem();


        when(orderItemRepository.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        OrderItemResponse result = orderItemService.updateItemStatus(OrderItemStatus.PREPARING, orderItem.getId());

        assertEquals(OrderItemStatus.PREPARING, result.orderItemStatus());
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    public void updateOrderItemStatus_shouldThrowException_whenOrderItemNotFound(){
        when(orderItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.updateItemStatus(OrderItemStatus.CANCELLED, 999L);
        });
    }

    @Test
    public void removeOrderItem_whenOrderIsOpen_shouldRecalculateTotalAndDelete(){
        OrderItem orderItem = OrderItemFixture.buildOrderItem();
        BigDecimal originalTotal = orderItem.getOrder().getTotalPrice();
        BigDecimal expectedSubtotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        when(orderItemRepository.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));

        orderItemService.removeItem(orderItem.getId());

        assertEquals(originalTotal.subtract(expectedSubtotal), orderItem.getOrder().getTotalPrice());
        verify(orderRepository).save(orderItem.getOrder());
        verify(orderItemRepository).delete(orderItem);
    }

    @Test
    public void removeOrderItem_whenOrderIsClosed_shouldKeepOriginalTotal(){
        Order closedOrder = OrderFixture.buildOrder(
                1L, UserFixture.buildUser(), RestaurantTableFixture.buildTable(),
                OrderStatus.CLOSED, new BigDecimal("10.00"), LocalDateTime.now()
        );
        OrderItem orderItem = OrderItemFixture.buildOrderItem(
                1L, closedOrder, MenuItemFixture.buildMenuItem(), 1, new BigDecimal("10.00"), null, OrderItemStatus.READY
        );

        BigDecimal originalTotal = orderItem.getOrder().getTotalPrice();

        when(orderItemRepository.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));

        orderItemService.removeItem(orderItem.getId());

        assertEquals(originalTotal, orderItem.getOrder().getTotalPrice());
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository).delete(orderItem);
    }
}
