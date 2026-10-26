package com.example.shop.stock.domain;

import java.time.LocalDateTime;

/**
 * 판매 보류 1건. 사유별로 한 건씩 기록한다.
 *
 * @param lotNo 로트 번호
 * @param reasonCode 보류 사유
 * @param comment 보류 사유 설명
 * @param heldAt 보류 일시
 * @param heldBy 보류한 사용자
 */
public record StockHold(String lotNo, HoldReason reasonCode, String comment, LocalDateTime heldAt,
        String heldBy) {
}
