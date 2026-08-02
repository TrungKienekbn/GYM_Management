<!--
============================================================
FILE: src/components/pet/PetSprite.vue
Pixel-art pet dạng "bé trai anh hùng" phong cách game 2D (Goku style).
- Canvas 32x48, có shading (bóng đổ) + highlight cho khối 3D.
- Cơ bụng (abs) cho các stage gầy/săn chắc, bụng phệ cho OVERWEIGHT.
- Walk animation mượt: chân sải bước + tay đánh nhịp theo legFrame.

CẬP NHẬT COSMETIC: màu áo/quần/tóc giờ nhận qua props (shirtColor,
pantsColor, hairColor) thay vì hardcode, để phối trang phục tự do.
Không đổi model/sprite/animation, chỉ đổi nguồn màu.

API: props { stage, legFrame, shirtColor, pantsColor, hairColor }
============================================================
-->
<template>
  <svg width="96" height="144" viewBox="0 0 32 48" shape-rendering="crispEdges">
    <!-- vẽ từ sau ra trước: chân -> quần -> thân -> tay -> đầu -> tóc -->
    <g v-html="legsHtml"></g>
    <g v-html="shortsHtml"></g>
    <g v-html="torsoHtml"></g>
    <g v-html="armsHtml"></g>
    <g v-html="headHtml"></g>
    <g v-html="hairHtml"></g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  stage: { type: String, required: true },
  legFrame: { type: String, default: 'a' },
  shirtColor: { type: String, default: '#E8641E' }, // cam (free)
  pantsColor: { type: String, default: '#B84A12' }, // cam (free)
  hairColor:  { type: String, default: '#F5C400' }  // vàng (free)
})

/* ---------- helper: tính màu bóng/sáng từ 1 màu base ---------- */
function shade(hex, percent) {
  const clean = hex.replace('#', '')
  const num = parseInt(clean, 16)
  let r = (num >> 16) + percent
  let g = ((num >> 8) & 0xFF) + percent
  let b = (num & 0xFF) + percent
  r = Math.max(0, Math.min(255, r))
  g = Math.max(0, Math.min(255, g))
  b = Math.max(0, Math.min(255, b))
  return '#' + ((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')
}

/* ---------- BẢNG MÀU CỐ ĐỊNH (không thuộc cosmetic) ---------- */
const skin = '#F0C9A0'; const skinD = '#D2A276'; const skinH = '#FBE3C1'
const shoe = '#2C2C2A'; const shoeH = '#4A4A46'
const eyeWhite = '#F7F2E8'; const pupil = '#2B2B29'; const mouth = '#B4553C'; const blush = '#E8A98A'
const belt = '#20489B'; const beltL = '#3A62BE'

/* ---------- BẢNG MÀU COSMETIC (động theo props) ---------- */
const shirt  = computed(() => props.shirtColor)
const shirtD = computed(() => shade(props.shirtColor, -40))
const shirtL = computed(() => shade(props.shirtColor, 40))

const shorts  = computed(() => props.pantsColor)
const shortsD = computed(() => shade(props.pantsColor, -40))
const shortsL = computed(() => shade(props.pantsColor, 40))

const hair  = computed(() => props.hairColor)
const hairD = computed(() => shade(props.hairColor, -35))
const hairH = computed(() => shade(props.hairColor, 45))

/* helper tạo 1 pixel-rect */
const r = (x, y, w, h, f) => `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="${f}"/>`

/* ---------- CẤU HÌNH THEO THỂ TRẠNG ----------
   shoulderW/waistW: bề ngang vai/eo (px)
   armW/legW: độ dày tay/chân
   abs: hiện cơ bụng ; belly: hiện bụng phệ
   (màu áo đã tách ra khỏi config này, xem cosmetic computed ở trên)
*/
const STAGE_CONFIG = {
  SLIM:       { shoulderW: 9,  waistW: 7,  armW: 2, legW: 3, abs: true,  belly: false },
  LEAN:       { shoulderW: 11, waistW: 8,  armW: 2, legW: 3, abs: true,  belly: false },
  FIT:        { shoulderW: 13, waistW: 9,  armW: 3, legW: 4, abs: true,  belly: false },
  AVERAGE:    { shoulderW: 12, waistW: 12, armW: 3, legW: 4, abs: false, belly: false },
  OVERWEIGHT: { shoulderW: 13, waistW: 16, armW: 4, legW: 4, abs: false, belly: true  }
}
const cfg = computed(() => STAGE_CONFIG[props.stage] || STAGE_CONFIG.AVERAGE)

/* ---------- HÌNH HỌC THÂN ----------
   Thân là hình thang: rộng ở vai, thu ở eo. OVERWEIGHT phình ở bụng dưới.
*/
const CENTER = 16
const TORSO_TOP = 16
const TORSO_H = 14 // các hàng y: 16..29

function torsoWidthAt(row, c) {
  const t = row / (TORSO_H - 1) // 0=vai, 1=eo
  let w = Math.round(c.shoulderW + (c.waistW - c.shoulderW) * t)
  if (c.belly) {
    const bulge = Math.sin(t * Math.PI) // phình giữa
    w += Math.round(bulge * 2)
    if (t > 0.5) w += 2 // bụng dưới xệ ra
  }
  return w
}

/* ---------- ĐẦU + MẶT (cố định, không đổi theo stage) ---------- */
const headHtml = computed(() => {
  let s = ''
  s += r(14, 15, 4, 2, skin)
  s += r(14, 16, 4, 1, skinD)
  s += r(11, 6, 10, 9, skin)
  s += r(20, 7, 1, 7, skinD)
  s += r(12, 14, 8, 1, skinD)
  s += r(10, 10, 1, 3, skin) + r(21, 10, 1, 3, skin)
  s += r(10, 11, 1, 2, skinD) + r(21, 11, 1, 2, skinD)
  s += r(12, 9, 3, 1, hairD.value) + r(17, 9, 3, 1, hairD.value)
  s += r(12, 10, 3, 2, eyeWhite) + r(17, 10, 3, 2, eyeWhite)
  s += r(13, 10, 1, 2, pupil) + r(18, 10, 1, 2, pupil)
  s += r(14, 10, 1, 1, '#FFFFFF') + r(19, 10, 1, 1, '#FFFFFF')
  s += r(12, 12, 1, 1, blush) + r(19, 12, 1, 1, blush)
  s += r(16, 12, 1, 1, skinD)
  s += r(15, 13, 2, 1, mouth)
  return s
})

/* ---------- TÓC (spiky, màu theo cosmetic) ---------- */
const hairHtml = computed(() => {
  let s = ''
  s += r(11, 5, 10, 1, hair.value)
  s += r(12, 4, 8, 1, hair.value)
  s += r(12, 3, 1, 1, hair.value) + r(15, 3, 1, 1, hair.value) + r(18, 3, 1, 1, hair.value)
  s += r(15, 2, 1, 1, hair.value)
  s += r(11, 6, 2, 1, hair.value) + r(19, 6, 2, 1, hair.value)
  s += r(14, 6, 1, 1, hair.value) + r(16, 6, 1, 1, hair.value)
  s += r(10, 7, 1, 2, hair.value) + r(21, 7, 1, 2, hair.value)
  s += r(12, 5, 4, 1, hairH.value)
  s += r(11, 6, 1, 1, hairH.value)
  s += r(19, 5, 1, 1, hairD.value)
  return s
})

/* ---------- THÂN + ÁO + CƠ BỤNG / BỤNG PHỆ + ĐAI XANH ---------- */
const torsoHtml = computed(() => {
  const c = cfg.value
  let s = ''
  for (let i = 0; i < TORSO_H; i++) {
    const y = TORSO_TOP + i
    const w = torsoWidthAt(i, c)
    const x = CENTER - Math.floor(w / 2)
    s += r(x, y, w, 1, shirt.value)
    s += r(x, y, 1, 1, shirtL.value)          // highlight cạnh trái
    s += r(x + w - 1, y, 1, 1, shirtD.value)  // bóng cạnh phải
  }
  // cổ áo
  s += r(13, TORSO_TOP, 6, 1, shirtD.value)
  s += r(14, TORSO_TOP, 4, 1, shirtL.value)

  if (c.abs) {
    s += r(12, 18, 3, 2, shirtL.value) + r(18, 18, 3, 2, shirtL.value)
    for (let ly = 20; ly <= 27; ly++) s += r(CENTER, ly, 1, 1, shirtD.value)
    ;[21, 24, 27].forEach(ay => { s += r(13, ay, 6, 1, shirtD.value) })
  }

  if (c.belly) {
    s += r(13, 22, 7, 1, shirtL.value)
    s += r(CENTER, 25, 1, 2, shirtD.value)
    s += r(11, 27, 11, 1, shirtD.value)
  }

  // Đai lưng xanh Goku ở đáy thân (giữ cố định, không thuộc cosmetic)
  const waistW = torsoWidthAt(TORSO_H - 1, c)
  const waistX = CENTER - Math.floor(waistW / 2)
  s += r(waistX, 28, waistW, 2, belt)
  s += r(waistX, 28, waistW, 1, beltL)

  return s
})

/* ---------- TAY (áo tay ngắn + da + băng cổ tay xanh + bàn tay) ---------- */
function armColumn(x, y, w, h) {
  let s = ''
  const sleeve = 4
  s += r(x, y, w, sleeve, shirt.value)
  s += r(x, y, 1, sleeve, shirtL.value)
  s += r(x + w - 1, y, 1, sleeve, shirtD.value)
  s += r(x, y + sleeve, w, h - sleeve, skin)
  s += r(x, y + sleeve, 1, h - sleeve, skinH)
  s += r(x + w - 1, y + sleeve, 1, h - sleeve, skinD)
  // băng cổ tay xanh
  s += r(x, y + h - 1, w, 1, belt)
  // bàn tay
  s += r(x, y + h, w, 1, skinD)
  return s
}
const armsHtml = computed(() => {
  const c = cfg.value
  const shoulderW = torsoWidthAt(0, c)
  const lx = CENTER - Math.floor(shoulderW / 2) - c.armW
  const rx = CENTER + Math.ceil(shoulderW / 2)
  const swing = props.legFrame === 'a' ? 1 : -1
  const top = 16
  const h = 11
  let s = ''
  s += armColumn(lx, top + Math.max(0, -swing), c.armW, h)
  s += armColumn(rx, top + Math.max(0, swing), c.armW, h)
  return s
})

/* ---------- QUẦN SHORT (màu theo cosmetic, tách riêng khỏi áo) ---------- */
const shortsHtml = computed(() => {
  const c = cfg.value
  const w = torsoWidthAt(TORSO_H - 1, c)
  const x = CENTER - Math.floor(w / 2)
  const y = TORSO_TOP + TORSO_H // 30
  let s = ''
  s += r(x, y, w, 4, shorts.value)
  s += r(x, y, 1, 4, shortsL.value)
  s += r(x + w - 1, y, 1, 4, shortsD.value)
  s += r(CENTER, y + 2, 1, 2, shortsD.value) // khe 2 ống
  return s
})

/* ---------- CHÂN + GIÀY (walk animation) ---------- */
function legPiece(x, top, w, h, footShift) {
  let s = ''
  s += r(x, top, w, h, skin)
  s += r(x, top, 1, h, skinH)
  s += r(x + w - 1, top, 1, h, skinD)
  s += r(x + footShift, top + h, w + 1, 2, shoe)
  s += r(x + footShift, top + h, w + 1, 1, shoeH)
  return s
}
const legsHtml = computed(() => {
  const c = cfg.value
  const w = torsoWidthAt(TORSO_H - 1, c)
  const gap = 1
  const legTop = TORSO_TOP + TORSO_H + 4 // 34
  const legH = 8
  const leftX = CENTER - gap - c.legW
  const rightX = CENTER + gap
  const fwd = props.legFrame === 'a'
  let s = ''
  if (fwd) {
    s += legPiece(leftX, legTop + 1, c.legW, legH - 1, 1)
    s += legPiece(rightX, legTop, c.legW, legH, -1)
  } else {
    s += legPiece(leftX, legTop, c.legW, legH, -1)
    s += legPiece(rightX, legTop + 1, c.legW, legH - 1, 1)
  }
  return s
})
</script>