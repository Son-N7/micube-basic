package com.example.shop.stock.api;

import com.example.shop.stock.domain.HoldReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 판매 보류 등록 요청.
 *
 * @param reasonCode 보류 사유 (QUALITY, CLAIM)
 * @param comment 보류 사유 설명 (필수, 500자 이내)
 */
public record HoldRequest(@NotNull HoldReason reasonCode, @NotBlank @Size(max = 500) String comment) {
}
