package com.restaurant.rest_api.controller;

import com.restaurant.rest_api.dto.OrderItemRequest;
import com.restaurant.rest_api.dto.OrderItemResponse;
import com.restaurant.rest_api.dto.UpdateStatusRequest;
import com.restaurant.rest_api.service.OrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderItemController {
    private final OrderItemService service;

    @PostMapping("/orders/{orderId}/items")
    public ResponseEntity<OrderItemResponse> addItem(@PathVariable Long orderId, @RequestBody @Valid OrderItemRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrderItem(orderId, request));
    }

    @PatchMapping("/items/{itemId}/status")
    public ResponseEntity<OrderItemResponse> updateStatus(@PathVariable Long itemId, @RequestBody @Valid UpdateStatusRequest request){
        return ResponseEntity.ok(service.updateItemStatus(request.orderItemStatus(), itemId));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId){
        service.removeItem(itemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
