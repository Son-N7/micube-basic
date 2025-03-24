-- 개발 DB 스키마 (MSSQL 문법, H2 MODE=MSSQLServer). 애플리케이션이 시작할 때마다 새로 만든다 (application.yml).
-- 운영 DB에는 자동 반영되지 않는다. 바꾸면 운영 반영이 필요한 DDL을 따로 정리한다.

-- 상품 (기본정보는 상품 관리 시스템에서 받아 온다)
CREATE TABLE product (
    product_code   VARCHAR(20)  NOT NULL PRIMARY KEY,
    product_name   VARCHAR(100) NOT NULL,
    category       VARCHAR(20)  NOT NULL,   -- FRESH 신선, FROZEN 냉동, PROCESSED 가공, LIVING 생활용품
    price          INT          NOT NULL,
    use_yn         CHAR(1)      DEFAULT 'Y' NOT NULL
);

-- 입고 로트
CREATE TABLE stock_lot (
    lot_no         VARCHAR(20)  NOT NULL PRIMARY KEY,
    product_code   VARCHAR(20)  NOT NULL,
    qty            INT          NOT NULL,
    status         VARCHAR(10)  NOT NULL,   -- AVAILABLE, HOLD, SOLD_OUT
    expiry_date    DATE,                    -- 소비기한 (포장 표시, 생활용품 등은 없음)
    received_at    DATETIME     NOT NULL,
    received_by    VARCHAR(20)  NOT NULL
);
CREATE INDEX ix_stock_lot_product ON stock_lot (product_code);
CREATE INDEX ix_stock_lot_expiry ON stock_lot (expiry_date);
-- 로트 번호 일련번호: 날짜와 상관없이 이어지고, 샘플 데이터(0001~)와 겹치지 않게 1001부터 시작한다
CREATE SEQUENCE lot_seq START WITH 1001;

-- 판매 보류 (사유별 건 단위. 해제하면 released_at 을 채운다. 활성 보류 = released_at 없음)
CREATE TABLE stock_hold (
    hold_id         BIGINT       IDENTITY PRIMARY KEY,
    lot_no          VARCHAR(20)  NOT NULL,
    reason_code     VARCHAR(20)  NOT NULL,   -- QUALITY 품질, CLAIM 고객 클레임
    hold_comment    VARCHAR(500),
    held_at         DATETIME     NOT NULL,
    held_by         VARCHAR(20)  NOT NULL,
    released_at     DATETIME,
    released_by     VARCHAR(20),
    release_comment VARCHAR(500)
);
CREATE INDEX ix_stock_hold_lot ON stock_hold (lot_no);
