# 재고 현황 쿼리 리팩토링 검토 (`StockSummaryMapper.selectStockSummaryList`)

작성: 2026-09-29 / 대상: `m6-start` 대비 `src/main/resources/mapper/stock/StockSummaryMapper.xml`

## 문제점 (리팩토링 전)

| # | 내용 | 근거 |
|---|---|---|
| 1 | 판매 가능분·보류분을 `UNION ALL`로 따로 구해 컬럼 목록·조건·`GROUP BY`가 두 번씩 반복 | 리팩토링 전 23~61행 (`UNION ALL` 42행) |
| 2 | 소비기한 월 조건이 `YEAR()`·`MONTH()` 함수라 소비기한 인덱스를 쓰지 못함 | 39, 59행 |
| 3 | 바깥 `SELECT *` (C3), 조인한 `product`에 `WITH (NOLOCK)` 없음 (C1) | 21, 29, 49행 |

## 변경 전후

| 항목 | 전 | 후 |
|---|---|---|
| 상태 조건 | `UNION ALL` 두 덩어리 (`AVAILABLE` / `HOLD`) | `status IN ('AVAILABLE', 'HOLD')` 한 번 |
| 반복 컬럼 | 세 번 반복 | `<sql id="lotGroupColumns">` + `<include>` (C6) |
| 월 조건 | `YEAR(l.expiry_date) = ? AND MONTH(l.expiry_date) = ?` | `<bind>`로 월초·다음 달 월초를 만들어 `expiry_date >= ? AND expiry_date < ?` |
| 컨벤션 | C1·C3 위반 | 위반 없음 (`node scripts/check-conventions.mjs`) |
| 그대로 둔 것 | 보류 조인의 `AND h.released_at IS NULL`(ON 절), `COUNT(h.hold_id)`, 마지막 해제 일시 상관 서브쿼리 | 같음 (결과 보존, 아래) |

## 결과 보존 근거

- `StockSummaryResultLockTest` 7개 (리팩토링 전 쿼리 결과를 기대값으로 고정): 리팩토링 후 수정 없이 모두 통과
  - 경계: 보류 없는 로트, 사유 두 개 로트, 해제 이력만 있는 로트, 소비기한 없는 로트, 소진 로트
  - 조합: 상품코드 앞부분 / 분류+상품코드 / 기준월 / 분류+기준월(결과 없음) / 상품코드 전체+기준월 / 분류만 / 조건 없음
- 대량 데이터(로트 20만 건)에서 전·후 행 수 같음: 3,368행 (분류 FRESH, 기준월 2027-03)
- 고정 테스트가 잡는지 확인: `COUNT(h.hold_id)` → `COUNT(*)`로 바꾸면 6개 실패, `released_at IS NULL`을 WHERE로 옮기면 5개 실패

## 측정값

조건: H2 2.4 파일 DB(MSSQL 호환 모드), 로트 200,000건·보류 약 37,000건(`./gradlew dbReset -Pperf`), 파라미터 분류 FRESH + 기준월 2027-03, `node scripts/render-sql.mjs --inline`으로 만든 SQL을 `node scripts/db.mjs --env dev --file`로 3회 실행.

| | 1회 | 2회 | 3회 | 행 수 |
|---|---|---|---|---|
| 전 | 1,147 ms | 1,163 ms | 1,462 ms | 3,368 |
| 후 | 680 ms | 684 ms | 629 ms | 3,368 |

- EXPLAIN: 후 쿼리는 `IX_STOCK_LOT_EXPIRY: EXPIRY_DATE >= DATE '2027-03-01' AND EXPIRY_DATE < DATE '2027-04-01'`로 범위 조회. 전 쿼리는 소비기한 인덱스를 쓰지 않음.
- H2 결과다. 운영 MSSQL의 실행계획·시간과 같다고 보지 않는다.

## 미검증 항목

| 항목 | 이유 | 확인 방법 |
|---|---|---|
| 상관 서브쿼리를 JOIN + GROUP BY로 바꾸는 안 | 이번 범위에서 측정만 하고 적용하지 않음 | 고정 테스트 + 같은 조건 측정 후 결정 |
| 운영 MSSQL 실행계획 | 로컬은 H2 | 운영 읽기 전용 계정으로 실행계획 확인 (DBA 협의) |
| 분류·상품코드 조건만 있을 때의 성능 | 측정한 조합은 기준월 포함 한 가지 | 같은 방법으로 추가 측정 |

## 완료 체크리스트

- 실행: [x] 재고 현황 API가 같은 응답을 낸다 (`StockSummaryApiTest`)
- 테스트: [x] 고정 테스트를 고치지 않고 통과 [x] 전체 `./gradlew test` 통과 [x] 린트 통과
- 측정: [x] 같은 조건·같은 데이터로 전·후 3회씩 [x] EXPLAIN 비교 [x] 행 수 비교
- 리뷰: [x] 고친 세션과 분리된 세션에서 diff 리뷰 (결과가 달라질 수 있는 변경 없음, 컨벤션 위반 없음)
