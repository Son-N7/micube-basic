-- ./gradlew dbReset -Pperf : 개발 DB에 대량 데이터를 넣는다 (6번 모듈 측정용).
-- 상품 1,000개, 로트 200,000건, 보류 약 37,000건. 실행할 때마다 같은 데이터가 만들어진다.

INSERT INTO product (product_code, product_name, category, price)
SELECT 'Q' || LPAD(CAST(x AS VARCHAR), 4, '0'),
       '대량 상품 ' || x,
       CASE MOD(x, 4) WHEN 0 THEN 'FRESH' WHEN 1 THEN 'FROZEN' WHEN 2 THEN 'PROCESSED' ELSE 'LIVING' END,
       1000 + MOD(x * 37, 20000)
  FROM SYSTEM_RANGE(1, 1000);

-- 로트: 약 89% 판매 가능, 6% 보류, 5% 소진. 소비기한은 2026-10 ~ 2027-12 (생활용품은 없음)
INSERT INTO stock_lot (lot_no, product_code, qty, status, expiry_date, received_at, received_by)
SELECT 'PF-' || LPAD(CAST(x AS VARCHAR), 6, '0'),
       'Q' || LPAD(CAST(MOD(x, 1000) + 1 AS VARCHAR), 4, '0'),
       CASE WHEN MOD(x, 19) = 7 THEN 0 ELSE 10 + MOD(x * 7, 190) END,
       CASE WHEN MOD(x, 19) = 7 THEN 'SOLD_OUT' WHEN MOD(x, 17) = 5 THEN 'HOLD' ELSE 'AVAILABLE' END,
       CASE WHEN MOD(MOD(x, 1000) + 1, 4) = 3 THEN NULL
            ELSE DATEADD('DAY', MOD(x * 13, 450), DATE '2026-10-01') END,
       DATEADD('MINUTE', MOD(x, 50000), TIMESTAMP '2026-06-01 08:00:00'),
       'wh0' || (MOD(x, 3) + 1)
  FROM SYSTEM_RANGE(1, 200000);

-- 보류 로트(HOLD)마다 활성 보류 1건, 그중 절반은 사유 2건
INSERT INTO stock_hold (lot_no, reason_code, hold_comment, held_at, held_by)
SELECT 'PF-' || LPAD(CAST(x AS VARCHAR), 6, '0'), 'QUALITY', '대량 데이터', TIMESTAMP '2026-09-01 10:00:00', 'qc01'
  FROM SYSTEM_RANGE(1, 200000) WHERE MOD(x, 17) = 5 AND MOD(x, 19) <> 7;
INSERT INTO stock_hold (lot_no, reason_code, hold_comment, held_at, held_by)
SELECT 'PF-' || LPAD(CAST(x AS VARCHAR), 6, '0'), 'CLAIM', '대량 데이터', TIMESTAMP '2026-09-02 10:00:00', 'qc02'
  FROM SYSTEM_RANGE(1, 200000) WHERE MOD(x, 34) = 5 AND MOD(x, 19) <> 7;

-- 로트 일부(약 10%)에 해제된 보류 이력
INSERT INTO stock_hold (lot_no, reason_code, hold_comment, held_at, held_by, released_at, released_by, release_comment)
SELECT 'PF-' || LPAD(CAST(x AS VARCHAR), 6, '0'), 'QUALITY', '대량 데이터', TIMESTAMP '2026-08-01 10:00:00', 'qc01',
       TIMESTAMP '2026-08-03 15:00:00', 'qc01', '이상 없음'
  FROM SYSTEM_RANGE(1, 200000) WHERE MOD(x, 10) = 3;
