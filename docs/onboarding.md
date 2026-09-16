# 신규 입사자 온보딩 (재고 백오피스)

작성: 김철수 / 2025-04-02, 보완: 2026-09-16 (코드와 대조)

재고 백오피스 개발에 처음 합류한 분을 위한 안내입니다. 업무 용어는 `docs/domain-guide.md`, 개발 규칙은 `CLAUDE.md`를 먼저 읽어 주세요.

## 개발 환경

- JDK, Gradle wrapper 포함. `./gradlew build`로 빌드와 테스트를 한 번에 돌립니다.
- 로컬 실행은 `./gradlew bootRun` (http://localhost:8080). 애플리케이션 DB는 내장 H2(MSSQL 호환 모드)라 따로 설치하지 않습니다.
- SQL을 직접 실행해 볼 때는 `./gradlew dbReset`으로 개발 DB를 만든 뒤 `node scripts/db.mjs --env dev "SELECT ..."`를 씁니다.
- SQL은 MyBatis 매퍼 XML(`src/main/resources/mapper/`)에 있습니다. 사내 MyBatis 컨벤션(`docs/conventions/mybatis-convention.md`)을 꼭 읽어 주세요.

## 꼭 알아야 할 업무 규칙

- 출고는 로트를 지정해서 합니다 (`POST /api/lots/{lotNo}/shipments`). 자동으로 로트를 고르지 않습니다. 로트 목록 API가 소비기한 빠른 순으로 보여 줄 뿐입니다.
- 판매 보류 사유는 품질(QUALITY)과 고객 클레임(CLAIM) 두 가지입니다 (`HoldReason`). 보류가 걸린 로트는 HOLD가 되고 출고할 수 없습니다.
- 판매 보류는 지금까지 품질팀 요청을 받아 DB에 직접 넣었습니다. 등록·해제 API는 아직 없습니다.

## 자주 쓰는 API

| API | 설명 |
|---|---|
| `GET /api/products/{productCode}` | 상품 조회 (MD) |
| `POST /api/lots` | 입고 (WAREHOUSE) |
| `POST /api/lots/{lotNo}/shipments` | 출고 (WAREHOUSE) |
| `GET /api/lots?productCode=` | 상품의 재고 로트 목록, 소비기한 빠른 순 (WAREHOUSE) |

`GET /api/lots/expiring`(소비기한 임박 로트)은 지금 코드에 없습니다. 확인 필요: 언제 없어졌는지, MD 일일 점검을 지금 어떻게 하는지 (물류팀).

## 코드 읽는 순서

1. `stock/api/LotController` → `stock/application/LotService` → `stock/mapper/StockLotMapper` → `resources/mapper/stock/StockLotMapper.xml`
2. 오류는 `common/ErrorCode`에 등록하고 `BusinessException`으로 던집니다. 응답은 모두 `ApiResponse`(`success`/`data`/`error`) 형식입니다.

## 알려진 위반 (기존 코드, 고치지 않음)

새 쿼리에서 따라 하지 않습니다.

| 파일:줄 | 규칙 | 내용 |
|---|---|---|
| `resources/mapper/stock/StockLotMapper.xml:27` | C3 | `selectLotOne`이 `SELECT *` |
| `resources/mapper/stock/StockLotMapper.xml:35` | C1 | `selectInStockListByProduct`에서 조인한 `product`에 `WITH (NOLOCK)` 없음 |

## 문의

- 업무 규칙: 품질팀 최수진, 물류팀
- 코드: 김철수
