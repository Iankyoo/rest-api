package com.restaurant.rest_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.rest_api.dto.CategoryResponse;
import com.restaurant.rest_api.dto.MenuItemRequest;
import com.restaurant.rest_api.dto.MenuItemResponse;
import com.restaurant.rest_api.exception.InvalidCategoryIdsException;
import com.restaurant.rest_api.exception.MenuItemNotFoundException;
import com.restaurant.rest_api.service.MenuItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuItemController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MenuItemControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuItemService menuItemService;

    @Autowired
    private ObjectMapper objectMapper;

    private MenuItemResponse buildResponse(){
        return new MenuItemResponse(
                1L, "nameTest", "descriptionTest", new BigDecimal("15.00"), true,
                Set.of(new CategoryResponse(1L, "categoryTest", null))
        );
    }

    @Test
    public void createMenuItem_shouldReturn201() throws Exception {
        MenuItemRequest request = new MenuItemRequest("nameTest", "descriptionTest", new BigDecimal("15.00"), true, Set.of(1L));

        when(menuItemService.createMenuItem(any(MenuItemRequest.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/menuitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.price").value(15.00))
                .andExpect(jsonPath("$.categories[0].name").value("categoryTest"));
    }

    @Test
    public void createMenuItem_shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
        MenuItemRequest request = new MenuItemRequest("", null, null, null, null);

        mockMvc.perform(post("/api/v1/menuitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.available").exists())
                .andExpect(jsonPath("$.categoryIds").exists());

        verify(menuItemService, never()).createMenuItem(any(MenuItemRequest.class));
    }

    @Test
    public void createMenuItem_shouldReturn400_whenCategoryIdsAreInvalid() throws Exception {
        MenuItemRequest request = new MenuItemRequest("nameTest", null, new BigDecimal("15.00"), true, Set.of(999L));

        when(menuItemService.createMenuItem(any(MenuItemRequest.class)))
                .thenThrow(new InvalidCategoryIdsException("One or more category IDs are invalid"));

        mockMvc.perform(post("/api/v1/menuitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("One or more category IDs are invalid"));
    }

    @Test
    public void findMenuItemById_shouldReturn200() throws Exception {
        when(menuItemService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/menuitems/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("nameTest"));
    }

    @Test
    public void findMenuItemById_shouldReturn404_whenNotFound() throws Exception {
        when(menuItemService.findById(999L)).thenThrow(new MenuItemNotFoundException(999L));

        mockMvc.perform(get("/api/v1/menuitems/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteMenuItem_shouldReturn204() throws Exception {
        doNothing().when(menuItemService).deleteMenuItem(1L);

        mockMvc.perform(delete("/api/v1/menuitems/1"))
                .andExpect(status().isNoContent());
    }
}
