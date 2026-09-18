package com.example.shop.stock.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.support.ShopIntegrationTest;
import com.example.shop.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ShopIntegrationTest
class HoldReleaseApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    TestData data;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbcTemplate);
    }

    @Test
    void 보류를_해제하면_판매_가능() throws Exception {
        String lotNo = data.qualityHoldLot();

        release(lotNo, "QC", "{\"reasonCode\":\"QUALITY\",\"comment\":\"재검사 이상 없음\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        assertThat(data.lotStatus(lotNo)).isEqualTo("AVAILABLE");
        assertThat(data.activeHoldCount(lotNo, "QUALITY")).isZero();
    }

    @Test
    void 코멘트_없이_해제할_수_있다() throws Exception {
        String lotNo = data.qualityHoldLot();

        release(lotNo, "QC", "{\"reasonCode\":\"QUALITY\"}")
                .andExpect(status().isOk());
    }

    @Test
    void 그_사유의_활성_보류가_없으면_409() throws Exception {
        String lotNo = data.availableLot();

        release(lotNo, "QC", "{\"reasonCode\":\"QUALITY\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("NO_ACTIVE_HOLD"));
    }

    private ResultActions release(String lotNo, String role, String body) throws Exception {
        return mockMvc.perform(post("/api/lots/{lotNo}/holds/release", lotNo)
                .header("X-User-Id", "u01").header("X-User-Roles", role)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
