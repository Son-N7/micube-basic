#!/usr/bin/env node
// PostToolUse Hook: 매퍼 XML을 Edit·Write로 저장한 직후 사내 컨벤션 C1~C3을 검사한다.
// 위반이면 종료 코드 2 + stderr 로 알려 Claude가 이어서 고치게 한다. 검사 로직은 scripts/check-conventions.mjs.
import { spawnSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import path from 'node:path';

const input = JSON.parse(readFileSync(0, 'utf8'));
const filePath = String(input.tool_input?.file_path || '');
const normalized = filePath.replace(/\\/g, '/');
if (!/src\/main\/resources\/mapper\/.+\.xml$/.test(normalized)) process.exit(0);

const root = process.env.CLAUDE_PROJECT_DIR || process.cwd();
const result = spawnSync(process.execPath, [path.join(root, 'scripts', 'check-conventions.mjs'), filePath], {
  cwd: root, encoding: 'utf8',
});
if (result.status === 1) {
  console.error(`사내 MyBatis 컨벤션 위반 (docs/conventions/mybatis-convention.md):\n${result.stdout}`
    + '고치는 방법: C1 조회 테이블마다 WITH (NOLOCK), C2 ${} 대신 #{} (정렬은 <choose>), C3 컬럼 나열 + resultMap');
  process.exit(2);
}
