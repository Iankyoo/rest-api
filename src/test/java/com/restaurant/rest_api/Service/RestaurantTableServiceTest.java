package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.RestaurantTableRequest;
import com.restaurant.rest_api.dto.RestaurantTableResponse;
import com.restaurant.rest_api.entity.RestaurantTable;
import com.restaurant.rest_api.entity.TableStatus;
import com.restaurant.rest_api.exception.RestaurantTableNotFoundException;
import com.restaurant.rest_api.repository.RestaurantTableRepository;
import com.restaurant.rest_api.service.RestaurantTableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.restaurant.rest_api.fixtures.RestaurantTableFixture.buildTable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestaurantTableServiceTest {
    @Mock
    private RestaurantTableRepository tableRepository;

    @InjectMocks
    private RestaurantTableService tableService;

    @Test
    public void createTableTest(){
        RestaurantTable table = buildTable();
        RestaurantTableRequest request = new RestaurantTableRequest(1,8, TableStatus.AVAILABLE);

        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(table);

        RestaurantTableResponse result = tableService.createTable(request);

        assertEquals(1, result.number());
        assertEquals(8, result.capacity());
        assertEquals(TableStatus.AVAILABLE, result.status());

        verify(tableRepository).save(any(RestaurantTable.class));
    }

    @Test
    public void findTableByIdTest(){
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RestaurantTableNotFoundException.class, () -> {tableService.findById(999L);});
    }

    @Test
    public void findAllTables_shouldReturnPagedTables(){
        RestaurantTable table = buildTable();
        Pageable pageable = PageRequest.of(0,10);
        Page<RestaurantTable> page = new PageImpl<>(List.of(table));

        when(tableRepository.findAll(pageable)).thenReturn(page);

        Page<RestaurantTableResponse> result = tableService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).id());
        assertEquals(8, result.getContent().get(0).capacity());
        assertEquals(TableStatus.AVAILABLE, result.getContent().get(0).status());
    }

    @Test
    public void updateTableTest(){
        RestaurantTable table = buildTable();
        RestaurantTableRequest request = new RestaurantTableRequest( 2, 10, TableStatus.OCCUPIED );

        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(table);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        RestaurantTableResponse result = tableService.updateTable(1L, request);

        assertEquals(2, result.number());
        assertEquals(10, result.capacity());
        assertEquals(TableStatus.OCCUPIED, result.status());
        verify(tableRepository).save(table);
        verify(tableRepository).findById(1L);
    }

    @Test
    public void updateTable_withoutTableStatus_shouldKeepOriginalValue(){
        RestaurantTable table = buildTable();
        RestaurantTableRequest request = new RestaurantTableRequest(2, 10, null);

        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(table);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        RestaurantTableResponse result = tableService.updateTable(1L, request);

        assertEquals(TableStatus.AVAILABLE, result.status());
    }

    @Test
    public void updateTable_shouldThrowNotFoundException_whenIdDoesNotExist(){
        RestaurantTableRequest request = new RestaurantTableRequest(2,10,null);
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RestaurantTableNotFoundException.class, () -> {tableService.updateTable(999L, request);});
    }
}
