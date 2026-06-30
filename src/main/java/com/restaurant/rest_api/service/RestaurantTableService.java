package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.RestaurantTableRequest;
import com.restaurant.rest_api.dto.RestaurantTableResponse;
import com.restaurant.rest_api.entity.RestaurantTable;
import com.restaurant.rest_api.exception.RestaurantTableNotFoundException;
import com.restaurant.rest_api.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RestaurantTableService {
    private final RestaurantTableRepository repository;

    private RestaurantTableResponse toResponse(RestaurantTable table){
        return new RestaurantTableResponse(table.getId(), table.getNumber(), table.getCapacity(), table.getStatus());
    }

    public Page<RestaurantTableResponse> findAll(Pageable pageable){
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public RestaurantTableResponse findById(Long id){
        RestaurantTable table = repository.findById(id)
                .orElseThrow(() -> new RestaurantTableNotFoundException(id));
        return toResponse(table);
    }

    public RestaurantTableResponse createTable(RestaurantTableRequest request){
        RestaurantTable newTable = RestaurantTable.builder()
                .number(request.number())
                .capacity(request.capacity())
                .status(request.status())
                .build();
        RestaurantTable saved = repository.save(newTable);
        return toResponse(saved);
    }

    public RestaurantTableResponse updateTable(RestaurantTableRequest request, Long id){
        RestaurantTable toUpdate = repository.findById(id)
                .orElseThrow(() -> new RestaurantTableNotFoundException(id));

        toUpdate.setNumber(request.number());
        toUpdate.setCapacity(request.capacity());

        if (request.status() != null){
            toUpdate.setStatus(request.status());
        }

        RestaurantTable saved = repository.save(toUpdate);
        return toResponse(saved);
    }

    public void deleteTable(Long id){
        RestaurantTable toDelete = repository.findById(id)
                .orElseThrow(() -> new RestaurantTableNotFoundException(id));
        repository.delete(toDelete);
    }
}
