# Controller 메서드 골격

```java
/** <설명>. */
@PostMapping("/<경로>")
public ApiResponse<LotResponse> <동작>(@PathVariable String lotNo, @Valid @RequestBody <Xxx>Request request,
        CurrentUser user) {
    user.requireRole(Role.<역할>);
    StockLot lot = <xxx>Service.<동작>(lotNo, request.<항목>(), user.userId());
    return ApiResponse.ok(LotResponse.from(lot));
}
```

- 생성(201)이면 `@ResponseStatus(HttpStatus.CREATED)`를 붙인다
