package com.restaurant.rest_api.controller;

import com.restaurant.rest_api.dto.MenuItemRequest;
import com.restaurant.rest_api.dto.MenuItemResponse;
import com.restaurant.rest_api.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menuitems")
public class MenuItemController {
    private final MenuItemService menuItemService;

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(@RequestBody @Valid MenuItemRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.createMenuItem(request));
    }

    @GetMapping
    public ResponseEntity<Page<MenuItemResponse>> findAll(Pageable pageable){
        return ResponseEntity.ok(menuItemService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(menuItemService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long id, @RequestBody @Valid MenuItemRequest request){
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id){
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
