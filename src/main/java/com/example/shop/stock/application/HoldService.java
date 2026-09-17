package com.example.shop.stock.application;

import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.stock.domain.HoldReason;
import com.example.shop.stock.domain.LotStatus;
import com.example.shop.stock.domain.StockLot;
import com.example.shop.stock.mapper.StockHoldMapper;
import com.example.shop.stock.mapper.StockLotMapper;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매 보류 서비스: 보류 등록·해제와 그에 따른 로트 상태.
 */
@Service
public class HoldService {

    private final StockLotMapper lotMapper;
    private final StockHoldMapper holdMapper;
    private final Clock clock;

    public HoldService(StockLotMapper lotMapper, StockHoldMapper holdMapper, Clock clock) {
        this.lotMapper = lotMapper;
        this.holdMapper = holdMapper;
        this.clock = clock;
    }

    /** 판매 보류를 등록한다. 소진 로트와, 같은 사유의 활성 보류가 있는 로트는 거부한다. */
    @Transactional
    public StockLot placeHold(String lotNo, HoldReason reason, String comment, String userId) {
        StockLot lot = getLot(lotNo);
        if (lot.status() == LotStatus.SOLD_OUT) {
            throw new BusinessException(ErrorCode.LOT_NOT_HOLDABLE, lotNo);
        }
        if (holdMapper.selectActiveHoldCount(lotNo, reason) > 0) {
            throw new BusinessException(ErrorCode.HOLD_ALREADY_EXISTS, lotNo + ", " + reason);
        }
        holdMapper.insertHold(lotNo, reason, comment, LocalDateTime.now(clock), userId);
        lotMapper.updateLotStatus(lotNo, LotStatus.HOLD);
        return lot.withStatus(LotStatus.HOLD);
    }

    private StockLot getLot(String lotNo) {
        return lotMapper.selectLotOne(lotNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND, lotNo));
    }
}
