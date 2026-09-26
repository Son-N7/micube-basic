#!/usr/bin/env node
// PreToolUse Hook: Bash·PowerShell로 실행하는 scripts/db.mjs 명령의 SQL을 검사한다.
//   --env prod : SELECT 외의 문장은 막는다
//   --env dev  : DROP, TRUNCATE, KILL, WHERE 없는 UPDATE·DELETE 는 막는다
// 대소문자·주석을 무시하고 판정한다. 막으면 종료 코드 2 + stderr 로 이유와 대안을 알린다.
// 한계: Claude가 Bash·PowerShell 도구로 실행하는 db.mjs 명령만 본다. 사람이 터미널에서 친 명령은 DB 권한이 막는다.
import { existsSync, readFileSync } from 'node:fs';
import path from 'node:path';

const input = JSON.parse(readFileSync(0, 'utf8'));
const command = String(input.tool_input?.command || '');
if (!/scripts[\\/]db\.mjs/.test(command)) process.exit(0);

const args = tokenize(command.slice(command.search(/scripts[\\/]db\.mjs/)));
const env = args[args.indexOf('--env') + 1];
let sql = '';
const fileIdx = args.indexOf('--file');
if (fileIdx >= 0) {
  const file = path.resolve(process.env.CLAUDE_PROJECT_DIR || process.cwd(), args[fileIdx + 1] || '');
  sql = existsSync(file) ? readFileSync(file, 'utf8') : '';
} else {
  sql = args.slice(1).filter((a, i, all) => a !== '--env' && all[i - 1] !== '--env').join(' ');
}

const statements = sql.replace(/\/\*[\s\S]*?\*\//g, ' ').replace(/--[^\n]*/g, ' ')
  .split(';').map((s) => s.trim().toUpperCase().replace(/\s+/g, ' ')).filter(Boolean);
const problems = [];
for (const s of statements) {
  const verb = s.split(' ')[0];
  if (env === 'prod' && verb !== 'SELECT' && verb !== 'WITH') {
    problems.push(`운영 DB에는 조회(SELECT)만 실행할 수 있습니다: ${verb}`);
  }
  if (env === 'dev') {
    if (/\b(DROP|TRUNCATE|KILL)\b/.test(s)) problems.push(`개발 DB에서도 ${s.match(/\b(DROP|TRUNCATE|KILL)\b/)[1]}는 실행하지 않습니다`);
    if (/^(UPDATE|DELETE)\b/.test(s) && !/\bWHERE\b/.test(s)) problems.push(`WHERE 없는 ${verb}는 테이블 전체를 바꿉니다`);
  }
}
if (problems.length) {
  console.error(`SQL 실행을 막았습니다 (sql-guard).\n- ${problems.join('\n- ')}\n`
    + '대안: 조회 SQL로 대상을 확인해 보고하고, 데이터 변경은 승인된 스크립트로 사람이 실행합니다.');
  process.exit(2);
}

// 셸 명령을 인자로 나눈다 (작은따옴표·큰따옴표 지원)
function tokenize(text) {
  const out = [];
  let cur = '';
  let quote = null;
  let has = false;
  for (const ch of text) {
    if (quote) {
      if (ch === quote) quote = null; else cur += ch;
    } else if (ch === '"' || ch === "'") {
      quote = ch; has = true;
    } else if (/\s/.test(ch)) {
      if (cur || has) out.push(cur);
      cur = ''; has = false;
    } else if (ch === '&' || ch === '|' || ch === ';' && !cur) {
      break;
    } else {
      cur += ch;
    }
  }
  if (cur || has) out.push(cur);
  return out;
}
