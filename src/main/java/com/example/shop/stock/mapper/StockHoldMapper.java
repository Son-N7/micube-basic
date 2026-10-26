package com.example.shop.stock.mapper;

import com.example.shop.stock.domain.HoldReason;
import com.example.shop.stock.domain.StockHold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 판매 보류 매퍼. SQL은 {@code resources/mapper/stock/StockHoldMapper.xml}.
 */
@Mapper
public interface StockHoldMapper {

    /** 한 로트에서 한 사유의 활성 보류(해제되지 않은 보류) 개수. */
    int selectActiveHoldCount(@Param("lotNo") String lotNo, @Param("reasonCode") HoldReason reasonCode);

    /** 보류를 저장한다. */
    void insertHold(StockHold hold);
}
