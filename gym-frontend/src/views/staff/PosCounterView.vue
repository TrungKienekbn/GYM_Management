<template>
  <div class="pos">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header>Sản phẩm</template>
          <el-input v-model="keyword" placeholder="Tìm sản phẩm..." clearable @input="loadProducts" style="margin-bottom:12px" />
          <el-table :data="productList" height="500" v-loading="loadingProducts">
            <el-table-column prop="name" label="Tên" />
            <el-table-column label="Giá" width="120">
              <template #default="{ row }">{{ formatVnd(row.salePrice || row.price) }}</template>
            </el-table-column>
            <el-table-column label="Tồn kho" width="100">
              <template #default="{ row }">{{ row.hasVariants ? 'Theo phân loại' : row.stock }}</template>
            </el-table-column>
            <el-table-column width="100">
              <template #default="{ row }">
                <el-button size="small" type="primary" :disabled="!row.hasVariants && row.stock <= 0" @click="addToCart(row)">Thêm</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card style="margin-bottom:16px">
          <template #header>Giỏ hàng tại quầy</template>
          <el-table :data="cart" size="small">
            <el-table-column label="Sản phẩm">
              <template #default="{ row }">
                <div>{{ row.name }}</div>
                <div v-if="row.variantLabel" class="variant-tag">{{ row.variantLabel }}</div>
              </template>
            </el-table-column>
            <el-table-column label="SL" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :max="row.stock" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="Thành tiền" width="110">
              <template #default="{ row }">{{ formatVnd((row.salePrice || row.price) * row.quantity) }}</template>
            </el-table-column>
            <el-table-column width="50">
              <template #default="{ row }">
                <el-button size="small" text type="danger" @click="removeFromCart(row)">X</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="total-row">Tổng cộng: <b>{{ formatVnd(cartTotal) }}</b></div>
        </el-card>

        <el-card>
          <template #header>Thông tin khách hàng</template>
          <el-form label-position="top">
            <el-form-item label="Tên khách (khách vãng lai)">
              <el-input v-model="form.customerName" placeholder="VD: Khách lẻ, hoặc tên khách" />
            </el-form-item>
            <el-form-item label="Số điện thoại">
              <el-input v-model="form.customerPhone" />
            </el-form-item>
            <el-form-item label="Email (để gửi hóa đơn, tuỳ chọn)">
              <el-input v-model="form.customerEmail" />
            </el-form-item>
            <el-form-item label="Phương thức thanh toán">
              <el-select v-model="form.paymentMethod" style="width:100%">
                <el-option label="Tiền mặt" value="CASH" />
                <el-option label="Chuyển khoản" value="BANK_TRANSFER" />
              </el-select>
            </el-form-item>
            <el-form-item label="Mã voucher">
  <el-input v-model="form.voucherCode" placeholder="Nhập mã (nếu có)">
    <template #append><el-button @click="applyVoucher">Áp dụng</el-button></template>
  </el-input>
</el-form-item>
<div v-if="voucherDiscount > 0" style="margin-bottom:12px;color:var(--el-color-success)">Giảm giá voucher: -{{ formatVnd(voucherDiscount) }}</div>
            <el-form-item label="Ghi chú">
              <el-input v-model="form.note" type="textarea" :rows="2" />
            </el-form-item>
          </el-form>
          <el-button type="success" style="width:100%" :loading="submitting" :disabled="cart.length === 0" @click="checkout">
            Thanh toán ({{ formatVnd(cartTotal) }})
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="variantDialogVisible" title="Chọn phân loại" width="380px">
      <div v-for="v in variantOptions" :key="v.id" class="variant-pick-row" @click="pickVariant(v)">
        <span>{{ v.label }}</span>
        <span>{{ v.stock > 0 ? `Còn ${v.stock}` : 'Hết hàng' }}</span>
      </div>
      <el-empty v-if="!variantOptions.length" description="Sản phẩm này chưa có biến thể nào" />
    </el-dialog>

    <el-dialog v-model="invoiceVisible" title="Hóa đơn bán hàng" width="480px">
      <div v-if="lastOrder">
        <p>Mã hóa đơn: <b>#{{ lastOrder.id }}</b></p>
        <p>Khách hàng: {{ lastOrder.receiverName }}</p>
        <p>Phương thức: {{ lastOrder.paymentMethod }}</p>
        <el-table :data="lastOrder.items" size="small" style="margin:12px 0">
          <el-table-column label="Sản phẩm">
            <template #default="{ row }">
              <div>{{ row.productName }}</div>
              <div v-if="row.variantLabel" class="variant-tag">{{ row.variantLabel }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="SL" width="60" />
          <el-table-column label="Thành tiền" width="110">
            <template #default="{ row }">{{ formatVnd(row.lineTotal) }}</template>
          </el-table-column>
        </el-table>
        <div class="total-row">Tổng cộng: <b>{{ formatVnd(lastOrder.total) }}</b></div>
      </div>
      <template #footer>
        <el-button type="primary" @click="invoiceVisible = false">Đóng</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { posAPI, variantAdminAPI } from '@/api'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const productList = ref([])
const loadingProducts = ref(false)
const cart = ref([])
const submitting = ref(false)
const invoiceVisible = ref(false)
const lastOrder = ref(null)

const variantDialogVisible = ref(false)
const variantOptions = ref([])
const variantPickProduct = ref(null)

const form = ref({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  paymentMethod: 'CASH',
  note: '',
  voucherCode: ''
})
const voucherDiscount = ref(0)

async function applyVoucher() {
  if (!form.value.voucherCode) { voucherDiscount.value = 0; return }
  try {
    const items = cart.value.map(it => ({ productId: it.id, lineTotal: (it.salePrice || it.price) * it.quantity }))
    const res = await posAPI.validateVoucher(form.value.voucherCode, items)
    voucherDiscount.value = res.data.discount
    ElMessage.success('Áp dụng voucher thành công')
  } catch { voucherDiscount.value = 0 }
}

const cartTotal = computed(() =>
  cart.value.reduce((sum, it) => sum + (it.salePrice || it.price) * it.quantity, 0)
)

function formatVnd(v) {
  return (v || 0).toLocaleString('vi-VN') + ' đ'
}

async function loadProducts() {
  loadingProducts.value = true
  try {
    const res = await posAPI.products({ keyword: keyword.value })
    productList.value = res.data || []
  } finally {
    loadingProducts.value = false
  }
}

async function addToCart(product) {
  if (product.hasVariants) {
    variantPickProduct.value = product
    const res = await variantAdminAPI.forProduct(product.id)
    variantOptions.value = res.data || []
    variantDialogVisible.value = true
    return
  }
  pushToCart(product, null, null, product.stock, product.salePrice || product.price)
}

function pickVariant(v) {
  if (v.stock <= 0) { ElMessage.warning('Phân loại này đã hết hàng'); return }
  const unit = v.priceOverride || variantPickProduct.value.salePrice || variantPickProduct.value.price
  pushToCart(variantPickProduct.value, v.id, v.label, v.stock, unit)
  variantDialogVisible.value = false
}

function pushToCart(product, variantId, variantLabel, stock, unitPrice) {
  const existing = cart.value.find(it => it.id === product.id && it.variantId === variantId)
  if (existing) {
    if (existing.quantity < stock) existing.quantity++
    else ElMessage.warning('Đã đạt số lượng tồn kho tối đa')
  } else {
    cart.value.push({ ...product, quantity: 1, variantId, variantLabel, stock, salePrice: unitPrice, price: unitPrice })
  }
}

function removeFromCart(row) {
  cart.value = cart.value.filter(it => !(it.id === row.id && it.variantId === row.variantId))
}

async function checkout() {
  if (!form.value.customerName.trim()) {
    ElMessage.error('Vui lòng nhập tên khách (VD: Khách lẻ)')
    return
  }
  submitting.value = true
  try {
    const payload = {
      items: cart.value.map(it => ({ productId: it.id, variantId: it.variantId, quantity: it.quantity })),
      customerName: form.value.customerName,
      customerPhone: form.value.customerPhone,
      customerEmail: form.value.customerEmail,
      paymentMethod: form.value.paymentMethod,
      note: form.value.note,
      voucherCode: form.value.voucherCode
    }
    const res = await posAPI.checkout(payload)
    lastOrder.value = res.data
    invoiceVisible.value = true
    cart.value = []
    form.value = { customerName: '', customerPhone: '', customerEmail: '', paymentMethod: 'CASH', note: '', voucherCode: '' }
    voucherDiscount.value = 0
    loadProducts()
  } finally {
    submitting.value = false
  }
}

onMounted(loadProducts)
</script>

<style scoped>
.total-row { text-align: right; font-size: 1.05rem; margin-top: 10px; }
.variant-tag { font-size: 0.75rem; color: #888; }
.variant-pick-row { display: flex; justify-content: space-between; padding: 10px; border: 1px solid var(--el-border-color); border-radius: 6px; margin-bottom: 8px; cursor: pointer; }
.variant-pick-row:hover { background: var(--el-fill-color-light); }
</style>