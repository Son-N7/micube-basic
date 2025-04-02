# 신규 입사자 온보딩 (재고 백오피스)

작성: 김철수 / 2025-04-02

재고 백오피스 개발에 처음 합류한 분을 위한 안내입니다. 업무 용어는 `docs/domain-guide.md`를 먼저 읽어 주세요.

## 개발 환경

- JDK, Gradle wrapper 포함. `./gradlew build`로 빌드와 테스트를 한 번에 돌립니다.
- 로컬 실행은 `./gradlew bootRun` (http://localhost:8080). DB는 내장 H2라 따로 설치하지 않습니다.
- SQL은 MyBatis 매퍼 XML(`src/main/resources/mapper/`)에 있습니다. 쿼리를 작성할 때는 사내 MyBatis 컨벤션(`docs/conventions/mybatis-convention.md`)을 꼭 읽어 주세요.

## 꼭 알아야 할 업무 규칙

- 출고하면 소비기한이 가장 빠른 로트부터 자동으로 나갑니다 (선입선출).
- 판매 보류 사유는 품질(QUALITY) 하나뿐입니다. 보류를 걸면 로트가 HOLD가 되고 출고할 수 없습니다.
- 판매 보류는 품질팀 요청을 받아 DB에 직접 넣습니다. 등록·해제 API는 아직 없습니다.

## 자주 쓰는 API

| API | 설명 |
|---|---|
| `GET /api/products/{productCode}` | 상품 조회 |
| `POST /api/lots` | 입고 |
| `POST /api/lots/{lotNo}/shipments` | 출고 |
| `GET /api/lots/expiring?days=7` | 소비기한 임박 로트 조회 (MD 일일 점검용) |

## 코드 읽는 순서

1. `stock/api/LotController` → `stock/application/LotService` → `stock/mapper/StockLotMapper` → `resources/mapper/stock/StockLotMapper.xml`
2. 오류는 `common/ErrorCode`에 등록하고 `BusinessException`으로 던집니다. 응답은 모두 `ApiResponse`(`success`/`data`/`error`) 형식입니다.

## 문의

- 업무 규칙: 품질팀 최수진, 물류팀
- 코드: 김철수
