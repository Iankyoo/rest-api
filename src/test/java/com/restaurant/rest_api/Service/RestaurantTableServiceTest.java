package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.repository.RestaurantTableRepository;
import com.restaurant.rest_api.service.RestaurantTableService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RestaurantTableServiceTest {
    @Mock
    private RestaurantTableRepository tableRepository;

    @InjectMocks
    private RestaurantTableService tableService;


}
