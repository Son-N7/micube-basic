package com.example.shop.product.api;

import com.example.shop.product.domain.Product;

/**
 * 상품 응답.
 *
 * @param productCode 상품 코드
 * @param productName 상품명
 * @param category 분류 (FRESH, FROZEN, PROCESSED, LIVING)
 * @param price 판매가
 */
public record ProductResponse(String productCode, String productName, String category, int price) {

    /** 도메인 객체로 만든다. */
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.productCode(), product.productName(), product.category(),
                product.price());
    }
}
