<template>
  <el-card>
    <template #header>Tạo / Sửa Voucher</template>
    <el-form :inline="true">
      <el-form-item label="Mã"><el-input v-model="form.code" style="width:140px" /></el-form-item>
      <el-form-item label="Loại">
        <el-select v-model="form.type" style="width:150px">
          <el-option label="Phần trăm (%)" value="PERCENT" />
          <el-option label="Số tiền cố định" value="AMOUNT" />
        </el-select>
      </el-form-item>
      <el-form-item label="Giá trị"><el-input-number v-model="form.value" :min="0" /></el-form-item>
      <el-form-item label="Đơn tối thiểu"><el-input-number v-model="form.minOrderAmount" :min="0" /></el-form-item>
      <el-form-item label="Giảm tối đa"><el-input-number v-model="form.maxDiscountAmount" :min="0" /></el-form-item>
      <el-form-item label="Số lượt"><el-input-number v-model="form.usageLimit" :min="1" /></el-form-item>
      <el-form-item label="Bắt đầu"><el-date-picker v-model="form.startAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item label="Kết thúc"><el-date-picker v-model="form.endAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item label="Mô tả"><el-input v-model="form.description" style="width:220px" /></el-form-item>

      <el-form-item label="Phạm vi áp dụng">
        <el-select v-model="form.scopeType" style="width:180px">
          <el-option label="Toàn bộ đơn hàng" value="ALL" />
          <el-option label="Theo danh mục" value="CATEGORY" />
          <el-option label="Theo sản phẩm cụ thể" value="PRODUCT" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.scopeType === 'CATEGORY'" label="Danh mục">
        <el-select v-model="form.scopeCategory" style="width:180px">
          <el-option label="Thực phẩm bổ sung" value="SUPPLEMENT" />
          <el-option label="Đồ ăn" value="FOOD" />
          <el-option label="Dụng cụ" value="EQUIPMENT" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.scopeType === 'PRODUCT'" label="Sản phẩm áp dụng">
        <el-select v-model="form.scopeProductIds" multiple filterable style="width:300px" placeholder="Chọn sản phẩm">
          <el-option v-for="p in allProducts" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="save">{{ editingId ? 'Cập nhật' : 'Tạo mới' }}</el-button>
        <el-button v-if="editingId" @click="resetForm">Hủy sửa</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-table :data="vouchers" style="margin-top:16px" v-loading="loading">
    <el-table-column prop="code" label="Mã" width="120" />
    <el-table-column prop="description" label="Mô tả" />
    <el-table-column label="Giá trị" width="110">
      <template #default="{ row }">{{ row.type === 'PERCENT' ? row.value + '%' : formatVnd(row.value) }}</template>
    </el-table-column>
    <el-table-column label="Phạm vi" width="150">
      <template #default="{ row }">
        <span v-if="row.scopeType === 'ALL' || !row.scopeType">Toàn đơn</span>
        <span v-else-if="row.scopeType === 'CATEGORY'">Danh mục: {{ catLabel(row.scopeCategory) }}</span>
        <span v-else>{{ (row.scopeProductIds || []).length }} sản phẩm</span>
      </template>
    </el-table-column>
    <el-table-column label="Đã dùng" width="100">
      <template #default="{ row }">{{ row.usedCount }}/{{ row.usageLimit ?? '∞' }}</template>
    </el-table-column>
    <el-table-column label="Hết hạn" width="160">
      <template #default="{ row }">{{ row.endAt ? new Date(row.endAt).toLocaleString('vi-VN') : 'Không giới hạn' }}</template>
    </el-table-column>
    <el-table-column prop="active" label="Hoạt động" width="90" />
    <el-table-column width="150">
      <template #default="{ row }">
        <el-button size="small" @click="edit(row)">Sửa</el-button>
        <el-button size="small" type="danger" @click="remove(row)">Ngừng</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { voucherAdminAPI, adminShopAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const vouchers = ref([])
const allProducts = ref([])
const loading = ref(false)
const editingId = ref(null)
const form = ref(emptyForm())

function emptyForm() {
  return { code: '', type: 'PERCENT', value: 10, minOrderAmount: null, maxDiscountAmount: null, usageLimit: null, startAt: null, endAt: null, description: '', scopeType: 'ALL', scopeCategory: null, scopeProductIds: [] }
}
function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }
function catLabel(v) { return ({ SUPPLEMENT: 'Thực phẩm bổ sung', FOOD: 'Đồ ăn', EQUIPMENT: 'Dụng cụ' })[v] || v }

async function load() {
  loading.value = true
  try { vouchers.value = (await voucherAdminAPI.all()).data || [] } finally { loading.value = false }
}
async function loadProducts() {
  allProducts.value = (await adminShopAPI.products()).data || []
}
function edit(row) { editingId.value = row.id; form.value = { ...emptyForm(), ...row, scopeProductIds: row.scopeProductIds || [] } }
function resetForm() { editingId.value = null; form.value = emptyForm() }

async function save() {
  try {
    if (editingId.value) await voucherAdminAPI.update(editingId.value, form.value)
    else await voucherAdminAPI.create(form.value)
    ElMessage.success('Đã lưu voucher')
    resetForm(); load()
  } catch { }
}
async function remove(row) {
  await ElMessageBox.confirm(`Ngừng áp dụng voucher ${row.code}?`, 'Xác nhận')
  await voucherAdminAPI.remove(row.id)
  ElMessage.success('Đã ngừng áp dụng'); load()
}
onMounted(() => { load(); loadProducts() })
</script>