package com.example.shop.stock.application;

import com.example.shop.stock.domain.StockSummary;
import com.example.shop.stock.mapper.StockSummaryMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 재고 현황 서비스 (상품 담당 화면).
 */
@Service
public class StockSummaryService {

    private final StockSummaryMapper summaryMapper;

    public StockSummaryService(StockSummaryMapper summaryMapper) {
        this.summaryMapper = summaryMapper;
    }

    /** 재고 현황을 조회한다. 기준월(year, month)은 소비기한이 그 달인 로트만. */
    @Transactional(readOnly = true)
    public List<StockSummary> findSummary(String category, String productCode, Integer year, Integer month) {
        return summaryMapper.selectStockSummaryList(category, productCode, year, month);
    }
}
