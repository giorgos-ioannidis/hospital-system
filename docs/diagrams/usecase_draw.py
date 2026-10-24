"""Διάγραμμα ΠΧ του 1ου Παραδοτέου με σταθερή διάταξη (SVG)."""
import math

W, H = 1500, 1290
L, M, R = 420, 790, 1160          # στήλες ΠΧ
RX = 150
INK, EDGE = '#000', '#000'
out = []

def esc(s):
    return s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')

uc = {}   # id -> (cx, cy, ry)

def usecase(key, cx, cy, lines, ext=None, rx=None):
    rx = rx or RX
    ry = 22 if len(lines) == 1 else 30
    if ext:
        ry += 14 if len(lines) == 1 else 12
    uc[key] = (cx, cy, ry, rx)
    out.append(f'<ellipse cx="{cx}" cy="{cy}" rx="{rx}" ry="{ry}" fill="none" stroke="#000" stroke-width="1.2"/>')
    n = len(lines) + (2 if ext else 0)
    y0 = cy - (n - 1) * 8 + 4
    for i, t in enumerate(lines):
        out.append(f'<text x="{cx}" y="{y0 + 16 * i}" text-anchor="middle" font-size="13" fill="{INK}">{esc(t)}</text>')
    if ext:
        sy = y0 + 16 * len(lines) - 11
        half = rx * math.sqrt(max(0, 1 - ((sy - cy) / ry) ** 2))
        out.append(f'<line x1="{cx - half}" y1="{sy}" x2="{cx + half}" y2="{sy}" stroke="#000" stroke-width="1"/>')
        out.append(f'<text x="{cx}" y="{y0 + 16 * len(lines) + 2}" text-anchor="middle" font-size="11.5" font-weight="700" fill="#000">extension points</text>')
        out.append(f'<text x="{cx}" y="{y0 + 16 * len(lines) + 16}" text-anchor="middle" font-size="11.5" fill="{INK}">{esc(ext)}</text>')

def edge_point(key, px, py):
    """Σημείο της έλλειψης στη γραμμή προς (px, py)."""
    cx, cy, ry, rx = uc[key]
    dx, dy = px - cx, py - cy
    k = 1 / math.sqrt((dx / rx) ** 2 + (dy / ry) ** 2)
    return cx + dx * k, cy + dy * k

actors = {}

def actor(key, x, y, label):
    actors[key] = (x, y)
    s = f'stroke="{INK}" stroke-width="1.6" fill="none"'
    out.append(f'<circle cx="{x}" cy="{y - 34}" r="10" fill="#7ccff5" stroke="{INK}" stroke-width="1.6"/>')
    out.append(f'<path d="M{x} {y - 24}V{y + 4}M{x - 16} {y - 14}H{x + 16}M{x} {y + 4}L{x - 13} {y + 24}M{x} {y + 4}L{x + 13} {y + 24}" {s}/>')
    for i, t in enumerate(label.split('\n')):
        out.append(f'<text x="{x}" y="{y + 42 + 15 * i}" text-anchor="middle" font-size="13.5" font-weight="600" fill="{INK}">{esc(t)}</text>')

def assoc(a, key):
    ax, ay = actors[a]
    cx, cy, _, rx = uc[key]
    sx = ax + (22 if cx > ax else -22)
    ex, ey = (cx - rx if cx > ax else cx + rx), cy
    out.append(f'<line x1="{sx}" y1="{ay - 6}" x2="{ex:.1f}" y2="{ey:.1f}" stroke="{EDGE}" stroke-width="1.2"/>')

def anchor(key, side):
    cx, cy, ry, rx = uc[key]
    return {'r': (cx + rx, cy), 'l': (cx - rx, cy), 't': (cx, cy - ry), 'b': (cx, cy + ry)}[side]

def dep(src, dst, label, lx=0, ly=0, a=None, b=None):
    sx, sy, _, _ = uc[src]
    tx, ty, _, _ = uc[dst]
    x1, y1 = anchor(src, a) if a else edge_point(src, tx, ty)
    x2, y2 = anchor(dst, b) if b else edge_point(dst, sx, sy)
    out.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{EDGE}" stroke-width="1.2" stroke-dasharray="6 4" marker-end="url(#open)"/>')
    mx, my = (x1 + x2) / 2 + lx, (y1 + y2) / 2 + ly
    out.append(f'<text x="{mx:.0f}" y="{my:.0f}" text-anchor="middle" font-size="12" fill="{EDGE}">{esc(label)}</text>')

def gen(path):
    out.append(f'<path d="{path}" fill="none" stroke="{INK}" stroke-width="1.3" marker-end="url(#tri)"/>')

# Όριο συστήματος
out.append(f'<rect x="330" y="70" width="840" height="1190" fill="#7ccff5" stroke="{INK}" stroke-width="1.2"/>')
out.append(f'<text x="750" y="98" text-anchor="middle" font-size="15" fill="{INK}">Πληροφοριακό Σύστημα Νοσοκομείου</text>')
C = 620
usecase('UC01', C, 140, ['UC-01 Σύνδεση'])
usecase('UC02', C, 215, ['UC-02 Διαχείριση Λογαριασμών'])
usecase('UC11', C, 290, ['UC-11 Διοικητικός Έλεγχος'])
usecase('UC08', C, 365, ['UC-08 Διαχείριση Κλινών'])
usecase('UC03', C, 460, ['UC-03 Διαχείριση Προγράμματος', 'Βαρδιών/Εφημεριών'], ext='Εξαγωγή')
usecase('UC04', C, 560, ['UC-04 Διαχείριση Αδειών'])
usecase('UC05', C, 635, ['UC-05 Διαχείριση Απουσιών'])
usecase('UC09', C, 730, ['UC-09 Διαχείριση', 'Ιατρικού Φακέλου'], ext='Εξαγωγή')
usecase('UC06', C, 830, ['UC-06 Διαχείριση Ωρών Υποδοχής'])
usecase('UC07', C, 915, ['UC-07 Διαχείριση Ραντεβού'], ext='Εξαγωγή')
usecase('UC10', C, 1005, ['UC-10 Διαχείριση Εξετάσεων'])
usecase('UC12', C, 1080, ['UC-12 Πληροφορίες Νοσοκομείου'])
usecase('UC14', C, 1155, ['UC-14 Αποστολή Ειδοποιήσεων'])
RX_SAVE = RX
usecase('UC13', 1040, 700, ['UC-13 Εξαγωγή PDF'], rx=100)

actor('User', 90, 180, 'Χρήστης')
actor('Sup', 90, 420, 'Προϊστάμενος')
actor('Nurse', 90, 650, 'Νοσηλευτής')
actor('Doc', 90, 930, 'Ιατρός')
actor('Pat', 1400, 905, 'Ασθενής')
actor('Pay', 1400, 760, 'Σύστημα\nΠληρωμών')
actor('Time', 1400, 1155, 'Χρόνος')

for k in ['UC01', 'UC02']:
    assoc('User', k)
for k in ['UC02', 'UC11', 'UC08', 'UC03', 'UC04', 'UC05']:
    assoc('Sup', k)
for k in ['UC03', 'UC04', 'UC05', 'UC09']:
    assoc('Nurse', k)
for k in ['UC06', 'UC07', 'UC10']:
    assoc('Doc', k)
for k in ['UC02', 'UC09', 'UC07', 'UC10', 'UC12']:
    assoc('Pat', k)
assoc('Pay', 'UC07')
assoc('Time', 'UC14')

dep('UC13', 'UC03', '<<Extend>>', lx=44, ly=-4, a='t', b='r')
dep('UC13', 'UC09', '<<Extend>>', lx=0, ly=-14, a='l', b='r')
dep('UC13', 'UC07', '<<Extend>>', lx=48, ly=0, a='b', b='r')

gen('M90 374V242')                    # Προϊστάμενος → Χρήστης
gen('M66 656H30V176H62')              # Νοσηλευτής → Χρήστης
gen('M90 884V714')                    # Ιατρός → Νοσηλευτής
gen('M1422 885H1470V40H90V132')            # Ασθενής → Χρήστης
svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" font-family="Helvetica, Arial, sans-serif">
<defs>
<marker id="open" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="9" markerHeight="9" orient="auto"><path d="M0 0L10 5L0 10" fill="none" stroke="{EDGE}" stroke-width="1.4"/></marker>
<marker id="tri" viewBox="0 0 12 12" refX="11" refY="6" markerWidth="14" markerHeight="14" orient="auto"><path d="M0 0L12 6L0 12z" fill="#fff" stroke="{INK}" stroke-width="1.2"/></marker>
</defs>
<rect width="{W}" height="{H}" fill="#fff"/>
{chr(10).join(out)}
</svg>'''
open('usecase.svg', 'w', encoding='utf-8').write(svg)
open('usecase.html', 'w', encoding='utf-8').write(f'<!doctype html><meta charset="utf-8"><style>html,body{{margin:0}}</style>{svg}')
print('ok', len(uc), 'ΠΧ')
