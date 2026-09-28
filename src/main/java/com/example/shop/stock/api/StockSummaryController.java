package com.example.shop.stock.api;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.auth.CurrentUser;
import com.example.shop.common.auth.Role;
import com.example.shop.stock.application.StockSummaryService;
import com.example.shop.stock.domain.StockSummary;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 재고 현황 API (상품 담당).
 */
@RestController
public class StockSummaryController {

    private final StockSummaryService summaryService;

    public StockSummaryController(StockSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    /** 재고가 남은 로트의 현황. 조건은 모두 선택. */
    @GetMapping("/api/stock-summary")
    public ApiResponse<List<StockSummary>> summary(@RequestParam(required = false) String category,
            @RequestParam(required = false) String productCode, @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month, CurrentUser user) {
        user.requireRole(Role.MD);
        return ApiResponse.ok(summaryService.findSummary(category, productCode, year, month));
    }
}
