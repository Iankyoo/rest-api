package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.MenuItemRequest;
import com.restaurant.rest_api.dto.MenuItemResponse;
import com.restaurant.rest_api.entity.Category;
import com.restaurant.rest_api.entity.MenuItem;
import com.restaurant.rest_api.exception.InvalidCategoryIdsException;
import com.restaurant.rest_api.exception.MenuItemNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @Test
    public void createMenuItem_shouldThrowException_whenCategoryIdsAreInvalid(){

        Category category = CategoryFixture.buildCategory();
        Set<Long> categoriesId = Set.of(category.getId(), 999L);

        MenuItemRequest request = new MenuItemRequest(
                "nameTest",
                "descriptionTest",
                new BigDecimal("35.00"),
                Boolean.TRUE,
                categoriesId
        );

        when(categoryRepository.findAllById(categoriesId)).thenReturn(List.of(category));

        assertThrows(InvalidCategoryIdsException.class, () -> {
            menuItemService.createMenuItem(request);
        });
    }

    @Test
    public void findMenuItemByIdTest(){
        MenuItem menuItem = MenuItemFixture.buildMenuItem();

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        MenuItemResponse result = menuItemService.findById(1L);

        assertEquals(1L, result.id());
        assertEquals("nameTest", result.name());
    }

    @Test
    public void findMenuItemByWrongId_shouldThrowException(){
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MenuItemNotFoundException.class, () -> {
            menuItemService.findById(999L);
        });
    }

    @Test
    public void updateMenuItemTest(){
        MenuItem menuItem = MenuItemFixture.buildMenuItem();
        Category category = menuItem.getCategories().iterator().next();
        Set<Long> categoriesId = Set.of(menuItem.getCategories().iterator().next().getId());

        MenuItemRequest request = new MenuItemRequest(
                "nameUpdated",
                "descriptionUpdated",
                new BigDecimal("35.00"),
                Boolean.TRUE,
                categoriesId
        );

        when(menuItemRepository.findById(menuItem.getId())).thenReturn(Optional.of(menuItem));
        when(categoryRepository.findAllById(categoriesId)).thenReturn(List.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponse result = menuItemService.updateMenuItem(1L, request);

        assertEquals("nameUpdated", result.name());
        assertEquals("descriptionUpdated", result.description());
    }

    @Test
    public void updateMenuItem_shouldThrowException_WhenCategoriesIdNotExists(){
        MenuItem menuItem = MenuItemFixture.buildMenuItem();
        Category category = menuItem.getCategories().iterator().next();
        Set<Long> categoriesId = Set.of(category.getId(), 999L);

        MenuItemRequest request = new MenuItemRequest(
                "nameUpdated",
                "descriptionUpdated",
                new BigDecimal("35.00"),
                Boolean.TRUE,
                categoriesId
        );

        when(menuItemRepository.findById(menuItem.getId())).thenReturn(Optional.of(menuItem));
        when(categoryRepository.findAllById(categoriesId)).thenReturn(List.of(category));

        assertThrows(InvalidCategoryIdsException.class, () -> {
            menuItemService.updateMenuItem(menuItem.getId(), request);
        });
    }

    @Test
    public void findAllMenuItemTest(){
        MenuItem menuItem = MenuItemFixture.buildMenuItem();
        Pageable pageable = PageRequest.of(0,10);
        Page<MenuItem> page = new PageImpl<>(List.of(menuItem));

        when(menuItemRepository.findAll(pageable)).thenReturn(page);

        Page<MenuItemResponse> result = menuItemService.findAll(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(menuItem.getName(), result.getContent().get(0).name());
    }

    @Test
    public void deleteMenuItemTest(){
        MenuItem menuItem = MenuItemFixture.buildMenuItem();

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        menuItemService.deleteMenuItem(1L);

        verify(menuItemRepository).delete(menuItem);
    }

    @Test
    public void deleteMenuItem_shouldThrowException_whenMenuItemNotFound(){
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MenuItemNotFoundException.class, () -> {
            menuItemService.deleteMenuItem(999L);
        });
    }
}
