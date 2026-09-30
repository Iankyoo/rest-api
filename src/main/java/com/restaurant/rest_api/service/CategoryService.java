package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.CategoryRequest;
import com.restaurant.rest_api.dto.CategoryResponse;
import com.restaurant.rest_api.entity.Category;
import com.restaurant.rest_api.exception.CategoryNotFoundException;
import com.restaurant.rest_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;

    private CategoryResponse toResponse(Category category){
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    private Category findCategory(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request){
        Category newCategory = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        Category saved = repository.save(newCategory);
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(Pageable pageable){
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id){
        Category category = findCategory(id);
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request){
        Category category = findCategory(id);
        category.setName(request.name());
        if (request.description() != null){
            category.setDescription(request.description());
        }
        Category saved = repository.save(category);
        return toResponse(saved);
    }

    @Transactional
    public void deleteCategory(Long id){
        Category toDelete = findCategory(id);
        repository.delete(toDelete);
    }
}
