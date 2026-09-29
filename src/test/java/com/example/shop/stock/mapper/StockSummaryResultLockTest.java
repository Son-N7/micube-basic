package com.example.shop.stock.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.stock.domain.StockSummary;
import com.example.shop.support.ShopIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 재고 현황 쿼리(selectStockSummaryList)의 지금 결과를 고정한다. 리팩토링 전후로 결과가 같아야 한다.
 * 기대값은 리팩토링 전 쿼리가 낸 값이다 (행 수, 각 행의 값, 정렬).
 */
@ShopIntegrationTest
class StockSummaryResultLockTest {

    @Autowired
    StockSummaryMapper summaryMapper;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("INSERT INTO product (product_code, product_name, category, price) VALUES "
                + "('T9001', '테스트 샐러드', 'FRESH', 1000), ('T9002', '테스트 만두', 'FROZEN', 2000)");
        lot("TL-SUM-1", "T9001", 10, "AVAILABLE", "2027-03-10");   // 보류 없음
        lot("TL-SUM-2", "T9001", 20, "HOLD", "2027-03-20");        // 사유 두 개
        lot("TL-SUM-3", "T9001", 30, "AVAILABLE", "2027-04-05");   // 해제된 보류만
        lot("TL-SUM-4", "T9002", 40, "AVAILABLE", null);           // 소비기한 없음
        lot("TL-SUM-5", "T9002", 0, "SOLD_OUT", "2027-03-15");     // 소진
        hold("TL-SUM-2", "QUALITY", null);
        hold("TL-SUM-2", "CLAIM", null);
        hold("TL-SUM-3", "QUALITY", "2026-09-05 15:00:00");
    }

    @Test
    void 상품코드_앞부분만() {
        assertThat(rows(null, "T9", null, null)).containsExactly(
                "T9001|TL-SUM-1|10|AVAILABLE|2027-03-10|0|null",
                "T9001|TL-SUM-2|20|HOLD|2027-03-20|2|null",
                "T9001|TL-SUM-3|30|AVAILABLE|2027-04-05|0|2026-09-05T15:00",
                "T9002|TL-SUM-4|40|AVAILABLE|null|0|null");
    }

    @Test
    void 분류_하나와_상품코드() {
        assertThat(rows("FRESH", "T9", null, null)).containsExactly(
                "T9001|TL-SUM-1|10|AVAILABLE|2027-03-10|0|null",
                "T9001|TL-SUM-2|20|HOLD|2027-03-20|2|null",
                "T9001|TL-SUM-3|30|AVAILABLE|2027-04-05|0|2026-09-05T15:00");
    }

    @Test
    void 기준월() {
        assertThat(rows(null, "T9", 2027, 3)).containsExactly(
                "T9001|TL-SUM-1|10|AVAILABLE|2027-03-10|0|null",
                "T9001|TL-SUM-2|20|HOLD|2027-03-20|2|null");
    }

    @Test
    void 분류_하나와_기준월_소진만_있으면_없음() {
        assertThat(rows("FROZEN", "T9", 2027, 3)).isEmpty();
    }

    @Test
    void 상품코드_전체_일치와_기준월() {
        assertThat(rows(null, "T9001", 2027, 4)).containsExactly(
                "T9001|TL-SUM-3|30|AVAILABLE|2027-04-05|0|2026-09-05T15:00");
    }

    @Test
    void 분류_하나만() {
        List<String> rows = rows("FRESH", null, null, null);
        assertThat(rows).hasSize(9);
        assertThat(rows).first().isEqualTo("P1001|LT-261001-0001|40|AVAILABLE|2026-11-05|0|null");
        assertThat(rows).last().isEqualTo("T9001|TL-SUM-3|30|AVAILABLE|2027-04-05|0|2026-09-05T15:00");
    }

    @Test
    void 조건_없음() {
        List<String> rows = rows(null, null, null, null);
        assertThat(rows).hasSize(19);
        assertThat(rows).contains("P2003|LT-261005-0011|90|HOLD|2027-02-28|1|null",
                "P2001|LT-261001-0003|120|AVAILABLE|2027-04-01|0|2026-10-02T17:30");
    }

    private List<String> rows(String category, String productCode, Integer year, Integer month) {
        return summaryMapper.selectStockSummaryList(category, productCode, year, month).stream()
                .map(StockSummaryResultLockTest::format).toList();
    }

    private static String format(StockSummary s) {
        return String.join("|", s.productCode(), s.lotNo(), String.valueOf(s.qty()), s.status(),
                String.valueOf(s.expiryDate()), String.valueOf(s.activeHoldCount()), String.valueOf(s.lastReleasedAt()));
    }

    private void lot(String lotNo, String productCode, int qty, String status, String expiryDate) {
        jdbc.update("INSERT INTO stock_lot (lot_no, product_code, qty, status, expiry_date, received_at, received_by) "
                + "VALUES (?, ?, ?, ?, CAST(? AS DATE), TIMESTAMP '2026-09-01 08:00:00', 'test')",
                lotNo, productCode, qty, status, expiryDate);
    }

    private void hold(String lotNo, String reason, String releasedAt) {
        jdbc.update("INSERT INTO stock_hold (lot_no, reason_code, hold_comment, held_at, held_by, released_at) "
                + "VALUES (?, ?, 'test', TIMESTAMP '2026-09-01 10:00:00', 'test', CAST(? AS TIMESTAMP))",
                lotNo, reason, releasedAt);
    }
}
