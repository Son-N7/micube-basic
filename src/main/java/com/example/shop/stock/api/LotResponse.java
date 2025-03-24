package com.example.shop.stock.api;

import com.example.shop.stock.domain.StockLot;
import java.time.LocalDate;

/**
 * 로트 응답.
 *
 * @param lotNo 로트 번호
 * @param productCode 상품 코드
 * @param qty 남은 수량
 * @param status 상태 (AVAILABLE, HOLD, SOLD_OUT)
 * @param expiryDate 소비기한 (없으면 null)
 */
public record LotResponse(String lotNo, String productCode, int qty, String status, LocalDate expiryDate) {

    /** 도메인 객체로 만든다. */
    public static LotResponse from(StockLot lot) {
        return new LotResponse(lot.lotNo(), lot.productCode(), lot.qty(), lot.status().name(), lot.expiryDate());
    }
}
