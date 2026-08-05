<template>
  <el-dialog :model-value="modelValue" title="Chọn trang phục" width="420px" append-to-body
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

  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { petCosmeticAPI, invoiceAPI } from '@/api'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  pet: { type: Object, required: true }
})
const emit = defineEmits(['update:modelValue', 'equipped'])

const activeTab = ref('SHIRT')
const items = ref([])
const loading = ref(false)
const router = useRouter()

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
  emit('update:modelValue', false)
  router.push(`/app/payment/${r.data.id}`)
}

watch(() => props.modelValue, v => { if (v) loadCatalog() })
</script>

<style scoped>
.item-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; }
.swatch { width: 20px; height: 20px; border-radius: 4px; display: inline-block; border: 1px solid #ddd; }
.name { flex: 1; }
.price { font-size: 0.85rem; color: #888; }
</style>
