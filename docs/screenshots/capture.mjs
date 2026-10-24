// Screenshots ανά οθόνη UI-xx για το 2ο Παραδοτέο.
//
// Χρήση (από τη ρίζα του project, με ΦΡΕΣΚΙΑ εφαρμογή στο dev: ./mvnw spring-boot:run):
//     node docs/screenshots/capture.mjs
// Απαιτεί Node >= 22 (ενσωματωμένο WebSocket) και Google Chrome. Άλλη διαδρομή Chrome: CHROME_PATH=... node ...
//
// Το script παίζει τις ροές σαν χρήστης (πρόγραμμα, άδεια, κράτηση, πληρωμή), ώστε κάθε οθόνη να έχει δεδομένα.
// Γράφει docs/screenshots/<ui>.png (1440 px) και docs/screenshots/mobile/<ui>.png (390 px).

import { spawn } from 'node:child_process';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const BASE = process.env.BASE_URL || 'http://localhost:8080';
const PASSWORD = 'demo1234';
const OUT = dirname(fileURLToPath(import.meta.url));
const CHROME = process.env.CHROME_PATH || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const DESKTOP = { width: 1440, height: 900, scale: 1, mobile: false };
const PHONE = { width: 390, height: 844, scale: 2, mobile: true };

// ---------- Ημερομηνίες που ταιριάζουν με τα demo δεδομένα ----------
const pad = n => String(n).padStart(2, '0');
const iso = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const addDays = (d, n) => new Date(d.getFullYear(), d.getMonth(), d.getDate() + n);
const today = new Date();
const thisMonth = iso(today).slice(0, 7);
/** Επόμενη Δευτέρα/Τετάρτη/Παρασκευή: ο doctor1 δέχεται 09:00–13:00 αυτές τις μέρες. */
function nextOfficeDay() {
  let d = addDays(today, 2);
  while (![1, 3, 5].includes(d.getDay())) d = addDays(d, 1);
  return iso(d);
}
const BOOKING_DAY = nextOfficeDay();

// ---------- Chrome DevTools Protocol ----------
const port = 9300 + Math.floor(Math.random() * 600);
const chrome = spawn(CHROME, ['--headless=new', `--remote-debugging-port=${port}`, '--hide-scrollbars', '--no-first-run',
  '--lang=el-GR', `--user-data-dir=${mkdtempSync(join(tmpdir(), 'cdp-'))}`, 'about:blank'], { stdio: 'ignore' });
const sleep = ms => new Promise(r => setTimeout(r, ms));
let target;
for (let i = 0; i < 60 && !target; i++) {
  try { target = (await (await fetch(`http://127.0.0.1:${port}/json/list`)).json()).find(t => t.type === 'page'); }
  catch { await sleep(200); }
}
if (!target) throw new Error('Δεν ξεκίνησε το Chrome: ' + CHROME);
const ws = new WebSocket(target.webSocketDebuggerUrl);
await new Promise(r => ws.addEventListener('open', r));
let seq = 0;
const pending = new Map();
const onLoad = [];
ws.addEventListener('message', e => {
  const m = JSON.parse(e.data);
  if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id); }
  if (m.method === 'Page.loadEventFired') onLoad.splice(0).forEach(f => f());
});
async function send(method, params = {}) {
  const m = await new Promise(r => { const id = ++seq; pending.set(id, r); ws.send(JSON.stringify({ id, method, params })); });
  if (m.error) throw new Error(`${method}: ${m.error.message}`);
  return m.result;
}
async function evaluate(expression) {
  const r = await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
  if (r.exceptionDetails) throw new Error(r.exceptionDetails.exception?.description || r.exceptionDetails.text);
  return r.result.value;
}
const nextLoad = () => new Promise(r => onLoad.push(r));
async function settle() {
  await evaluate('document.fonts.ready.then(() => true)');
  await sleep(250);
}
async function viewport(v, height = v.height) {
  await send('Emulation.setDeviceMetricsOverride', { width: v.width, height, deviceScaleFactor: v.scale, mobile: v.mobile });
}

// ---------- Ενέργειες χρήστη ----------
async function go(path) {
  const loaded = nextLoad();
  await send('Page.navigate', { url: BASE + path });
  await loaded;
  await settle();
}

/** Συμπληρώνει τα πεδία της φόρμας (με name) και την υποβάλλει, όπως ο χρήστης. */
async function submit(formSelector, values = {}) {
  const loaded = nextLoad();
  await evaluate(`(() => {
    const form = document.querySelector(${JSON.stringify(formSelector)});
    if (!form) throw new Error('Δεν βρέθηκε φόρμα ${formSelector.replace(/'/g, '')} στο ' + location.pathname);
    for (const [name, value] of Object.entries(${JSON.stringify(values)})) {
      const field = form.elements[name];
      if (!field) throw new Error('Δεν βρέθηκε πεδίο ' + name);
      if (field.type === 'checkbox') field.checked = Boolean(value); else field.value = value;
    }
    HTMLFormElement.prototype.submit.call(form);
  })()`);
  await loaded;
  await settle();
}

/** Πατά ένα κουμπί ή σύνδεσμο (όπως ο χρήστης) και περιμένει τη νέα σελίδα. */
async function click(selector, force = false) {
  const loaded = nextLoad();
  await evaluate(`(() => {
    const el = document.querySelector(${JSON.stringify(selector)});
    if (!el) throw new Error('Δεν βρέθηκε ' + ${JSON.stringify(selector)} + ' στο ' + location.pathname);
    if (${force}) el.disabled = false;
    el.click();
  })()`);
  await loaded;
  await settle();
}

async function login(username) {
  await send('Network.clearBrowserCookies');
  await go('/login');
  await submit('form[action$="/login"]', { username, password: PASSWORD });
  const path = await evaluate('location.pathname');
  if (path.startsWith('/login')) throw new Error('Αποτυχία σύνδεσης για ' + username);
}

const saved = [];
/** Φωτογραφίζει όλη τη σελίδα (έως 3200 px ύψος). */
async function shot(name, v = DESKTOP) {
  await viewport(v);
  await settle();
  const { cssContentSize } = await send('Page.getLayoutMetrics');
  await viewport(v, Math.min(Math.ceil(cssContentSize.height), 3200));
  await sleep(150);
  const { data } = await send('Page.captureScreenshot', { format: 'png' });
  const dir = v.mobile ? join(OUT, 'mobile') : OUT;
  mkdirSync(dir, { recursive: true });
  writeFileSync(join(dir, `${name}.png`), Buffer.from(data, 'base64'));
  saved.push(`${v.mobile ? 'mobile/' : ''}${name}.png`);
  console.log('✓', saved[saved.length - 1]);
  await viewport(v);
}
async function both(name) {
  await shot(name);
  await shot(name, PHONE);
}

// ---------- Σενάριο ----------
await send('Page.enable');
await send('Network.enable');
await viewport(DESKTOP);

// Δημόσιες οθόνες
await go('/login');
await both('ui-01a-login');
await go('/register');
await shot('ui-02-register');
await go('/privacy');
await shot('privacy');

// Προϊστάμενος: πρόγραμμα του μήνα (UC-05), έγκριση, διοίκηση
await login('supervisor');
await go('/schedules');
await submit('form[action$="/schedules"]', { departmentId: 1, month: thisMonth, maxWeeklyHours: 56 });
await shot('ui-05b-calendar-draft');
await submit('form[action$="/approve"]');
await shot('ui-05b-calendar');
await go('/schedules');
await shot('ui-05a-params');
await go('/');
await both('ui-01b-home-supervisor');
await go('/staff');
await shot('ui-03a-staff');
await go('/staff/10');
await shot('ui-03b-staff-edit');
await go('/absences/replacements');
await shot('ui-10-replacements');
await go(`/overtime?departmentId=1&month=${thisMonth}`);
await shot('ui-21-overtime');
await go('/beds?amka=01011980002&departmentId=1&severity=3');
await shot('ui-24-assign');
await go('/beds/occupancy');
await shot('ui-25-occupancy');
await go('/info/map?room=1');
await shot('ui-22-map');
await go('/info/directory');
await shot('ui-23-directory');

// Νοσηλευτής του Παθολογικού (3 νοσηλευτές για 3 βάρδιες): η άδεια θα δείξει προειδοποίηση στελέχωσης (UC-06/07)
await login('nurse2');
await go('/leave');
await submit('form[action$="/leave"]', { type: 'ANNUAL', startDate: iso(addDays(today, 3)), endDate: iso(addDays(today, 4)),
  reason: 'Οικογενειακή υποχρέωση' });
await shot('ui-06-request');

// Νοσηλευτής του Καρδιολογικού (με ενεργό πρόγραμμα): δήλωση απουσίας (UC-09), πρόγραμμα (UC-08)
await login('nurse1');
await go('/absences');
await shot('ui-08-declare');
await go('/schedules/mine');
await both('ui-09-my-schedule');
await go('/');
await shot('ui-01b-home-nurse');

// Προϊστάμενος του Παθολογικού: η αίτηση με την προειδοποίηση στελέχωσης (UC-07)
await login('supervisor2');
await go('/leave/pending');
await shot('ui-07-pending');

// Ασθενής: κράτηση (UC-14 → UC-13 → UC-16), τα ραντεβού μου (UC-15)
await login('patient2');
await go('/appointments/new?departmentId=1');
await shot('ui-11-book-step2');
const grid = '/appointments/new?departmentId=1&serviceId=2&doctorId=2';
const slot = `button[name="slot"][value="${BOOKING_DAY}T10:30"]`;
await go(grid);
await both('ui-11-book');
await evaluate(`document.getElementById('reason').value = 'Αίσθημα παλμών'`);
await click(slot);
await both('ui-12-preview');
await submit('form[action$="/appointments/payment"]');
await shot('ui-16-payment');
await submit('form[action$="/appointments"]', { paymentMethod: 'CARD' });
await both('ui-14-mine');
await click('a[href*="/reschedule"]');
await both('ui-14b-reschedule');
// Ροή 7a1: η ίδια ώρα (πλέον κλεισμένη) στέλνεται ξανά → μήνυμα και 3 εναλλακτικές
await go(grid);
await click(slot, true);
await shot('ui-11-book-alternatives');
await go('/');
await both('ui-01b-home-patient');
await go('/notifications');
await shot('ui-17-notifications');
await go('/profile');
await shot('ui-04-profile');

// Ασθενής που νοσηλεύεται: φάκελος, πρόσβαση, αποτελέσματα (UC-19, UC-21)
await login('patient3');
await go('/records/mine');
await both('ui-18b-record-patient');
await go('/records/mine/access-log');
await shot('ui-18c-access-log');
await go('/exams/mine');
await both('ui-20-results');

// Ιατρός: ώρες υποδοχής (UC-11), ραντεβού (UC-15), φάκελοι (UC-19), εξετάσεις (UC-20)
await login('doctor1');
await go('/');
await both('ui-01b-home-doctor');
await go('/office-hours');
await shot('ui-13-office-hours');
await go('/appointments/doctor');
await shot('ui-15-doctor');
await go('/records');
await shot('ui-18a-search');
await go('/records/22');
await shot('ui-18b-record');
await go('/exams');
await shot('ui-19-doctor-exams');

// Άρνηση πρόσβασης σε ξένο φάκελο (NFR-02, TC-5)
await login('patient1');
await go('/records/21');
await shot('error-403-record');

ws.close();
chrome.kill();
writeFileSync(join(OUT, 'index.txt'), saved.join('\n') + '\n');
console.log(`\n${saved.length} εικόνες στο ${OUT}`);
