#!/usr/bin/env node
// 사내 MyBatis 컨벤션 기계적 검사 (C1~C3). AI를 부르지 않는다.
//
//   node scripts/check-conventions.mjs                 src/main/resources/mapper 아래 XML 전부
//   node scripts/check-conventions.mjs a.xml b.xml     지정한 파일만 (XML이 아닌 파일은 건너뜀)
//   node scripts/check-conventions.mjs --json          {"violations":[{file,line,rule,message}]}
//
// C1 조회(<select>)하는 모든 테이블에 WITH (NOLOCK)   C2 ${} 금지   C3 SELECT * 금지 (docs/conventions/mybatis-convention.md)
// 종료 코드: 위반 없음 0, 위반 있음 1
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import path from 'node:path';
import { ROOT } from './lib/run.mjs';

const args = process.argv.slice(2);
const json = args.includes('--json');
const inputs = args.filter((a) => a !== '--json');
const files = inputs.length ? inputs.filter((f) => f.endsWith('.xml')) : listXml(path.join(ROOT, 'src/main/resources/mapper'));

const violations = [];
for (const file of files) {
  const abs = path.resolve(process.cwd(), file);
  if (!existsSync(abs)) continue; // 삭제된 파일
  const rel = path.relative(ROOT, abs);
  const shown = (rel.startsWith('..') ? abs : rel).split(path.sep).join('/');
  check(readFileSync(abs, 'utf8'), shown);
}

violations.sort((a, b) => a.file.localeCompare(b.file) || a.line - b.line);
if (json) {
  console.log(JSON.stringify({ violations }, null, 2));
} else {
  for (const v of violations) console.log(`${v.file}:${v.line} [${v.rule}] ${v.message}`);
  console.log(violations.length ? `위반 ${violations.length}건` : `위반 없음 (${files.length}개 파일)`);
}
process.exit(violations.length ? 1 : 0);

function check(text, file) {
  // 주석은 같은 길이의 공백으로 지워 줄 번호를 유지한다
  const clean = text.replace(/<!--[\s\S]*?-->/g, blank);
  const lineOf = (offset) => clean.slice(0, offset).split('\n').length;

  for (const m of clean.matchAll(/\$\{/g)) {
    violations.push({ file, line: lineOf(m.index), rule: 'C2', message: '${} 사용 금지. #{} 로 바인딩하고, 정렬 컬럼은 <choose>로 허용 목록만' });
  }
  for (const block of clean.matchAll(/<select\b[\s\S]*?<\/select>/g)) {
    // 태그를 공백으로 바꿔 SQL 글자만 남긴다 (<if>, <include> 등)
    const sql = block[0].replace(/<[^>]*>/g, blank);
    for (const m of sql.matchAll(/\bSELECT\s+(?:DISTINCT\s+)?(?:TOP\s*\(?\s*\d+\s*\)?\s+)?(?:\w+\.)?\*/gi)) {
      violations.push({ file, line: lineOf(block.index + m.index), rule: 'C3', message: 'SELECT * 금지. 컬럼을 나열하고 resultMap으로 매핑' });
    }
    const tableRef = /\b(FROM|JOIN)\s+([A-Za-z_][\w.]*)(?:\s+(?:AS\s+)?(?!WITH\b|WHERE\b|ON\b|JOIN\b|INNER\b|LEFT\b|RIGHT\b|CROSS\b|ORDER\b|GROUP\b|UNION\b)([A-Za-z_]\w*))?(\s*WITH\s*\(\s*NOLOCK\s*\))?/gi;
    for (const m of sql.matchAll(tableRef)) {
      if (!m[4]) {
        violations.push({ file, line: lineOf(block.index + m.index), rule: 'C1', message: `${m[2]} 조회에 WITH (NOLOCK) 없음` });
      }
    }
  }
}

function blank(s) {
  return s.replace(/[^\n]/g, ' ');
}

function listXml(dir) {
  if (!existsSync(dir)) return [];
  const result = [];
  for (const name of readdirSync(dir).sort()) {
    const p = path.join(dir, name);
    if (statSync(p).isDirectory()) result.push(...listXml(p));
    else if (name.endsWith('.xml')) result.push(p);
  }
  return result;
}
