# shop-basic

식품 온라인몰 재고 백오피스 (실습용 가상 시스템).

## 기술 스택

Java 25, Spring Boot 4.1, MyBatis, H2 (MSSQL 호환 모드), Gradle (wrapper 포함)

## 패키지 구조 (`src/main/java/com/example/shop`)

기능(`product`, `stock`) 아래를 계층으로 나눈다. `common`·`config`는 공통.

| 계층 | 두는 것 |
|---|---|
| `api` | Controller, `XxxRequest`, `XxxResponse` |
| `application` | Service (`@Transactional` 경계) |
| `domain` | record, enum, 업무 규칙 |
| `mapper` | MyBatis 매퍼 인터페이스. SQL은 `src/main/resources/mapper/<기능>/XxxMapper.xml` |

## 핵심 규칙

- 의존 방향: api → application → domain·mapper. 다른 기능의 데이터는 그 기능의 Service로 가져온다 (예: 상품 정보는 `ProductService`). 그 Service가 돌려주는 domain 타입은 써도 된다.
- 업무 오류는 `ErrorCode`에 등록하고 `BusinessException`으로 던진다. 응답은 `ApiResponse`로 감싼다.
- 현재 날짜·시각은 주입받은 `Clock`으로만 구한다.
- SQL은 MSSQL 문법으로 쓴다.
- 운영 DB(`--env prod`)는 조회만 한다.
- 완료 전에 `./gradlew test`와 `./gradlew checkstyleMain`을 통과시킨다.
- 요청 범위 밖의 리팩터링은 하지 않는다.

## 명령

| 할 일 | 명령 |
|---|---|
| 빌드 (테스트 + 린트) | `./gradlew build` |
| 전체 테스트 / 테스트 하나 | `./gradlew test` / `./gradlew test --tests "com.example.shop.stock.api.LotApiTest"` |
| 린트 | `./gradlew checkstyleMain` |
| 실행 | `./gradlew bootRun` |
| 실행 (개발 파일 DB에 연결, 호출 결과를 db.mjs로 확인) | `./gradlew bootRun --args='--spring.profiles.active=devdb'` |
| DB 다시 만들기 (개발·운영 흉내) | `./gradlew dbReset` |
| SQL 실행 (개발 DB) | `node scripts/db.mjs --env dev "SELECT ..."` |
| 매퍼 SQL 렌더링 | `node scripts/render-sql.mjs XxxMapper.queryId params.json` |

## 언어

- 답변과 문서는 한국어로 쓴다.

## 세부 규칙과 문서

| 주제 | 위치 | 언제 올라오나 |
|---|---|---|
| 매퍼 작성 규칙 | `.claude/rules/mybatis.md` (원문: `docs/conventions/mybatis-convention.md`) | 매퍼 XML·인터페이스를 다룰 때 |
| 커밋 메시지 | `.claude/rules/git.md` | 항상 |
| 테스트 작성 | `src/test/CLAUDE.md` | 테스트 파일을 다룰 때 |
| API 작성 (선택 과제) | `.claude/rules/api.md` | API 파일을 다룰 때 |
| 업무 용어·흐름 | `docs/domain-guide.md` | 필요할 때 읽음 |
| 온보딩, 알려진 위반 | `docs/onboarding.md` | 필요할 때 읽음 |
