package com.restaurant.rest_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.rest_api.dto.OrderItemResponse;
import com.restaurant.rest_api.dto.OrderRequest;
import com.restaurant.rest_api.dto.OrderResponse;
import com.restaurant.rest_api.entity.OrderItemStatus;
import com.restaurant.rest_api.entity.OrderStatus;
import com.restaurant.rest_api.exception.OrderNotFoundException;
import com.restaurant.rest_api.exception.OrderNotOpenException;
import com.restaurant.rest_api.exception.TableNotAvailableException;
import com.restaurant.rest_api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponse buildResponse(OrderStatus status, List<OrderItemResponse> items){
        return new OrderResponse(1L, status, new BigDecimal("30.00"), LocalDateTime.now(), 1L, 1L, items);
    }

    @Test
    public void createOrder_shouldReturn201() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(buildResponse(OrderStatus.OPEN, List.of()));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest(1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.tableId").value(1L));
    }

    @Test
    public void createOrder_shouldReturn400_whenTableIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tableId").exists());

        verify(orderService, never()).createOrder(any(OrderRequest.class));
    }

    @Test
    public void createOrder_shouldReturn409_whenTableIsNotAvailable() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class))).thenThrow(new TableNotAvailableException(1L));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest(1L))))
                .andExpect(status().isConflict());
    }

    @Test
    public void findOrderById_shouldReturn200WithItems() throws Exception {
        OrderItemResponse item = new OrderItemResponse(
                1L, 1L, "menuItemTest", 2, new BigDecimal("15.00"), null, OrderItemStatus.PENDING
        );
        when(orderService.findById(1L)).thenReturn(buildResponse(OrderStatus.OPEN, List.of(item)));

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].menuItemName").value("menuItemTest"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    public void findOrderById_shouldReturn404_whenNotFound() throws Exception {
        when(orderService.findById(999L)).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void closeOrder_shouldReturn200() throws Exception {
        when(orderService.closeOrder(1L)).thenReturn(buildResponse(OrderStatus.CLOSED, List.of()));

        mockMvc.perform(patch("/api/v1/orders/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    public void closeOrder_shouldReturn400_whenOrderIsNotOpen() throws Exception {
        when(orderService.closeOrder(1L)).thenThrow(new OrderNotOpenException(1L));

        mockMvc.perform(patch("/api/v1/orders/1/close"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cancelOrder_shouldReturn200() throws Exception {
        when(orderService.cancelOrder(1L)).thenReturn(buildResponse(OrderStatus.CANCELLED, List.of()));

        mockMvc.perform(patch("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
