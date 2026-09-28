package com.example.shop.stock.mapper;

import com.example.shop.stock.domain.StockSummary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 재고 현황 매퍼. SQL은 {@code resources/mapper/stock/StockSummaryMapper.xml} (구 시스템 쿼리 이관).
 */
@Mapper
public interface StockSummaryMapper {

    /** 재고가 남은 로트를 상품 정보·보류 건수와 함께 조회한다. 조건은 모두 선택. */
    List<StockSummary> selectStockSummaryList(@Param("category") String category,
            @Param("productCode") String productCode, @Param("year") Integer year, @Param("month") Integer month);
}
