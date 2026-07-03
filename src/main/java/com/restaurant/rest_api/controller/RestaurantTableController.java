package com.restaurant.rest_api.controller;

import com.restaurant.rest_api.dto.RestaurantTableRequest;
import com.restaurant.rest_api.dto.RestaurantTableResponse;
import com.restaurant.rest_api.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tables")
public class RestaurantTableController {
    private final RestaurantTableService tableService;

    @PostMapping
    public ResponseEntity<RestaurantTableResponse> createTable(@RequestBody @Valid RestaurantTableRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(tableService.createTable(request));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantTableResponse>> findAll(Pageable pageable){
        return ResponseEntity.ok(tableService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(tableService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> updateTable(@PathVariable Long id,@RequestBody @Valid RestaurantTableRequest request){
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id){
        tableService.deleteTable(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
