---
name: add-api
description: 이 저장소에 REST API를 하나 추가할 때 쓴다. 명세 파일을 받아 기준 API(출고 API)와 같은 구성으로 구현하고, 테스트·린트를 돌린 뒤 커밋 메시지를 제안하고 멈춘다.
argument-hint: <명세 파일 경로>
disable-model-invocation: true
---

# API 추가

인자: `$ARGUMENTS` (예: `docs/specs/release-hold.md`)

## 절차

1. **명세를 읽는다.** API, 요청 항목, 처리 순서, 오류 표, 확인 시나리오를 확인한다. 명세에 없는 기능은 만들지 않는다.
2. **규칙을 읽는다.** `reference.md`를 읽는다.
3. **기준 API를 확인한다.** `stock/api/LotController.ship`, `stock/application/LotService.ship`, `stock/api/ShipRequest`, `LotApiTest`의 지금 코드를 연다. reference·templates와 다르면 지금 코드를 따른다.
4. **파일을 만든다.** 아래 순서로, `templates/`의 골격을 쓴다.
   1. domain: 새 업무 규칙이 필요할 때만
   2. mapper: 매퍼 인터페이스 메서드와 XML 쿼리. `.claude/rules/mybatis.md`를 따른다 (`templates/Mapper.xml.md`)
   3. application: Service 메서드. 상태 변경과 기록은 한 `@Transactional` 메서드에서
   4. api: `XxxRequest`(`templates/Request.java.md`), Controller 메서드(`templates/Controller.java.md`). 응답은 기존 `XxxResponse`를 쓰고, 없으면 만든다
   5. 새 오류는 `common/ErrorCode`에 등록
5. **짝 테스트를 쓴다.** `templates/ApiTest.java.md`로. 명세의 확인 시나리오를 하나씩 테스트로 옮긴다.
6. **실행한다.** `./gradlew test`, `./gradlew checkstyleMain`. 실패하면 고친다. 기존 테스트를 고치거나 지워서 통과시키지 않는다.
7. **보고한다.** 확인 시나리오별 테스트 이름 표, 명세에 없어서 내가 정한 것.
8. **커밋 메시지를 제안하고 멈춘다.** `.claude/rules/git.md` 형식으로 `.claude/tmp/commit-msg.txt`에 저장하고 "검토 후 커밋해 주세요"라고 안내한다. **커밋하지 않는다.**
