package com.example.shop.stock.api;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.auth.CurrentUser;
import com.example.shop.common.auth.Role;
import com.example.shop.stock.application.LotService;
import com.example.shop.stock.domain.StockLot;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 입고·출고·판매 보류·로트 조회 API.
 */
@RestController
@RequestMapping("/api/lots")
public class LotController {

    private final LotService lotService;

    public LotController(LotService lotService) {
        this.lotService = lotService;
    }

    /** 상품의 재고 로트 목록 (소진 제외, 소비기한 빠른 순). */
    @GetMapping
    public ApiResponse<List<LotResponse>> findByProduct(@RequestParam String productCode, CurrentUser user) {
        user.requireRole(Role.WAREHOUSE);
        return ApiResponse.ok(lotService.findInStockLots(productCode).stream().map(LotResponse::from).toList());
    }

    /** 입고한다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LotResponse> receive(@Valid @RequestBody ReceiveRequest request, CurrentUser user) {
        user.requireRole(Role.WAREHOUSE);
        StockLot lot = lotService.receive(request.productCode(), request.qty(), request.expiryDate(), user.userId());
        return ApiResponse.ok(LotResponse.from(lot));
    }

    /** 출고한다. 출고할 로트를 지정한다. */
    @PostMapping("/{lotNo}/shipments")
    public ApiResponse<LotResponse> ship(@PathVariable String lotNo, @Valid @RequestBody ShipRequest request,
            CurrentUser user) {
        user.requireRole(Role.WAREHOUSE);
        StockLot lot = lotService.ship(lotNo, request.qty());
        return ApiResponse.ok(LotResponse.from(lot));
    }

    /** 판매 보류를 건다. 사유별로 한 건씩 기록한다. */
    @PostMapping("/{lotNo}/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LotResponse> hold(@PathVariable String lotNo, @Valid @RequestBody HoldRequest request,
            CurrentUser user) {
        user.requireRole(Role.QC);
        StockLot lot = lotService.hold(lotNo, request.reasonCode(), request.comment(), user.userId());
        return ApiResponse.ok(LotResponse.from(lot));
    }
}
