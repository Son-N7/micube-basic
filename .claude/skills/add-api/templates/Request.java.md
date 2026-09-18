# Request 골격

```java
package com.example.shop.<기능>.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * <요청 설명>.
 *
 * @param <항목> <설명 (필수 여부, 범위)>
 */
public record <Xxx>Request(@NotNull <Enum> <항목>, @NotBlank @Size(max = 500) String <문자열 항목>) {
}
```

- 선택 항목은 검증 애너테이션 중 `@NotBlank`·`@NotNull`을 빼고 `@Size`만 둔다
