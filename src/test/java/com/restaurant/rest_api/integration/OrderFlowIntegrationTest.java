package com.restaurant.rest_api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode readBody(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private ResultActions postJson(String url, String token, String body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String login(String email, String password) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password)))
                .andExpect(status().isOk());
        return readBody(result).get("token").asText();
    }

    @Test
    public void fullOrderFlow_shouldOpenOrderAddItemsAndCloseIt() throws Exception {
        // Admin criado pelo AdminSeeder com as credenciais do application.properties de teste
        String adminToken = login("admin@test.com", "admin123");

        long categoryId = readBody(postJson("/api/v1/categories", adminToken,
                "{\"name\": \"Pratos\", \"description\": \"Pratos principais\"}")
                .andExpect(status().isCreated())).get("id").asLong();

        long menuItemId = readBody(postJson("/api/v1/menuitems", adminToken,
                "{\"name\": \"Feijoada\", \"price\": 45.50, \"available\": true, \"categoryIds\": [%d]}".formatted(categoryId))
                .andExpect(status().isCreated())).get("id").asLong();

        long tableId = readBody(postJson("/api/v1/tables", adminToken,
                "{\"number\": 10, \"capacity\": 4, \"status\": \"AVAILABLE\"}")
                .andExpect(status().isCreated())).get("id").asLong();

        // Cliente se registra, mas não tem permissão para abrir pedidos
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Cliente\", \"email\": \"cliente@test.com\", \"password\": \"senha123\"}"))
                .andExpect(status().isCreated());
        String customerToken = login("cliente@test.com", "senha123");

        postJson("/api/v1/orders", customerToken, "{\"tableId\": %d}".formatted(tableId))
                .andExpect(status().isForbidden());

        // Admin abre o pedido e a mesa fica ocupada
        long orderId = readBody(postJson("/api/v1/orders", adminToken, "{\"tableId\": %d}".formatted(tableId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))).get("id").asLong();

        postJson("/api/v1/orders", adminToken, "{\"tableId\": %d}".formatted(tableId))
                .andExpect(status().isConflict());

        // Adiciona 2 unidades do item: total = 2 x 45.50
        postJson("/api/v1/orders/%d/items".formatted(orderId), adminToken,
                "{\"menuItemId\": %d, \"quantity\": 2, \"observation\": \"sem cebola\"}".formatted(menuItemId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitPrice").value(45.50));

        mockMvc.perform(get("/api/v1/orders/%d".formatted(orderId))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(91.00))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].observation").value("sem cebola"));

        // Fecha o pedido: mesa volta a ficar disponível e o pedido não aceita mais itens
        mockMvc.perform(patch("/api/v1/orders/%d/close".formatted(orderId))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(get("/api/v1/tables/%d".formatted(tableId))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        postJson("/api/v1/orders/%d/items".formatted(orderId), adminToken,
                "{\"menuItemId\": %d, \"quantity\": 1}".formatted(menuItemId))
                .andExpect(status().isBadRequest());
    }
}
