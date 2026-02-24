/* ────────────────────────────────────────────────
   배달소대 와이어프레임 — app.js
   ──────────────────────────────────────────────── */

const tabs    = document.querySelectorAll('.tab');
const screens = document.querySelectorAll('.screen');

function switchScreen(id) {
  // 탭 업데이트
  tabs.forEach(t => t.classList.toggle('active', t.dataset.screen === id));
  // 화면 전환
  screens.forEach(s => s.classList.toggle('active', s.id === `screen-${id}`));
  // 스크롤 최상단
  document.querySelector('.canvas').scrollTop = 0;
}

// 상단 탭 클릭
tabs.forEach(tab => {
  tab.addEventListener('click', () => switchScreen(tab.dataset.screen));
});

// 화면 내부 내비게이션 (data-nav 속성)
document.addEventListener('click', e => {
  const el = e.target.closest('[data-nav]');
  if (el) switchScreen(el.dataset.nav);
});

// 메뉴 탭 (음식점 상세 내부)
document.querySelectorAll('.menu-tab').forEach((btn, i, arr) => {
  btn.addEventListener('click', () => {
    arr.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

// 배달 토글 (장바구니 내부)
document.querySelectorAll('.dtog').forEach((btn, i, arr) => {
  btn.addEventListener('click', () => {
    arr.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

// 결제 수단 선택
document.querySelectorAll('.pay-opt').forEach((opt, i, arr) => {
  opt.addEventListener('click', () => {
    arr.forEach(o => o.classList.remove('selected-pay'));
    opt.classList.add('selected-pay');
  });
});

// 옵션 라디오
document.querySelectorAll('.option-row:not(.checkbox-row)').forEach((row) => {
  row.addEventListener('click', () => {
    const group = row.closest('.option-group');
    group.querySelectorAll('.option-row:not(.checkbox-row)')
         .forEach(r => r.classList.remove('selected-option'));
    row.classList.add('selected-option');
  });
});
