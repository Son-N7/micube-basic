package com.example.shop.stock.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 재고 현황 한 줄 (재고가 남은 로트 하나).
 *
 * @param productCode 상품 코드
 * @param productName 상품명
 * @param category 분류
 * @param lotNo 로트 번호
 * @param qty 남은 수량
 * @param status 로트 상태 (AVAILABLE, HOLD)
 * @param expiryDate 소비기한 (없으면 null)
 * @param activeHoldCount 활성 보류 건수
 * @param lastReleasedAt 마지막 보류 해제 일시 (없으면 null)
 */
public record StockSummary(String productCode, String productName, String category, String lotNo, int qty,
        String status, LocalDate expiryDate, int activeHoldCount, LocalDateTime lastReleasedAt) {
}
