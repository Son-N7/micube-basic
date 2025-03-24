package com.example.shop.product.api;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.auth.CurrentUser;
import com.example.shop.common.auth.Role;
import com.example.shop.product.application.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 조회 API (상품 담당).
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 판매 중인 상품을 조회한다. */
    @GetMapping("/{productCode}")
    public ApiResponse<ProductResponse> get(@PathVariable String productCode, CurrentUser user) {
        user.requireRole(Role.MD);
        return ApiResponse.ok(ProductResponse.from(productService.getProduct(productCode)));
    }
}
