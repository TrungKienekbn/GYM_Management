<!--
============================================================
FILE: src/components/pet/PetWidget.vue
Widget hiển thị pet chạy trái <-> phải trong khung "pet-stage".

- Pet và Aura (lửa) chạy CÙNG một quỹ đạo (@keyframes pet-run-cycle),
  nhưng Aura có animation-delay -> tạo hiệu ứng "lửa đuổi theo sau" pet
  thay vì dính chặt 1:1.
- SpiderWebOverlay (mạng nhện) được đặt làm con TRỰC TIẾP của .pet-stage,
  KHÔNG nằm trong track di chuyển -> mạng nhện đứng yên cố định quanh
  khung màn hình như thiết kế gốc, dù pet có chạy qua lại.
- Pet tự lật mặt (quay đầu) đúng lúc đổi hướng chạy, đồng bộ với mốc
  48%-50% trong keyframes.

CẬP NHẬT COSMETIC: click vào .pet-stage mở PetSkinShopDialog. Màu
áo/quần/tóc lấy từ pet.shirtColor/pantsColor/hairColor (BE trả về)
và truyền xuống PetSprite. Sau khi equip, dialog emit lại pet mới
-> cập nhật reactive state -> đổi màu ngay, không reload.

VỊ TRÍ ĐẶT FILE: src/components/pet/PetWidget.vue (ghi đè file cũ)
CÁC FILE LIÊN QUAN:
  - src/components/pet/PetSprite.vue      (đã sửa: nhận màu qua props)
  - src/components/pet/AuraEffect.vue     (giữ nguyên)
  - src/components/pet/SpiderWebOverlay.vue (giữ nguyên)
  - src/components/pet/PetSkinShopDialog.vue (mới)
============================================================
-->

<template>
  <div class="pet-stage" @click="showShop = true" title="Bấm để đổi trang phục">
    <!-- Mạng nhện: cố định quanh khung màn hình, KHÔNG di chuyển theo pet -->
    <SpiderWebOverlay :count="pet.webCount" class="stage-web" />

    <!-- Aura: chạy trễ hơn pet (đuổi theo sau) -->
    <div class="aura-track">
      <AuraEffect :tier="pet.auraTier" class="layer-aura" />
    </div>

    <!-- Pet: chạy trái <-> phải, tự lật mặt theo hướng di chuyển -->
    <div class="pet-track">
      <div class="pet-layer-wrap" :class="{ 'is-facing-left': facingLeft }">
        <PetSprite
          :stage="pet.stage"
          :leg-frame="legFrame"
          :shirt-color="pet.shirtColor"
          :pants-color="pet.pantsColor"
          :hair-color="pet.hairColor"
          class="layer-pet"
        />
      </div>
    </div>
  </div>

  <PetSkinShopDialog
    v-model="showShop"
    @equipped="onEquipped"
    @click.stop
  />
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { petAPI } from '@/api'
import PetSprite from './PetSprite.vue'
import AuraEffect from './AuraEffect.vue'
import SpiderWebOverlay from './SpiderWebOverlay.vue'
import PetSkinShopDialog from './PetSkinShopDialog.vue'

const pet = reactive({
  stage: 'AVERAGE',
  currentStreak: 0,
  missedStreak: 0,
  auraTier: 'NONE',
  webCount: 0,
  shirtColor: '#E8641E',
  pantsColor: '#B84A12',
  hairColor: '#F5C400'
})

const legFrame = ref('a')
const facingLeft = ref(false)
const showShop = ref(false)

let legTimer
let cycleTimer
let turnTimeout

// Phải khớp với thời lượng animation "pet-run-cycle" bên dưới (đơn vị: giây)
const RUN_SPEED = 10
// Mốc % trong keyframes bắt đầu quay đầu (48%-50% là lúc pet dừng ở mép phải)
const TURN_POINT_RATIO = 0.49

async function loadPet() {
  try {
    const r = await petAPI.get()
    Object.assign(pet, r.data)
  } catch (e) {
    console.error('Load pet failed', e)
  }
}

// Nhận PetResponse mới nhất từ dialog sau khi equip -> đổi màu ngay tại chỗ
function onEquipped(updatedPet) {
  Object.assign(pet, updatedPet)
}

// Mỗi vòng chạy: bắt đầu quay mặt phải (facingLeft=false),
// tới đúng mốc quay đầu (~49% thời lượng) thì lật sang trái.
function scheduleFacingCycle() {
  facingLeft.value = false
  turnTimeout = setTimeout(() => {
    facingLeft.value = true
  }, RUN_SPEED * 1000 * TURN_POINT_RATIO)
}

onMounted(() => {
  loadPet()
  legTimer = setInterval(() => {
    legFrame.value = legFrame.value === 'a' ? 'b' : 'a'
  }, 180)

  scheduleFacingCycle()
  cycleTimer = setInterval(scheduleFacingCycle, RUN_SPEED * 1000)
})

onUnmounted(() => {
  clearInterval(legTimer)
  clearInterval(cycleTimer)
  clearTimeout(turnTimeout)
})
</script>

<style scoped>
.pet-stage {
  position: relative;
  width: 100%;
  min-height: 260px;
  overflow: visible; /* để aura + pet tràn ra ngoài không bị cắt */
  background: transparent;
  cursor: pointer; /* gợi ý có thể click để mở shop */
}

/* ---------- Mạng nhện: cố định theo khung màn hình ---------- */
.stage-web {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 3; /* luôn nổi trên cùng, không di chuyển theo pet/aura */
}

/* ---------- Track của AURA: chạy trễ hơn pet ---------- */
.aura-track {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 96px;
  height: 144px;
  z-index: 0;
  pointer-events: none;
  animation: pet-run-cycle 10s linear infinite;
  animation-delay: 0.01s; /* <-- chỉnh số này để tăng/giảm độ trễ của lửa so với pet */
}

.layer-aura {
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 230px;
  height: 320px;
}

/* ---------- Track của PET: chạy đúng nhịp, không trễ ---------- */
.pet-track {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 96px;
  height: 144px;
  z-index: 1;
  animation: pet-run-cycle 10s linear infinite;
}

.pet-layer-wrap {
  position: relative;
  width: 96px;
  height: 144px;
  transition: transform 0.15s ease-in-out; /* lật mặt mượt hơn thay vì giật */
}
.pet-layer-wrap.is-facing-left {
  transform: scaleX(-1);
}

.layer-pet {
  position: absolute;
  left: 0;
  bottom: 0;
  transform: translate(1px, -45px); /* giữ nguyên vị trí pet trong lòng lửa */
}

/* Dùng "left" (không phải transform: translateX(%)) vì % của left tính
   theo chiều rộng .pet-stage (cha định vị), còn % trong transform chỉ
   tính theo chính phần tử đó (96px) -> sẽ không chạy đúng quãng đường */
@keyframes pet-run-cycle {
  0%   { left: 20; }
  48%  { left: calc(100% - 96px); }
  50%  { left: calc(100% - 96px); }
  98%  { left: 20; }
  100% { left: 20; }
}
</style>