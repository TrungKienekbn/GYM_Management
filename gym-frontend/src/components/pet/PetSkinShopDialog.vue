<template>
  <el-dialog :model-value="modelValue" title="Chọn trang phục" width="420px"
             @update:model-value="v => emit('update:modelValue', v)">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="Áo" name="SHIRT" />
      <el-tab-pane label="Quần" name="PANTS" />
      <el-tab-pane label="Tóc" name="HAIR" />
    </el-tabs>

    <div class="item-list" v-loading="loading">
      <div v-for="item in filteredItems" :key="item.code" class="item-row">
        <span class="swatch" :style="{ background: item.colorHex }" />
        <span class="name">{{ item.displayName }}</span>

        <template v-if="item.equipped">
          <el-tag type="success">Đang mặc</el-tag>
        </template>
        <template v-else-if="item.owned">
          <el-button size="small" @click="equip(item.code)">Mặc</el-button>
        </template>
        <template v-else>
          <span class="price">{{ item.price.toLocaleString() }}đ</span>
          <el-button size="small" type="primary" @click="startPurchase(item)">Mua</el-button>
        </template>
      </div>
    </div>

    <!-- Thanh toán -->
    <div v-if="paymentInvoice" class="payment-box">
      <p>Quét mã để thanh toán {{ paymentInvoice.price.toLocaleString() }}đ</p>
      <img :src="paymentInvoice.qrCodeUrl" width="200" />
      <p class="hint">Đang chờ xác nhận thanh toán...</p>
      <el-button text @click="cancelPurchase">Hủy</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { petCosmeticAPI, invoiceAPI } from '@/api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  pet: { type: Object, required: true }
})
const emit = defineEmits(['update:modelValue', 'equipped'])

const activeTab = ref('SHIRT')
const items = ref([])
const loading = ref(false)
const paymentInvoice = ref(null)
let pollTimer = null

const filteredItems = computed(() => items.value.filter(i => i.slot === activeTab.value))

async function loadCatalog() {
  loading.value = true
  try {
    const r = await petCosmeticAPI.getCatalog()
    items.value = r.data || []
  } finally {
    loading.value = false
  }
}

async function equip(code) {
  const r = await petCosmeticAPI.equip(code)
  emit('equipped', r.data)
  await loadCatalog()
}

async function startPurchase(item) {
  const r = await invoiceAPI.createCosmetic(item.code)
  paymentInvoice.value = r.data
  pollTimer = setInterval(async () => {
    const check = await invoiceAPI.getOne(paymentInvoice.value.id)
    if (check.data.status === 'PAID') {
      clearInterval(pollTimer)
      paymentInvoice.value = null
      await loadCatalog()
    } else if (['EXPIRED', 'CANCELLED', 'FAILED'].includes(check.data.status)) {
      clearInterval(pollTimer)
      paymentInvoice.value = null
    }
  }, 3000)
}

async function cancelPurchase() {
  if (paymentInvoice.value) {
    await invoiceAPI.cancel(paymentInvoice.value.id).catch(() => {})
  }
  clearInterval(pollTimer)
  paymentInvoice.value = null
}

watch(() => props.modelValue, v => { if (v) loadCatalog() })
onUnmounted(() => clearInterval(pollTimer))
</script>

<style scoped>
.item-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; }
.swatch { width: 20px; height: 20px; border-radius: 4px; display: inline-block; border: 1px solid #ddd; }
.name { flex: 1; }
.price { font-size: 0.85rem; color: #888; }
.payment-box { text-align: center; margin-top: 16px; border-top: 1px solid #eee; padding-top: 16px; }
.hint { font-size: 0.8rem; color: #999; }
</style>