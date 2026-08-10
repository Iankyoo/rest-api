package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.OrderRequest;
import com.restaurant.rest_api.dto.OrderResponse;
import com.restaurant.rest_api.entity.*;
import com.restaurant.rest_api.exception.OrderNotFoundException;
import com.restaurant.rest_api.exception.RestaurantTableNotFoundException;
import com.restaurant.rest_api.exception.TableNotAvailableException;
import com.restaurant.rest_api.fixtures.OrderFixture;
import com.restaurant.rest_api.fixtures.RestaurantTableFixture;
import com.restaurant.rest_api.fixtures.UserFixture;
import com.restaurant.rest_api.repository.OrderRepository;
import com.restaurant.rest_api.repository.RestaurantTableRepository;
import com.restaurant.rest_api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private RestaurantTableRepository tableRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void createOrder(){
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            User user = UserFixture.buildUser();
            RestaurantTable table = RestaurantTableFixture.buildTable();
            OrderRequest request = new OrderRequest(table.getId());
            Order order = OrderFixture.buildOrder();

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            OrderResponse result = orderService.createOrder(request);

            assertEquals(order.getUser().getId(), result.userId());
            assertEquals(order.getTotalPrice(), result.totalPrice());
            assertEquals(OrderStatus.OPEN, result.status());
            assertEquals(TableStatus.OCCUPIED, table.getStatus());
            verify(orderRepository).save(any(Order.class));
            verify(tableRepository).save(table);
        }
    }

    @Test
    public void createOrder_shouldThrowException_whenTableIsOccupied(){
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)){
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            User user = UserFixture.buildUser();
            RestaurantTable table = RestaurantTableFixture.buildTable(
                    1L,
                    1,
                    TableStatus.OCCUPIED,
                    8
            );
            OrderRequest request = new OrderRequest(table.getId());

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));


            assertThrows(TableNotAvailableException.class, () -> {
                orderService.createOrder(request);
            });
        }
    }

    @Test
    public void createOrder_shouldThrowException_whenTableNotFound(){
        OrderRequest request = new OrderRequest(999L);
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RestaurantTableNotFoundException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    public void closeOrder(){
        Order order = OrderFixture.buildOrder();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.closeOrder(order.getId());

        assertEquals(OrderStatus.CLOSED, order.getStatus());
        assertEquals(TableStatus.AVAILABLE, order.getRestaurantTable().getStatus());
        assertEquals(OrderStatus.CLOSED, result.status());
        verify(orderRepository).save(order);
        verify(tableRepository).save(order.getRestaurantTable());

    }

    @Test
    public void cancelOrder(){
        Order order = OrderFixture.buildOrder();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

         OrderResponse result = orderService.cancelOrder(order.getId());

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(TableStatus.AVAILABLE, order.getRestaurantTable().getStatus());
        assertEquals(OrderStatus.CANCELLED, result.status());
        verify(orderRepository).save(order);
        verify(tableRepository).save(order.getRestaurantTable());
    }

    @Test
    public void findById(){
        Order order = OrderFixture.buildOrder();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        OrderResponse result = orderService.findById(order.getId());

        assertEquals(order.getId(), result.id());
        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void findById_shouldThrowException_withIdNotExist(){
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.findById(999L);
        });
    }

    @Test
    public void findAll_shouldReturnPagedOrders(){
        Order order = OrderFixture.buildOrder();
        Pageable pageable = PageRequest.of(0,10);
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(pageable)).thenReturn(page);

        Page<OrderResponse> result = orderService.findAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(orderRepository).findAll(pageable);
    }
}
