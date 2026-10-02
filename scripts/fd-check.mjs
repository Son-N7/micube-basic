#!/usr/bin/env node
// 기능정의서와 소스를 claude -p 로 대조한다 (사람이 묻지 않아도 돌아가는 검증).
//
//   node scripts/fd-check.mjs docs/design/SHOP-FD-012-sales-hold.md
//
// 결과: reports/fd-<정의서 이름>.json, 불일치 개수를 출력. 불일치가 있으면 종료 코드 1.
import { mkdirSync, writeFileSync, existsSync } from 'node:fs';
import path from 'node:path';
import { ROOT, run } from './lib/run.mjs';

const spec = process.argv[2];
if (!spec || !existsSync(path.resolve(ROOT, spec))) {
  console.error('사용법: node scripts/fd-check.mjs <기능정의서 경로>');
  process.exit(2);
}

// 결과 모양을 고정한다 → 다른 스크립트(precommit-check)가 기계적으로 읽을 수 있다
const schema = {
  type: 'object',
  properties: {
    issues: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          item: { type: 'string' },
          spec: { type: 'string' },
          source: { type: 'string' },
          location: { type: 'string' },
          verdict: { type: 'string', enum: ['불일치', '확인 필요'] },
        },
        required: ['item', 'spec', 'source', 'location', 'verdict'],
      },
    },
  },
  required: ['issues'],
};

const prompt = `${spec} 기능정의서와 실제 소스(Controller, Request, Service, 매퍼 XML, ErrorCode, 테스트)를 대조해.
정의서의 항목(요청·응답, 업무프로세스, 오류 코드, SQL, 단위테스트)마다 소스를 확인하고, 불일치와 확인 필요 항목만 issues 로 돌려줘.
location 은 근거 소스 파일:줄. 어느 쪽을 고칠지는 정하지 마. 파일은 수정하지 마.`;

const args = [
  '-p', prompt,
  '--output-format', 'json',               // 결과를 JSON 한 덩어리로 (structured_output 포함)
  '--json-schema', JSON.stringify(schema), // 결과 모양 고정
  '--permission-mode', 'dontAsk',          // 사람이 없는 실행: 허용 목록 밖의 도구는 묻지 않고 거부
  '--allowedTools', 'Read,Grep,Glob',      // 읽기 도구만 허용 (수정·명령 실행 불가)
  '--max-turns', '30',                     // 시간·비용 상한
];
const result = run('claude', args);
if (result.status !== 0) {
  console.error(`claude 실행 실패 (종료 코드 ${result.status})\n${result.stderr.slice(0, 2000)}`);
  process.exit(2);
}

let output;
try {
  output = JSON.parse(result.stdout).structured_output;
} catch (e) {
  console.error(`결과 JSON을 읽지 못했습니다: ${e.message}`);
  process.exit(2);
}
if (!output || !Array.isArray(output.issues)) {
  console.error('구조화된 결과(structured_output)가 없습니다.');
  process.exit(2);
}

mkdirSync(path.join(ROOT, 'reports'), { recursive: true });
const name = path.basename(spec, '.md');
const reportPath = path.join(ROOT, 'reports', `fd-${name}.json`);
writeFileSync(reportPath, JSON.stringify(output, null, 2) + '\n');

const mismatches = output.issues.filter((i) => i.verdict === '불일치');
for (const i of output.issues) console.log(`[${i.verdict}] ${i.item}: 정의서 ${i.spec} / 소스 ${i.source} (${i.location})`);
console.log(`불일치 ${mismatches.length}건, 확인 필요 ${output.issues.length - mismatches.length}건 → ${path.relative(ROOT, reportPath)}`);
process.exit(mismatches.length ? 1 : 0);
