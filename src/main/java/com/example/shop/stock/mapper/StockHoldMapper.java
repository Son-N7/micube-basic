package com.example.shop.stock.mapper;

import com.example.shop.stock.domain.HoldReason;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 판매 보류 매퍼. SQL은 {@code resources/mapper/stock/StockHoldMapper.xml}.
 */
@Mapper
public interface StockHoldMapper {

    /** 보류를 기록한다. */
    void insertHold(@Param("lotNo") String lotNo, @Param("reason") HoldReason reason,
            @Param("comment") String comment, @Param("heldAt") LocalDateTime heldAt, @Param("heldBy") String heldBy);

    /** 한 사유의 활성 보류 건수. */
    int selectActiveHoldCount(@Param("lotNo") String lotNo, @Param("reason") HoldReason reason);
}
