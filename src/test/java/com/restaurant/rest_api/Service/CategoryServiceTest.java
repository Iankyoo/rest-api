package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.CategoryRequest;
import com.restaurant.rest_api.dto.CategoryResponse;
import com.restaurant.rest_api.entity.Category;
import com.restaurant.rest_api.exception.CategoryNotFoundException;
import com.restaurant.rest_api.repository.CategoryRepository;
import com.restaurant.rest_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void createCategoryTest(){
        CategoryRequest categoryRequest = new CategoryRequest("Drinks", "");
        Category savedCategory = Category.builder()
                .id(1L)
                .name("Drinks")
                .description("")
                .build();


        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(categoryRequest);

        assertEquals(1L,response.id());
        assertEquals("Drinks", response.name());
        assertEquals("", response.description());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    public void findById_shouldThrowException_whenCategoryNotFound(){
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.findById(999L);
        });
    }
}
