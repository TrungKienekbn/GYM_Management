<template>
  <el-dialog v-model="visible" title="Chọn phân loại" width="min(520px, 94vw)" :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving">
    <h3>{{ product?.name }}</h3>
    <div v-loading="loading" class="variant-list" aria-label="Phân loại sản phẩm">
      <el-alert v-if="failed" title="Không tải được phân loại. Vui lòng thử lại." type="error" :closable="false" />
      <el-button v-if="failed" @click="loadOptions">Thử lại</el-button>
      <el-empty v-else-if="!loading && !options.length" description="Chưa có phân loại đang bán" />
      <button v-for="v in options" :key="v.id" type="button" class="variant-option" :class="{ selected: selectedId === v.id }" :disabled="v.stock <= 0 || saving" :aria-pressed="selectedId === v.id" @click="selectedId = v.id">
        <span><b>{{ v.label || v.sku || `Phân loại #${v.id}` }}</b><small>{{ v.stock > 0 ? `Còn ${v.stock} sản phẩm` : 'Hết hàng' }}</small></span>
        <strong>{{ money(priceOf(v)) }}</strong>
      </button>
    </div>
    <p v-if="selected">Đã chọn: <b>{{ selected.label || selected.sku }}</b></p>
    <template #footer>
      <el-button :disabled="saving" @click="visible = false">Đóng</el-button>
      <el-button type="primary" :disabled="!selected || loading || failed" :loading="saving" @click="confirm">Thêm vào giỏ</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { variantAdminAPI } from '@/api'

const props = defineProps({ addItem: { type: Function, required: true } })
const visible = ref(false), product = ref(null), options = ref([])
const selectedId = ref(null), loading = ref(false), saving = ref(false), failed = ref(false)
let requestId = 0
const selected = computed(() => options.value.find(v => v.id === selectedId.value && v.stock > 0))
const money = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
const priceOf = v => v.priceOverride > 0 ? v.priceOverride : product.value.salePrice || product.value.price

function open(p) {
  product.value = p
  visible.value = true
  loadOptions()
}
async function loadOptions() {
  const id = ++requestId
  options.value = []
  selectedId.value = null
  loading.value = true
  failed.value = false
  try {
    const response = await variantAdminAPI.forProduct(product.value.id)
    if (id === requestId) options.value = (response.data || []).filter(v => v.active === true)
  } catch {
    if (id === requestId) failed.value = true
  } finally {
    if (id === requestId) loading.value = false
  }
}
async function confirm() {
  if (!selected.value || saving.value) return
  saving.value = true
  try {
    if (await props.addItem(product.value, selected.value) !== false) visible.value = false
  } catch {
    // The API interceptor displays the error; keep the selection for retry.
  } finally {
    saving.value = false
  }
}
defineExpose({ open })
</script>

<style scoped>
.variant-list { min-height: 100px; display: grid; gap: 10px; }
.variant-option { display: flex; justify-content: space-between; align-items: center; gap: 12px; width: 100%; padding: 14px; text-align: left; border: 1px solid var(--el-border-color); border-radius: 8px; background: var(--el-bg-color); color: var(--el-text-color-primary); cursor: pointer; font: inherit; }
.variant-option.selected { border-color: var(--el-color-primary); background: var(--el-color-primary-light-9); }
.variant-option:disabled { opacity: .5; cursor: not-allowed; }
.variant-option small { display: block; margin-top: 6px; }
.variant-option strong { white-space: nowrap; }
</style>
