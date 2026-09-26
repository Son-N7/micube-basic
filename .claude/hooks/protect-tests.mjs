#!/usr/bin/env node
// PreToolUse Hook: .claude/protected-tests.txt 에 적힌 테스트 파일을 Edit·Write로 고치지 못하게 막는다.
//   - 목록 형식: 저장소 루트 기준 경로를 한 줄에 하나. 빈 줄과 # 주석은 무시한다.
//   - 목록에 없는 파일, 새로 만드는 파일은 막지 않는다.
// 한계: 편집 도구(Edit·Write)만 본다. Bash 명령(sed, mv 등)이나 사람이 편집기로 바꾼 파일은 막지 못하므로
//       작업 뒤 `git diff -- src/test`로 확인한다.
import { existsSync, readFileSync } from 'node:fs';
import path from 'node:path';

const input = JSON.parse(readFileSync(0, 'utf8'));
const filePath = String(input.tool_input?.file_path || '');
if (!filePath) process.exit(0);

const root = process.env.CLAUDE_PROJECT_DIR || input.cwd || process.cwd();
const listFile = path.join(root, '.claude', 'protected-tests.txt');
if (!existsSync(listFile)) process.exit(0);

const norm = (p) => p.replace(/\\/g, '/').replace(/^\.\//, '');
const rootN = norm(root).replace(/\/$/, '');
let target = norm(filePath);
if (target.toLowerCase().startsWith(rootN.toLowerCase() + '/')) target = target.slice(rootN.length + 1);

const protectedFiles = readFileSync(listFile, 'utf8').split(/\r?\n/)
  .map((line) => norm(line.replace(/#.*$/, '').trim()))
  .filter(Boolean);

const hit = protectedFiles.find((entry) => target === entry || target.endsWith('/' + entry));
if (hit) {
  console.error(`보호된 테스트 파일입니다: ${hit} (.claude/protected-tests.txt)\n`
    + '이 테스트는 사람이 검토하고 고정한 기준입니다. 테스트가 아니라 제품 코드를 고치세요.\n'
    + '테스트 자체가 잘못되었다고 판단되면 고치지 말고 그 이유를 사용자에게 설명하세요.');
  process.exit(2);
}
