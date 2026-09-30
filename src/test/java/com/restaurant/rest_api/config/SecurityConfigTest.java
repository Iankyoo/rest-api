package com.restaurant.rest_api.config;

import com.restaurant.rest_api.controller.BaseControllerTest;
import com.restaurant.rest_api.controller.CategoryController;
import com.restaurant.rest_api.controller.OrderController;
import com.restaurant.rest_api.controller.RestaurantTableController;
import com.restaurant.rest_api.controller.UserController;
import com.restaurant.rest_api.service.CategoryService;
import com.restaurant.rest_api.service.OrderService;
import com.restaurant.rest_api.service.RestaurantTableService;
import com.restaurant.rest_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CategoryController.class, RestaurantTableController.class, OrderController.class, UserController.class})
@Import(SecurityConfig.class)
public class SecurityConfigTest extends BaseControllerTest {

    private static final String CATEGORY_JSON = """
            {"name": "Bebidas", "description": "Sucos"}
            """;
    private static final String TABLE_JSON = """
            {"number": 1, "capacity": 4, "status": "AVAILABLE"}
            """;
    private static final String ROLE_JSON = """
            {"role": "WAITER"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private RestaurantTableService tableService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    @Test
    public void getCategories_shouldReturn200_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    public void createCategory_shouldReturn401_withoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORY_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WAITER")
    public void createCategory_shouldReturn403_whenWaiter() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORY_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createCategory_shouldReturn201_whenAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORY_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void getTables_shouldReturn403_whenCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/tables"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WAITER")
    public void getTables_shouldReturn200_whenWaiter() throws Exception {
        mockMvc.perform(get("/api/v1/tables"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WAITER")
    public void createTable_shouldReturn403_whenWaiter() throws Exception {
        mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TABLE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void getOrders_shouldReturn403_whenCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WAITER")
    public void getOrders_shouldReturn200_whenWaiter() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WAITER")
    public void updateRole_shouldReturn403_whenWaiter() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ROLE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateRole_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ROLE_JSON))
                .andExpect(status().isOk());
    }
}
