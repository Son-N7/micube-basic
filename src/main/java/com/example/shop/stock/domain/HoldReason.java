package com.example.shop.stock.domain;

/**
 * 판매 보류 사유. 보류는 사유별 건 단위로 stock_hold 에 기록한다.
 */
public enum HoldReason {
    /** 품질 이슈 (이물 신고, 변질 의심 등). */
    QUALITY,
    /** 고객 클레임 (조사 중). */
    CLAIM
}
