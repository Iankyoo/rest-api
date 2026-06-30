package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.CategoryResponse;
import com.restaurant.rest_api.dto.MenuItemRequest;
import com.restaurant.rest_api.dto.MenuItemResponse;
import com.restaurant.rest_api.entity.Category;
import com.restaurant.rest_api.entity.MenuItem;
import com.restaurant.rest_api.exception.InvalidCategoryIdsException;
import com.restaurant.rest_api.exception.MenuItemNotFoundException;
import com.restaurant.rest_api.repository.CategoryRepository;
import com.restaurant.rest_api.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    private MenuItemResponse toResponse(MenuItem menuItem){
        Set<CategoryResponse> categories = menuItem.getCategories().stream()
                .map(c -> new CategoryResponse( c.getId(), c.getName(), c.getDescription()))
                .collect(Collectors.toSet());

        return new MenuItemResponse(menuItem.getId(), menuItem.getName(), menuItem.getDescription(), menuItem.getPrice(), menuItem.getAvailable(), categories);
    }

    private List<Category> verifyRequestIds(Set<Long> Ids){
        List<Category> categories = categoryRepository.findAllById(Ids);

        if (categories.size() != Ids.size()){
            throw new InvalidCategoryIdsException("One or more category IDs are invalid");
        }
        return categories;
    }

    public MenuItemResponse createMenuItem(MenuItemRequest request){

        MenuItem newMenuItem = MenuItem.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .available(request.available())
                .build();

        newMenuItem.setCategories(new HashSet<>(verifyRequestIds(request.categoryIds())));

        MenuItem saved = menuItemRepository.save(newMenuItem);
        return toResponse(saved);
    }

    public Page<MenuItemResponse> findAll(Pageable pageable){
        return menuItemRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public MenuItemResponse findById(Long id){
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(()-> new MenuItemNotFoundException(id));
        return toResponse(menuItem);
    }

    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request){
        MenuItem toUpdate = menuItemRepository.findById(id)
                        .orElseThrow(() -> new MenuItemNotFoundException(id));

        toUpdate.setName(request.name());

        if (request.description() != null){
            toUpdate.setDescription(request.description());
        }

        toUpdate.setPrice(request.price());

        toUpdate.setAvailable(request.available());

        toUpdate.setCategories(new HashSet<>(verifyRequestIds(request.categoryIds())));

        MenuItem saved = menuItemRepository.save(toUpdate);

        return toResponse(saved);
    }

    public void deleteMenuItem(Long id){
        MenuItem toDelete = menuItemRepository.findById(id)
                        .orElseThrow(()-> new MenuItemNotFoundException(id));
        menuItemRepository.delete(toDelete);
    }
}
