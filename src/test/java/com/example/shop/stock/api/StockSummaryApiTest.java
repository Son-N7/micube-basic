package com.example.shop.stock.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.support.ShopIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@ShopIntegrationTest
class StockSummaryApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 분류로_재고_현황을_조회한다() throws Exception {
        mockMvc.perform(get("/api/stock-summary").param("category", "FROZEN")
                        .header("X-User-Id", "md01").header("X-User-Roles", "MD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].productCode").value("P2001"));
    }

    @Test
    void 상품_담당이_아니면_403() throws Exception {
        mockMvc.perform(get("/api/stock-summary").header("X-User-Id", "wh01").header("X-User-Roles", "WAREHOUSE"))
                .andExpect(status().isForbidden());
    }
}
