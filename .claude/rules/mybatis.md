---
paths:
  - "src/main/resources/mapper/**"
  - "src/main/java/**/mapper/**"
---

# 매퍼 규칙

원문: `docs/conventions/mybatis-convention.md` (아래는 C1~C5 요약)

- C1 조회하는 모든 테이블(조인·서브쿼리 포함)에 `WITH (NOLOCK)`. INSERT·UPDATE·DELETE에는 붙이지 않는다
- C2 값은 `#{}`로만 바인딩. `${}` 금지. 정렬 컬럼은 `<choose>`로 허용한 것만
- C3 `SELECT *` 금지. 컬럼을 나열하고 `resultMap`(record는 `<constructor>`, `<arg>`는 생성자 순서대로)으로 매핑
- C4 XML `namespace` = 매퍼 인터페이스 전체 경로, 쿼리 `id` = 메서드 이름. 파라미터가 둘 이상이면 모두 `@Param`, 그 이름 = `#{}` 이름
- C5 쿼리 id: `select{대상}{One|List|Count}`, `insert{대상}`, `update{대상}{바꾸는 것}`, `delete{대상}`

기존 매퍼의 위반은 `docs/onboarding.md` "알려진 위반"에 있다. 새 쿼리에서 따라 하지 않는다.
