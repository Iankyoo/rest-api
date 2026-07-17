package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.RestaurantTableRequest;
import com.restaurant.rest_api.dto.RestaurantTableResponse;
import com.restaurant.rest_api.entity.RestaurantTable;
import com.restaurant.rest_api.entity.TableStatus;
import com.restaurant.rest_api.repository.RestaurantTableRepository;
import com.restaurant.rest_api.service.RestaurantTableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.restaurant.rest_api.fixtures.RestaurantTableFixture.buildTable;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        verify(tableRepository).save(table);
    }
}
