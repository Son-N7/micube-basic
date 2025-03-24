package com.example.shop.product.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.support.ShopIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@ShopIntegrationTest
class ProductApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 상품_담당은_상품을_조회할_수_있다() throws Exception {
        mockMvc.perform(get("/api/products/{code}", "P1001").header("X-User-Id", "md01").header("X-User-Roles", "MD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName").value("국산콩 두부 300g"));
    }

    @Test
    void 없는_상품은_404() throws Exception {
        mockMvc.perform(get("/api/products/{code}", "P0000").header("X-User-Id", "md01").header("X-User-Roles", "MD"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"));
    }
}
