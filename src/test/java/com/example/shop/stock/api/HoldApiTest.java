package com.example.shop.stock.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.stock.domain.LotStatus;
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
class HoldApiTest {

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
    void 품질_담당은_판매_보류를_등록할_수_있다() throws Exception {
        String lotNo = data.availableLot();

        hold(lotNo, "QC", "{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("HOLD"))
                .andExpect(jsonPath("$.data.qty").value(100));

        assertThat(data.lotStatus(lotNo)).isEqualTo("HOLD");
        assertThat(data.activeHoldCount(lotNo, "QUALITY")).isEqualTo(1);
    }

    @Test
    void 다른_사유의_보류는_함께_걸_수_있다() throws Exception {
        String lotNo = data.qualityHoldLot();

        hold(lotNo, "QC", "{\"reasonCode\":\"CLAIM\",\"comment\":\"고객 클레임 조사\"}")
                .andExpect(status().isCreated());

        assertThat(data.activeHoldCount(lotNo, "QUALITY")).isEqualTo(1);
        assertThat(data.activeHoldCount(lotNo, "CLAIM")).isEqualTo(1);
    }

    @Test
    void 같은_사유의_활성_보류가_있으면_409() throws Exception {
        String lotNo = data.qualityHoldLot();

        hold(lotNo, "QC", "{\"reasonCode\":\"QUALITY\",\"comment\":\"재신고\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("HOLD_ALREADY_EXISTS"));

        assertThat(data.activeHoldCount(lotNo, "QUALITY")).isEqualTo(1);
    }

    @Test
    void 소진_로트는_보류할_수_없다() throws Exception {
        String lotNo = data.lot(TestData.TOFU, 0, LotStatus.SOLD_OUT);

        hold(lotNo, "QC", "{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("LOT_NOT_HOLDABLE"));
    }

    @Test
    void 품질_담당이_아니면_403() throws Exception {
        String lotNo = data.availableLot();

        hold(lotNo, "WAREHOUSE", "{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}")
                .andExpect(status().isForbidden());

        assertThat(data.lotStatus(lotNo)).isEqualTo("AVAILABLE");
    }

    @Test
    void 코멘트가_공백이면_400() throws Exception {
        String lotNo = data.availableLot();

        hold(lotNo, "QC", "{\"reasonCode\":\"QUALITY\",\"comment\":\"  \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        assertThat(data.activeHoldCount(lotNo, "QUALITY")).isZero();
    }

    @Test
    void 없는_로트는_404() throws Exception {
        hold("LT-000000-0000", "QC", "{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("LOT_NOT_FOUND"));
    }

    private ResultActions hold(String lotNo, String role, String body) throws Exception {
        return mockMvc.perform(post("/api/lots/{lotNo}/holds", lotNo)
                .header("X-User-Id", "u01").header("X-User-Roles", role)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
