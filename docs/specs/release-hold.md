# 판매 보류 해제 API

품질팀이 사유가 해소된 보류를 해제한다. 보류는 사유별로 해제한다.

## API

`POST /api/lots/{lotNo}/holds/release` — 역할 `QC`

요청 본문

| 항목 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `reasonCode` | 문자열 | O | 해제할 보류의 사유. `QUALITY` 또는 `CLAIM` |
| `comment` | 문자열 | - | 해제 사유 설명. 500자 이내 |

응답: 200, 로트 (`LotResponse`)

## 처리

1. 로트가 없으면 404
2. 요청한 사유의 활성 보류가 없으면 거부한다
3. 요청한 사유의 활성 보류만 해제한다 (해제 일시, 해제한 사용자, 해제 코멘트 기록). **다른 사유의 보류는 그대로 둔다**
4. 남은 활성 보류가 있으면 로트는 HOLD를 유지하고, 없으면 AVAILABLE로 바꾼다. 수량이 0이면 SOLD_OUT
5. 3·4는 한 트랜잭션

## 오류

| 상황 | HTTP | 오류 코드 |
|---|---|---|
| QC 역할 아님 | 403 | `FORBIDDEN` |
| 로트 없음 | 404 | `LOT_NOT_FOUND` |
| 요청한 사유의 활성 보류 없음 | 409 | `NO_ACTIVE_HOLD` |
| `reasonCode` 없음·잘못된 값, `comment` 500자 초과 | 400 | `INVALID_REQUEST` |

## 확인 시나리오

| # | 요청 | 기대 |
|---|---|---|
| 1 | QUALITY 보류만 있는 `LT-261002-0006`에서 `QUALITY` 해제 | 200, 상태 AVAILABLE, 보류에 해제 일시·해제자 기록 |
| 2 | QUALITY·CLAIM 보류가 모두 있는 로트에서 `CLAIM` 해제 | 200, 상태 HOLD 유지, QUALITY 활성 보류 1건 남음 |
| 3 | 보류 없는 `LT-261001-0001`에서 `QUALITY` 해제 | 409 `NO_ACTIVE_HOLD` |
| 4 | WAREHOUSE가 해제 | 403, 데이터 변화 없음 |
| 5 | `comment` 없이 해제 | 200 |
