package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.MenuItemRequest;
import com.restaurant.rest_api.dto.MenuItemResponse;
import com.restaurant.rest_api.entity.Category;
import com.restaurant.rest_api.entity.MenuItem;
import com.restaurant.rest_api.fixtures.CategoryFixture;
import com.restaurant.rest_api.fixtures.MenuItemFixture;
import com.restaurant.rest_api.repository.CategoryRepository;
import com.restaurant.rest_api.repository.MenuItemRepository;
import com.restaurant.rest_api.service.MenuItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.parser.Entity;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MenuItemServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuItemService menuItemService;

    @Test
    public void createMenuItemTest(){
        Category category = CategoryFixture.buildCategory();
        Set<Long> categoriesId = Set.of(category.getId());
        MenuItem menuItem = MenuItemFixture.buildMenuItem();

        MenuItemRequest request = new MenuItemRequest(
                "nameTest",
                "descriptionTest",
                new BigDecimal("35.00"),
                Boolean.TRUE,
                categoriesId
        );

        when(categoryRepository.findAllById(categoriesId)).thenReturn(List.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponse result = menuItemService.createMenuItem(request);

        assertEquals("nameTest", result.name());
        assertEquals("descriptionTest", result.description());
        assertEquals(new BigDecimal("15.00"), result.price());
        assertEquals(Boolean.TRUE, result.available());
        assertEquals(1, result.categories().size());
        assertEquals(category.getName(), result.categories().iterator().next().name());
    }
}
