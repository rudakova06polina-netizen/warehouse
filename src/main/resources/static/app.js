const API = '/api/equipment';
let editingId = null;
let current = [];

const $ = id => document.getElementById(id);

async function load() {
  const q = $('search').value.trim();
  const r = await fetch(API + (q ? '?q=' + encodeURIComponent(q) : ''));
  if (r.redirected || !r.ok) { location.href = '/login.html'; return; }
  current = await r.json();
  render();
  loadHistogram();
}

function render() {
  const tbody = $('rows');
  tbody.innerHTML = '';
  for (const e of current) {
    const tr = document.createElement('tr');
    const cells = [e.id, e['вид_техники'], e.techGroup, e.dateIn, e.dateOut || '—', e.driverName];
    for (const c of cells) {
      const td = document.createElement('td');
      td.textContent = c;
      tr.appendChild(td);
    }
    const act = document.createElement('td');
    const b1 = document.createElement('button');
    b1.textContent = 'Изменить';
    b1.onclick = () => edit(e);
    const b2 = document.createElement('button');
    b2.textContent = 'Удалить';
    b2.className = 'danger';
    b2.onclick = () => remove(e.id);
    act.append(b1, b2);
    tr.appendChild(act);
    tbody.appendChild(tr);
  }
  $('count').textContent = current.length;
}

function edit(e) {
  editingId = e.id;
  $('formTitle').textContent = 'Редактирование записи #' + e.id;
  $('kind').value = e['вид_техники'];
  $('group').value = e.techGroup;
  $('dateIn').value = e.dateIn;
  $('dateOut').value = e.dateOut || '';
  $('driver').value = e.driverName;
  window.scrollTo(0, 0);
}

function resetForm() {
  editingId = null;
  $('formTitle').textContent = 'Новая запись';
  for (const id of ['kind', 'group', 'dateIn', 'dateOut', 'driver']) $(id).value = '';
  $('err').textContent = '';
}

async function save() {
  const body = {
    'вид_техники': $('kind').value.trim(),
    techGroup: $('group').value.trim(),
    dateIn: $('dateIn').value || null,
    dateOut: $('dateOut').value || null,
    driverName: $('driver').value.trim()
  };
  const r = await fetch(editingId ? API + '/' + editingId : API, {
    method: editingId ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (!r.ok) {
    let m = 'Ошибка сохранения';
    try { const j = await r.json(); m = j.message || j.error || m; } catch (x) {}
    $('err').textContent = m;
    return;
  }
  resetForm();
  load();
}

async function remove(id) {
  if (!confirm('Удалить запись #' + id + '?')) return;
  await fetch(API + '/' + id, { method: 'DELETE' });
  load();
}

async function loadHistogram() {
  const r = await fetch(API + '/histogram');
  if (!r.ok) return;
  drawHistogram(await r.json());
}

function drawHistogram(data) {
  const canvas = $('hist');
  const ctx = canvas.getContext('2d');
  const W = canvas.width, H = canvas.height;
  const padL = 40, padB = 40, padT = 20, padR = 10;
  ctx.clearRect(0, 0, W, H);
  const max = Math.max(1, ...data.map(d => d.count));
  const plotW = W - padL - padR, plotH = H - padT - padB;
  const barW = plotW / data.length;

  ctx.strokeStyle = '#999'; ctx.fillStyle = '#222'; ctx.font = '12px Arial';
  ctx.beginPath(); ctx.moveTo(padL, padT); ctx.lineTo(padL, H - padB); ctx.lineTo(W - padR, H - padB); ctx.stroke();

  for (let i = 0; i <= max; i++) {
    const y = H - padB - (plotH * i) / max;
    ctx.fillText(i, 12, y + 4);
    ctx.strokeStyle = '#eee';
    ctx.beginPath(); ctx.moveTo(padL, y); ctx.lineTo(W - padR, y); ctx.stroke();
  }
  data.forEach((d, i) => {
    const h = (plotH * d.count) / max;
    const x = padL + i * barW;
    ctx.fillStyle = '#2c3e50';
    ctx.fillRect(x + 2, H - padB - h, barW - 4, h);
    if (i % 3 === 0) {
      ctx.fillStyle = '#222';
      ctx.save();
      ctx.translate(x + barW / 2, H - padB + 12);
      ctx.rotate(Math.PI / 4);
      ctx.fillText(d.date.slice(8, 10) + '.' + d.date.slice(5, 7), 0, 0);
      ctx.restore();
    }
  });
}

load();
