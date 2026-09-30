package com.restaurant.rest_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.rest_api.dto.CategoryRequest;
import com.restaurant.rest_api.dto.CategoryResponse;
import com.restaurant.rest_api.exception.CategoryNotFoundException;
import com.restaurant.rest_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void categoryCreate_shouldReturn201() throws Exception {
        CategoryRequest request = new CategoryRequest("nameTest", "descriptionTest");
        CategoryResponse response = new CategoryResponse(1L, "nameTest", "descriptionTest");

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("nameTest"));
    }

    @Test
    public void findCategoriesById_shouldReturn200() throws Exception{
        CategoryResponse category = new CategoryResponse(1L, "nameTest", "descriptionTest");

        when(categoryService.findById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("nameTest"));
    }

    @Test
    public void findCategoryById_shouldReturn404_whenNotFound() throws Exception {
        when(categoryService.findById(999L))
                .thenThrow(new CategoryNotFoundException(999L));

        mockMvc.perform(get("/api/v1/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findAllCategories_shouldReturn200() throws Exception {
        CategoryResponse category = new CategoryResponse(1L, "nameTest", "descriptionTest");
        Page<CategoryResponse> page = new PageImpl<>(List.of(category));

        when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("nameTest"));
    }

    @Test
    public void updateCategory_shouldReturn200() throws Exception {
        CategoryRequest request = new CategoryRequest("nameUpdated", "descriptionUpdated");
        CategoryResponse response = new CategoryResponse(1L, "nameUpdated", "descriptionUpdated");

        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("nameUpdated"));
    }

    @Test
    public void deleteCategory_shouldReturn204() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void createCategory_shouldReturn400_whenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body or invalid field value"));
    }

    @Test
    public void deleteCategory_shouldReturn409_whenCategoryIsInUse() throws Exception {
        doThrow(new DataIntegrityViolationException("fk violation"))
                .when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isConflict());
    }

    @Test
    public void findCategoryById_shouldReturn500WithGenericMessage_whenUnexpectedErrorOccurs() throws Exception {
        when(categoryService.findById(1L)).thenThrow(new RuntimeException("internal detail"));

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
}
