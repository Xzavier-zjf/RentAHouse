package com.example.rentalorder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateOrder() throws Exception {
        String json = """
        {
            "houseId": 1001,
            "startDate": "2024-07-01",
            "endDate": "2024-12-01",
            "deposit": 3000
        }
        """;

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer mock-token")
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}
