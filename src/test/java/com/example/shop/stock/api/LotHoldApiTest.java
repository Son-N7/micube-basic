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

@ShopIntegrationTest
class LotHoldApiTest {

    static final String LOT = "LT-261001-0001";
    static final String SOLD_OUT_LOT = "LT-261006-0014";

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
    void 품질_담당은_판매_보류를_걸_수_있다() throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.lotNo").value(LOT))
                .andExpect(jsonPath("$.data.status").value("HOLD"))
                .andExpect(jsonPath("$.data.qty").value(40));

        assertThat(data.lotStatus(LOT)).isEqualTo("HOLD");
        assertThat(data.lotQty(LOT)).isEqualTo(40);
        assertThat(data.activeHoldCount(LOT, "QUALITY")).isEqualTo(1);
        assertThat(jdbcTemplate.queryForMap(
                "SELECT hold_comment, held_at, held_by FROM stock_hold WHERE lot_no = ?", LOT))
                .containsEntry("HOLD_COMMENT", "이물 신고")
                .containsEntry("HELD_BY", "qc01")
                .containsKey("HELD_AT");
    }

    @Test
    void 같은_사유의_활성_보류가_있으면_409() throws Exception {
        hold(LOT, "QUALITY");

        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("HOLD_ALREADY_EXISTS"));

        assertThat(data.activeHoldCount(LOT, "QUALITY")).isEqualTo(1);
    }

    @Test
    void 다른_사유의_보류는_함께_걸_수_있다() throws Exception {
        hold(LOT, "QUALITY");

        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"CLAIM\",\"comment\":\"고객 클레임\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("HOLD"));

        assertThat(data.activeHoldCount(LOT, "QUALITY")).isEqualTo(1);
        assertThat(data.activeHoldCount(LOT, "CLAIM")).isEqualTo(1);
    }

    @Test
    void 품질_담당이_아니면_보류할_수_없다() throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "wh01").header("X-User-Roles", "WAREHOUSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        assertThat(data.lotStatus(LOT)).isEqualTo("AVAILABLE");
        assertThat(data.activeHoldCount(LOT, "QUALITY")).isZero();
    }

    @Test
    void 코멘트가_공백이면_400() throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"QUALITY\",\"comment\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        assertThat(data.lotStatus(LOT)).isEqualTo("AVAILABLE");
        assertThat(data.activeHoldCount(LOT, "QUALITY")).isZero();
    }

    @Test
    void 코멘트가_500자를_넘으면_400() throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"QUALITY\",\"comment\":\"" + "가".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void 사유가_없거나_잘못된_값이면_400() throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"이물 신고\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/lots/{lotNo}/holds", LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"OTHER\",\"comment\":\"이물 신고\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void 소진_로트는_보류할_수_없다() throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", SOLD_OUT_LOT)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("LOT_NOT_HOLDABLE"));

        assertThat(data.activeHoldCount(SOLD_OUT_LOT, "QUALITY")).isZero();
    }

    @Test
    void 없는_로트면_404() throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", "LT-000000-0000")
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"QUALITY\",\"comment\":\"이물 신고\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("LOT_NOT_FOUND"));
    }

    private void hold(String lotNo, String reasonCode) throws Exception {
        mockMvc.perform(post("/api/lots/{lotNo}/holds", lotNo)
                        .header("X-User-Id", "qc01").header("X-User-Roles", "QC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"" + reasonCode + "\",\"comment\":\"첫 보류\"}"))
                .andExpect(status().isCreated());
    }
}
