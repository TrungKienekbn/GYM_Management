<template>
  <el-card style="margin-bottom:20px">
    <template #header>Thuộc tính sản phẩm</template>
    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="Tên thuộc tính mới">
        <el-input v-model="newAttrName" placeholder="VD: Kích thước, Màu sắc" style="width:220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="createAttribute">Thêm thuộc tính</el-button>
      </el-form-item>
    </el-form>

    <div v-for="attr in attributes" :key="attr.id" class="attr-block">
      <div class="attr-head">
        <b>{{ attr.name }}</b>
        <el-button size="small" text type="danger" @click="deleteAttribute(attr)">Xóa thuộc tính</el-button>
      </div>
      <div class="attr-values">
        <el-tag v-for="v in attr.values" :key="v.id" closable @close="deleteValue(v)">{{ v.value }}</el-tag>
        <el-input v-model="newValueInput[attr.id]" placeholder="Thêm giá trị..." size="small" style="width:140px" @keyup.enter="addValue(attr)" />
        <el-button size="small" @click="addValue(attr)">Thêm</el-button>
      </div>
    </div>
  </el-card>

  <el-card>
    <template #header>Biến thể sản phẩm</template>
    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="Chọn sản phẩm">
        <el-select v-model="selectedProductId" filterable style="width:260px" @change="loadVariants">
          <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <template v-if="selectedProductId">
      <el-card class="new-variant-card" shadow="never">
        <template #header>Thêm biến thể mới</template>
        <el-form :inline="true">
          <el-form-item v-for="attr in attributes" :key="attr.id" :label="attr.name">
            <el-select v-model="newVariant.picks[attr.id]" clearable style="width:140px" placeholder="Chọn">
              <el-option v-for="v in attr.values" :key="v.id" :label="v.value" :value="v.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="SKU">
            <el-input v-model="newVariant.sku" style="width:120px" />
          </el-form-item>
          <el-form-item label="Tồn kho">
            <el-input-number v-model="newVariant.stock" :min="0" />
          </el-form-item>
          <el-form-item label="Giá riêng (bỏ trống = theo giá sản phẩm gốc)">
            <el-input-number v-model="newVariant.priceOverride" :min="0" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="createVariant">Tạo biến thể</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-table :data="variants" style="margin-top:16px" v-loading="loadingVariants">
        <el-table-column prop="label" label="Phân loại" />
        <el-table-column prop="sku" label="SKU" width="120" />
        <el-table-column label="Giá" width="120">
          <template #default="{ row }">{{ row.priceOverride != null ? formatVnd(row.priceOverride) : 'Theo SP gốc' }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="Tồn kho" width="90" />
        <el-table-column prop="active" label="Hoạt động" width="90" />
        <el-table-column width="180">
          <template #default="{ row }">
            <el-button size="small" @click="editForm={...row}">Sửa</el-button><el-button size="small" type="danger" text @click="deleteVariant(row)">Ngừng bán</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </el-card>
<el-dialog :model-value="!!editForm" title="Sửa phân loại" width="min(480px,94vw)" @close="editForm=null"><el-form v-if="editForm" label-position="top"><el-form-item label="SKU"><el-input v-model="editForm.sku"/></el-form-item><el-form-item label="Tồn kho"><el-input-number v-model="editForm.stock" :min="0"/></el-form-item><el-form-item label="Giá riêng (0 dùng giá sản phẩm)"><el-input-number v-model="editForm.priceOverride" :min="0"/></el-form-item><el-switch v-model="editForm.active" active-text="Đang bán"/></el-form><template #footer><el-button @click="saveVariant">Lưu</el-button></template></el-dialog></template>

<script setup>
import { ref, onMounted } from 'vue'
import { attributeAdminAPI, variantAdminAPI, adminShopAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const attributes = ref([])
const newAttrName = ref('')
const newValueInput = ref({})

const products = ref([])
const selectedProductId = ref(null)
const editForm=ref(null);async function saveVariant(){await variantAdminAPI.update(editForm.value.id,editForm.value);editForm.value=null;await loadVariants()}
const variants = ref([])
const loadingVariants = ref(false)
const newVariant = ref({ picks: {}, sku: '', stock: 0, priceOverride: null })

function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }

async function loadAttributes() {
  attributes.value = (await attributeAdminAPI.all()).data || []
}
async function createAttribute() {
  if (!newAttrName.value.trim()) return
  try {
    await attributeAdminAPI.create(newAttrName.value)
    newAttrName.value = ''
    ElMessage.success('Đã thêm thuộc tính')
    loadAttributes()
  } catch { }
}
async function deleteAttribute(attr) {
  await ElMessageBox.confirm(`Xóa thuộc tính "${attr.name}"? Các biến thể đang dùng giá trị này có thể bị ảnh hưởng.`, 'Xác nhận')
  await attributeAdminAPI.remove(attr.id)
  ElMessage.success('Đã xóa')
  loadAttributes()
}
async function addValue(attr) {
  const val = newValueInput.value[attr.id]
  if (!val || !val.trim()) return
  try {
    await attributeAdminAPI.addValue(attr.id, val)
    newValueInput.value[attr.id] = ''
    loadAttributes()
  } catch { }
}
async function deleteValue(v) {
  await ElMessageBox.confirm(`Xóa giá trị "${v.value}"?`, 'Xác nhận')
  await attributeAdminAPI.removeValue(v.id)
  loadAttributes()
}

async function loadProducts() {
  products.value = (await adminShopAPI.products()).data || []
}
async function loadVariants() {
  if (!selectedProductId.value) return
  loadingVariants.value = true
  try {
    variants.value = (await variantAdminAPI.forProduct(selectedProductId.value)).data || []
  } finally { loadingVariants.value = false }
}
async function createVariant() {
  const attributeValueIds = Object.values(newVariant.value.picks).filter(Boolean)
  if (attributeValueIds.length === 0) { ElMessage.error('Vui lòng chọn ít nhất 1 giá trị thuộc tính'); return }
  try {
    await variantAdminAPI.create(selectedProductId.value, {
      attributeValueIds, sku: newVariant.value.sku, stock: newVariant.value.stock, priceOverride: newVariant.value.priceOverride
    })
    ElMessage.success('Đã tạo biến thể')
    newVariant.value = { picks: {}, sku: '', stock: 0, priceOverride: null }
    loadVariants()
  } catch { }
}
async function deleteVariant(row) {
  await ElMessageBox.confirm('Xóa biến thể này?', 'Xác nhận')
  await variantAdminAPI.remove(row.id)
  ElMessage.success('Đã xóa')
  loadVariants()
}

onMounted(() => { loadAttributes(); loadProducts() })
</script>

<style scoped>
.attr-block { padding: 12px 0; border-bottom: 1px solid var(--el-border-color); }
.attr-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.attr-values { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.new-variant-card { margin-bottom: 0; background: var(--el-fill-color-light); }
</style>