# shop-basic — Claude Code 기본과정 실습 저장소

식품 온라인몰의 재고 백오피스를 단순화한 실습용 저장소입니다. 내용은 모두 가상입니다.
업무는 [`docs/domain-guide.md`](docs/domain-guide.md)를 봐 주세요.

- Java 25, Spring Boot 4.1, MyBatis, Gradle 9 (wrapper 포함), H2 (MSSQL 호환 모드)
- JDK 25가 없으면 Gradle이 처음 빌드할 때 자동으로 내려받습니다 (사내망에서 막히면 JDK 25를 직접 설치).
- **Windows: Git Bash 필수** (Git for Windows에 포함). 실습의 [터미널] 명령은 macOS 터미널과 Git Bash에서 같습니다. VS Code 기본 터미널을 Git Bash로 바꿔 둡니다.
- Node.js 20 이상 (DB 도구·SQL 렌더러, Hook 스크립트)
- Claude Code는 네이티브 설치를 권장합니다 (`claude --version`으로 확인).

## 수업 전 준비

```bash
git clone <수강생용 저장소 주소> shop-basic
cd shop-basic
./gradlew build            # 의존성 내려받기 + 테스트 + 린트
cp .env.example .env       # DB 접속 정보 (커밋하지 않음)
./gradlew dbReset          # 개발 DB·운영 흉내 DB 만들기 (.local/db)
node scripts/db.mjs --env dev "SELECT TOP 3 lot_no, status FROM stock_lot WITH (NOLOCK)"   # 표가 나오면 준비 완료
```

## 내 GitHub 저장소 연결 (처음 한 번)

실습 결과는 각자 GitHub 저장소에 올려 보관합니다. 클론한 저장소(`origin`)의 이름을 `upstream`으로 바꾸고, 내 저장소를 `origin`으로 연결합니다. GitHub 웹에서 빈 저장소를 만든 뒤(README·.gitignore 추가하지 않음):

```bash
git remote rename origin upstream
git remote add origin https://github.com/<내 계정>/shop-basic.git
git push origin main --tags
```

GitHub에 접근할 수 없는 환경이면 이 단계는 건너뛰고 로컬에 커밋만 해 둡니다.

## 모듈별 실습 흐름

모듈마다 정해진 시작 태그가 있습니다. 앞 모듈 결과와 상관없이, 모듈을 시작할 때마다 **시작 태그에서 내 실습 브랜치를 새로 만들어** 진행합니다. 앞 모듈의 모범 답안은 다음 모듈 시작 태그에 들어 있습니다. `main`과 태그는 건드리지 않습니다.

```bash
# 1. 이전 모듈 작업을 내 브랜치에 커밋 (변경이 없으면 nothing to commit 으로 끝남)
git add -A
git commit -m "chore: 이전 모듈 실습 결과"

# 2. 시작 태그에서 실습 브랜치 만들기
git switch -c practice/m4 m4-start
```

| 모듈 | 시작 태그 | 늦어졌을 때 합류 지점 |
|---|---|---|
| 1 AX 시대 개발자 역할 변화: SDLC별 AI 적용 지점 | (저장소 쓰지 않음) | |
| 2 Claude Code 구조와 에이전틱 루프: 실습 환경 점검 | `m2-start` | |
| 3 코드베이스 탐색, CLAUDE.md 구조화, 세션 관리 | `m3-start` | |
| 4 신규 API 구현과 Skill | `m4-start` | `m4-task1-done` (Skill 제작부터), `m4-task2-done` (Skill 적용부터) |
| 5 버그 수정과 강제 장치 | `m5-start` | `m5-fix-done` (위험 SQL 방어부터), `m5-guard-done` (개인 챌린지부터) |
| 6 SQL 리팩토링 | `m6-start` | `m6-lock-done` (리팩토링부터) |
| 7 헤드리스 검증, 커밋 전 검사, 산출물 문서 | `m7-start` | `m7-fdcheck-done` (커밋 전 검증부터) |

늦어졌을 때는 지금까지 한 작업을 커밋하고 합류 지점에서 새 브랜치를 만듭니다. 내 작업은 원래 브랜치에 그대로 남습니다.

```bash
git add -A
git commit -m "chore: 진행 중"
git switch -c practice/m4-task1 m4-task1-done
```

### 자주 막히는 경우

| 증상 | 해결 |
|---|---|
| 브랜치를 만들 때 "would be overwritten" 오류 | 커밋하지 않은 변경이 있습니다. 먼저 커밋합니다 |
| `practice/m4 already exists` | 이어서 하려면 `git switch practice/m4`, 새로 시작하려면 `practice/m4-2` 같은 다른 이름을 씁니다 |
| `DB가 준비되지 않았습니다` | `./gradlew dbReset` 을 실행합니다 |

## 실습 프롬프트

수업 중 입력할 프롬프트와 명령은 `prompts/`에 모듈별로 있습니다. 모듈 시작 태그로 이동하면 그 모듈까지의 파일이 들어 있습니다.

- 파일을 편집기에서 열어 코드 블록을 그대로 복사해 붙여넣습니다. `<꺾쇠>` 부분만 자기 값으로 바꿉니다.
- Claude Code에 `@prompts/...`로 파일을 붙이지 말고, 내용을 복사해 입력합니다.
- Claude Code가 이 폴더를 읽거나 검색하지 않도록 `.claude/settings.json`(읽기 차단)과 `.ignore`(내용 검색 제외)로 막아 두었습니다. 지우지 마세요.

## 자주 쓰는 명령

| 할 일 | 명령 |
|---|---|
| 빌드 (테스트 + 린트) | `./gradlew build` |
| 전체 테스트 / 린트 | `./gradlew test` / `./gradlew checkstyleMain` |
| 실행 | `./gradlew bootRun` (http://localhost:8080) |
| DB 다시 만들기 | `./gradlew dbReset` |
| SQL 실행 (개발 DB) | `node scripts/db.mjs --env dev "SELECT ..."` |
| 매퍼 SQL 렌더링 | `node scripts/render-sql.mjs XxxMapper.queryId params.json` |
