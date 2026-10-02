# 기능정의서 SHOP-FD-012 판매 보류 등록·해제

| 문서 번호 | SHOP-FD-012 | 버전 | 1.2 |
|---|---|---|---|
| 시스템 | 재고 백오피스 | 작성 | 김철수 (2026-09-12) |
| 관련 화면 | 품질 관리 > 판매 보류 | 승인 | 품질팀 최수진 |

## 1. 개요

품질팀이 문제가 생긴 입고 로트를 사유별로 판매 보류하고, 사유가 해소되면 그 사유의 보류를 해제한다.
판매 보류 중인 로트는 출고할 수 없다.

## 2. 요청·응답

### 2.1 판매 보류 등록 `POST /api/lots/{lotNo}/holds`

| 구분 | 항목 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| 경로 | lotNo | String(20) | Y | 로트 번호 |
| 요청 | reasonCode | String | Y | 보류 사유. QUALITY(품질), CLAIM(고객 클레임) |
| 요청 | comment | String(200) | Y | 보류 사유 설명. 공백만 입력 불가 |
| 응답(데이터) | lotNo, productCode, qty, status, expiryDate | - | - | 처리 후 로트 |
| 응답(처리 결과) | success, error.code, error.message | - | - | 공통 응답 형식 |

### 2.2 판매 보류 해제 `POST /api/lots/{lotNo}/holds/release`

| 구분 | 항목 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| 경로 | lotNo | String(20) | Y | 로트 번호 |
| 요청 | reasonCode | String | Y | 해제할 보류의 사유 |
| 요청 | comment | String(500) | N | 해제 사유 설명 |
| 응답(데이터) | lotNo, productCode, qty, status, expiryDate | - | - | 처리 후 로트 |
| 응답(처리 결과) | success, error.code, error.message | - | - | 공통 응답 형식 |

권한: 두 API 모두 품질 담당(QC)만 호출할 수 있다.

## 3. 업무 프로세스

### 3.1 등록

1. 요청 사용자의 역할이 QC인지 확인한다
2. 요청 값을 검증한다 (사유 코드, 코멘트 필수·길이)
3. 로트를 조회한다. 없으면 오류
4. 소진 로트면 오류
5. 같은 사유의 활성 보류가 있으면 오류
6. 판매 보류 이력(stock_hold)에 1건을 기록한다
7. 로트 상태를 HOLD로 바꾼다 (수량은 바꾸지 않음)

### 3.2 해제

1. 요청 사용자의 역할이 QC인지 확인한다
2. 로트를 조회한다. 없으면 오류
3. 요청한 사유의 활성 보류가 없으면 오류
4. 요청한 사유의 활성 보류에 해제 일시·해제자·해제 코멘트를 기록한다
5. 남은 활성 보류가 있으면 HOLD를 유지하고, 남은 보류가 없으면 AVAILABLE로 바꾼다

## 4. 오류 코드

| 코드 | HTTP | 상황 |
|---|---|---|
| FORBIDDEN | 403 | QC 역할이 아님 |
| INVALID_REQUEST | 400 | 필수 값 누락, 길이 초과 |
| HOLD_REASON_INVALID | 400 | 정의되지 않은 사유 코드 |
| LOT_NOT_FOUND | 404 | 로트 없음 |
| LOT_SOLD_OUT | 409 | 소진 로트 보류 |
| HOLD_ALREADY_EXISTS | 409 | 같은 사유의 활성 보류가 이미 있음 |
| NO_ACTIVE_HOLD | 409 | 해제할 활성 보류 없음 |

## 5. SQL

| 쿼리 | 매퍼 | 내용 |
|---|---|---|
| 활성 보류 건수 (사유별) | `StockHoldMapper.selectActiveHoldCount` | `stock_hold` WITH (NOLOCK), lot_no·reason_code, released_at IS NULL |
| 활성 보류 건수 (로트) | `StockHoldMapper.selectLotActiveHoldCount` | `stock_hold` WITH (NOLOCK), lot_no, released_at IS NULL |
| 보류 기록 | `StockHoldMapper.insertHold` | `stock_hold` INSERT |
| 보류 해제 | `StockHoldMapper.updateHoldRelease` | `stock_hold` UPDATE, lot_no·reason_code·released_at IS NULL 조건 |
| 로트 상태 변경 | `StockLotMapper.updateLotStatus` | `stock_lot` UPDATE |

## 6. 단위테스트

| ID | 시나리오 | 기대 결과 |
|---|---|---|
| UT-01 | QC가 판매 가능 로트에 QUALITY 보류 | 201, HOLD, 보류 1건 |
| UT-02 | 같은 사유로 다시 보류 | 409 HOLD_ALREADY_EXISTS |
| UT-03 | 다른 사유(CLAIM) 추가 보류 | 201, 활성 보류 2건 |
| UT-04 | 소진 로트 보류 | 409 LOT_SOLD_OUT |
| UT-05 | WAREHOUSE 사용자가 보류 등록 | 403 |
| UT-06 | 코멘트 공백 | 400 |
| UT-07 | QUALITY 보류만 있는 로트 해제 | 200, AVAILABLE |
| UT-08 | QUALITY·CLAIM 중 CLAIM만 해제 | 200, HOLD 유지, QUALITY 남음 |
| UT-09 | 활성 보류 없는 로트 해제 | 409 NO_ACTIVE_HOLD |
| UT-10 | WAREHOUSE 사용자가 보류 해제 | 403, 보류 그대로 |
