package com.example.shop.stock.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.support.ShopIntegrationTest;
import com.example.shop.support.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/** BUG-0217: 한 사유를 해제하면 다른 사유의 보류는 남아야 한다. */
@ShopIntegrationTest
class HoldReleaseRegressionTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void CLAIM만_해제하면_QUALITY_보류가_남고_HOLD를_유지한다() throws Exception {
        TestData data = new TestData(jdbcTemplate);
        String lotNo = data.qualityHoldLot();
        data.activeHold(lotNo, "CLAIM");

        mockMvc.perform(post("/api/lots/{lotNo}/holds/release", lotNo)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"CLAIM\",\"comment\":\"클레임 조사 종료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HOLD"));

        assertThat(data.activeHoldCount(lotNo, "QUALITY")).isEqualTo(1);
        assertThat(data.activeHoldCount(lotNo, "CLAIM")).isZero();
        assertThat(data.lotStatus(lotNo)).isEqualTo("HOLD");
    }
}
