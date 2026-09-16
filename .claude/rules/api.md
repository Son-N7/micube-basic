---
paths:
  - "src/main/java/**/api/**"
---

# API 작성 규칙

기준 API: 출고 `stock/api/LotController.ship` (`POST /api/lots/{lotNo}/shipments`)

- Controller 메서드 첫 줄에서 역할을 확인한다: `user.requireRole(Role.X)` (`CurrentUser user` 파라미터)
- 요청 본문은 `XxxRequest` record + Bean Validation(`@NotBlank`, `@Size`, `@NotNull`, `@Min`), 파라미터에 `@Valid`
- 응답은 `ApiResponse.ok(XxxResponse.from(도메인))`. 생성은 `@ResponseStatus(HttpStatus.CREATED)`
- Controller는 Service만 부른다. 매퍼를 직접 쓰지 않는다
- 오류 코드: 대상 없음 404, 상태 충돌 409, 입력 오류 400(`INVALID_REQUEST`, 자동), 권한 없음 403(`FORBIDDEN`)
- 새 API마다 짝 테스트 `XxxApiTest`를 만든다 (`src/test/CLAUDE.md`)
