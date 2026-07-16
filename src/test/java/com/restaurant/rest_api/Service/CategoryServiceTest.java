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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.restaurant.rest_api.fixtures.CategoryFixture.buildCategory;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void createCategoryTest(){
        CategoryRequest categoryRequest = new CategoryRequest("nameTest", "descriptionTest");
        Category savedCategory = buildCategory();


        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(categoryRequest);

        assertEquals(1L,response.id());
        assertEquals("nameTest", response.name());
        assertEquals("descriptionTest", response.description());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    public void findById_shouldThrowException_whenCategoryNotFound(){
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.findById(999L);
        });
    }

    @Test
    public void findAll_shouldReturnPagedCategories(){
        Category category = buildCategory();
        Pageable pageable = PageRequest.of(0,10);
        Page<Category> page = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(pageable)).thenReturn(page);

        Page<CategoryResponse> response = categoryService.findAll(pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("nameTest", response.getContent().get(0).name());
    }

    @Test
    public void updateTest(){
        Category existingCategory = buildCategory();

        CategoryRequest request = new CategoryRequest(
                "nameUpdated", "descriptionUpdated"
        );

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertEquals("nameUpdated", result.name());
        assertEquals("descriptionUpdated", result.description());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(existingCategory);

    }
}
