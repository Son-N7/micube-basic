# 짝 테스트 골격

```java
@ShopIntegrationTest
class <Xxx>ApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    TestData data;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbcTemplate);
    }

    @Test
    void <성공_시나리오>() throws Exception {
        String lotNo = data.availableLot();

        mockMvc.perform(post("/api/lots/{lotNo}/<경로>", lotNo)
                        .header("X-User-Id", "u01").header("X-User-Roles", "<역할>")
                        .contentType(MediaType.APPLICATION_JSON).content("<본문 JSON>"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("<상태>"));

        assertThat(data.lotStatus(lotNo)).isEqualTo("<상태>");
    }

    @Test
    void <실패_시나리오>() throws Exception {
        // 상태 코드와 $.error.code, 그리고 DB가 그대로인지까지 확인한다
    }
}
```
