# API 구현 규칙 (add-api 참고)

기준 API: 출고 `POST /api/lots/{lotNo}/shipments` (`stock/api/LotController.ship`)

## 계층과 파일

| 계층 | 파일 | 할 일 |
|---|---|---|
| mapper | `<기능>/mapper/XxxMapper.java`, `resources/mapper/<기능>/XxxMapper.xml` | 쿼리. 규칙은 `.claude/rules/mybatis.md` |
| application | `<기능>/application/XxxService.java` | 업무 처리. 변경은 `@Transactional`, 조회는 `@Transactional(readOnly = true)` |
| api | `<기능>/api/XxxRequest.java`, `XxxController.java` | 입력 검증, 역할 확인, 응답 변환 |
| 테스트 | `src/test/.../<기능>/api/XxxApiTest.java` | 짝 테스트 |

- Controller는 Service만 부른다. 다른 기능의 데이터는 그 기능의 Service로 (예: `ProductService.getProduct`).
- Service는 Request를 받지 않는다. 원시값·enum을 받고 도메인 객체(`StockLot` 등)를 돌려준다.
- 대상이 없으면 `BusinessException(ErrorCode.XXX_NOT_FOUND, 번호)`, 상태 충돌은 409 오류 코드를 `ErrorCode`에 추가한다.

## 요청·응답

- Controller 첫 줄: `user.requireRole(Role.X);`
- Request는 record + Bean Validation. 필수 문자열 `@NotBlank`, 길이 `@Size(max = …)`, enum·숫자 `@NotNull`. 파라미터에 `@Valid`
- 잘못된 enum 값·검증 실패는 `GlobalExceptionHandler`가 400 `INVALID_REQUEST`로 바꾼다
- 응답: `return ApiResponse.ok(LotResponse.from(lot));` 생성은 `@ResponseStatus(HttpStatus.CREATED)`

## 테스트 구성

| 케이스 | 확인할 것 |
|---|---|
| 성공 | 상태 코드, 응답 값, **DB에 반영된 값** (`TestData`) |
| 입력 오류 | 400 `INVALID_REQUEST`, DB 변화 없음 |
| 권한 없음 | 403, DB 변화 없음 |
| 대상 없음 | 404 |
| 상태 충돌 | 409, 해당 오류 코드, DB 변화 없음 |

- `@ShopIntegrationTest` + MockMvc + `TestData`. 안내는 `src/test/CLAUDE.md`.
