# 사내 MyBatis 컨벤션

개발팀 공통 규칙입니다. 리뷰에서 이 문서를 기준으로 봅니다. (개발팀 / 2024-11 개정)

## 1. 조회 잠금

**C1.** 조회(SELECT)하는 모든 테이블에 `WITH (NOLOCK)`을 붙인다. 조인하는 테이블, 서브쿼리 안의 테이블도 모두 붙인다. INSERT·UPDATE·DELETE에는 붙이지 않는다.

```xml
SELECT l.lot_no, p.product_name
  FROM stock_lot l WITH (NOLOCK)
  JOIN product p WITH (NOLOCK) ON p.product_code = l.product_code
```

## 2. 파라미터 바인딩

**C2.** 값은 `#{}`로만 바인딩한다. `${}`는 SQL 인젝션 위험이 있어 쓰지 않는다. 정렬처럼 컬럼 이름이 바뀌어야 하면 `<choose>`로 허용한 컬럼만 고른다.

## 3. 조회 컬럼과 결과 매핑

**C3.** `SELECT *`를 쓰지 않는다. 필요한 컬럼을 나열하고, 결과는 `resultMap`으로 컬럼과 필드를 명시해 매핑한다. record는 `<constructor>`를 쓰고 `<arg>` 순서는 record 생성자 순서와 같게 한다.

## 4. 매퍼 인터페이스와 XML의 짝

**C4.** XML의 `namespace`는 매퍼 인터페이스의 전체 경로, 쿼리 `id`는 메서드 이름과 같다. 파라미터가 둘 이상이면 모두 `@Param`을 붙이고, 그 이름과 `#{}` 이름을 맞춘다.

**C5.** 쿼리 id(=메서드 이름)는 `select{대상}{One|List|Count}`, `insert{대상}`, `update{대상}{바꾸는 것}`, `delete{대상}` 형식으로 짓는다.

## 5. 재사용

**C6.** 여러 쿼리가 같이 쓰는 컬럼 목록·조건은 `<sql id>`로 빼고 `<include>`로 쓴다.

## 6. 동적 SQL

**C7.** 동적 조건은 `<where>`·`<if>`로 쓴다. 문자열 조건은 `null`과 빈 문자열(`''`)을 모두 검사하고, `<foreach>`는 빈 목록일 때를 처리한다.

## 7. 운영 DB

**C8.** 운영 DB는 조회만 한다. 데이터 변경은 사람이 승인한 스크립트로만 한다. 개발 PC에는 운영 읽기 전용 계정만 둔다.

## 8. 위치와 이름

- 매퍼 인터페이스: `<기능>/mapper/XxxMapper.java` (`@Mapper`)
- 매퍼 XML: `src/main/resources/mapper/<기능>/XxxMapper.xml`
- SQL은 MSSQL 문법으로 쓴다 (`TOP n`, `ISNULL`, `GETDATE()`). 로컬은 H2를 MSSQL 호환 모드로 쓴다.
- 한 건 조회 메서드는 `Optional<T>`를 돌려준다.
