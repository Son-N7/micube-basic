package com.example.shop.stock.api;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.auth.CurrentUser;
import com.example.shop.common.auth.Role;
import com.example.shop.stock.application.HoldService;
import com.example.shop.stock.domain.StockLot;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매 보류 API (품질 담당).
 */
@RestController
@RequestMapping("/api/lots/{lotNo}/holds")
public class HoldController {

    private final HoldService holdService;

    public HoldController(HoldService holdService) {
        this.holdService = holdService;
    }

    /** 판매 보류를 해제한다. */
    @PostMapping("/release")
    public ApiResponse<LotResponse> release(@PathVariable String lotNo, @Valid @RequestBody ReleaseRequest request,
            CurrentUser user) {
        user.requireRole(Role.QC);
        StockLot lot = holdService.releaseHold(lotNo, request.reasonCode(), request.comment(), user.userId());
        return ApiResponse.ok(LotResponse.from(lot));
    }

    /** 판매 보류를 등록한다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LotResponse> hold(@PathVariable String lotNo, @Valid @RequestBody HoldRequest request,
            CurrentUser user) {
        user.requireRole(Role.QC);
        StockLot lot = holdService.placeHold(lotNo, request.reasonCode(), request.comment(), user.userId());
        return ApiResponse.ok(LotResponse.from(lot));
    }
}
