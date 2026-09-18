package com.example.shop.stock.api;

import com.example.shop.stock.domain.HoldReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 판매 보류 해제 요청.
 *
 * @param reasonCode 해제할 보류의 사유 (QUALITY, CLAIM)
 * @param comment 해제 사유 설명 (선택, 500자 이내)
 */
public record ReleaseRequest(@NotNull HoldReason reasonCode, @Size(max = 500) String comment) {
}
