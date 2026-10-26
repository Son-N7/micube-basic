package com.example.shop.stock.application;

import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.product.application.ProductService;
import com.example.shop.stock.domain.HoldReason;
import com.example.shop.stock.domain.LotStatus;
import com.example.shop.stock.domain.StockHold;
import com.example.shop.stock.domain.StockLot;
import com.example.shop.stock.mapper.StockHoldMapper;
import com.example.shop.stock.mapper.StockLotMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 입고 로트 서비스: 입고, 출고, 판매 보류, 로트 조회.
 */
@Service
public class LotService {

    private final StockLotMapper lotMapper;
    private final StockHoldMapper holdMapper;
    private final ProductService productService;
    private final Clock clock;

    public LotService(StockLotMapper lotMapper, StockHoldMapper holdMapper, ProductService productService,
            Clock clock) {
        this.lotMapper = lotMapper;
        this.holdMapper = holdMapper;
        this.productService = productService;
        this.clock = clock;
    }

    /** 입고한다. 로트를 새로 만들고 판매 가능 상태로 둔다. 소비기한은 없을 수 있다(생활용품 등). */
    @Transactional
    public StockLot receive(String productCode, int qty, LocalDate expiryDate, String userId) {
        productService.getProduct(productCode);
        LocalDateTime now = LocalDateTime.now(clock);
        StockLot lot = new StockLot(lotMapper.nextLotNo(now.toLocalDate()), productCode, qty,
                LotStatus.AVAILABLE, expiryDate, now, userId);
        lotMapper.insertLot(lot);
        return lot;
    }

    /** 출고한다. 판매 보류 중이거나 소진된 로트는 출고할 수 없다. 남은 수량이 0이면 소진. */
    @Transactional
    public StockLot ship(String lotNo, int qty) {
        StockLot lot = getLot(lotNo);
        if (lot.status() != LotStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.LOT_NOT_SHIPPABLE, lotNo + ", " + lot.status());
        }
        if (qty > lot.qty()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_QTY, lotNo + ", 남은 수량 " + lot.qty());
        }
        int remaining = lot.qty() - qty;
        LotStatus status = LotStatus.resolve(remaining, false);
        lotMapper.updateLotQtyAndStatus(lotNo, remaining, status);
        return lot.withQtyAndStatus(remaining, status);
    }

    /** 판매 보류를 건다. 소진 로트는 보류할 수 없고, 같은 사유의 활성 보류가 있으면 거부한다. 수량은 바꾸지 않는다. */
    @Transactional
    public StockLot hold(String lotNo, HoldReason reason, String comment, String userId) {
        StockLot lot = getLot(lotNo);
        if (lot.status() == LotStatus.SOLD_OUT) {
            throw new BusinessException(ErrorCode.LOT_NOT_HOLDABLE, lotNo + ", " + lot.status());
        }
        if (holdMapper.selectActiveHoldCount(lotNo, reason) > 0) {
            throw new BusinessException(ErrorCode.HOLD_ALREADY_EXISTS, lotNo + ", " + reason);
        }
        holdMapper.insertHold(new StockHold(lotNo, reason, comment, LocalDateTime.now(clock), userId));
        lotMapper.updateLotStatus(lotNo, LotStatus.HOLD);
        return lot.withStatus(LotStatus.HOLD);
    }

    /** 상품의 재고 로트(판매 가능·판매 보류)를 소비기한 빠른 순으로 조회한다. 상품이 없으면 404. */
    @Transactional(readOnly = true)
    public List<StockLot> findInStockLots(String productCode) {
        productService.getProduct(productCode);
        return lotMapper.selectInStockListByProduct(productCode);
    }

    private StockLot getLot(String lotNo) {
        return lotMapper.selectLotOne(lotNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND, lotNo));
    }
}
