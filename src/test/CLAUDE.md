# 테스트 작성 안내

## 통합 테스트 어노테이션

- API 테스트는 `@ShopIntegrationTest`를 붙인다. 스프링 컨텍스트 + 인메모리 H2(`schema.sql`, `data.sql`) + MockMvc, 테스트마다 롤백. 기준: `stock/api/LotApiTest.java`
- `@ShopMapperTest`(MyBatis 슬라이스)도 있다. (support 코드에만 있음, 지금 쓰는 테스트는 없음)

## 테스트 데이터

- `support/TestData`로 만든다 (`new TestData(jdbcTemplate)`). 예: `data.availableLot()`, `data.lot(TestData.TOFU, 10, LotStatus.AVAILABLE)`
- 테스트 로트 번호는 `TL-` 접두어라 `data.sql`의 로트(`LT-…`)와 겹치지 않는다
- `data.sql`의 상품을 쓸 때는 상수 `TestData.TOFU`(P1001), `TestData.DUMPLING`(P2001)

## 현재 시각 고정

- 현재 시각은 `TestClock`으로 고정된다 (기본 2026-10-12 10:00, 테스트가 끝나면 기본값으로 돌아감)
- 바꾸려면 주입받은 `TestClock`의 `set(...)` (support 코드에만 있음, 지금 쓰는 테스트는 없음)

## 요청 사용자 헤더

- 요청 사용자는 헤더 `X-User-Id`, `X-User-Roles`로 보낸다 (예: `X-User-Roles: WAREHOUSE`)
