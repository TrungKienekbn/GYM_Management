<template>
  <div class="public-shop">
    <header class="shop-header">
      <router-link to="/" class="brand">GYM<span>PRO</span></router-link>
      <el-input v-model="keyword" placeholder="Tìm sản phẩm..." clearable style="width:280px" @input="load" />
      <div class="header-actions">
        <el-badge :value="wishlist.length" :hidden="!wishlist.length"><el-button circle @click="wishlistVisible = true">♥</el-button></el-badge>
        <el-badge :value="compareList.length" :hidden="!compareList.length"><el-button circle @click="compareVisible = true">⇄</el-button></el-badge>
        <el-badge :value="cartCount" :hidden="!cartCount"><el-button type="primary" @click="goToCart">Giỏ hàng</el-button></el-badge>
      </div>
    </header>

    <div v-if="activeVouchers.length" class="promo-bar">
      🎁 Khuyến mãi đang chạy:
      <span v-for="v in activeVouchers" :key="v.code" class="promo-tag">{{ v.code }} - {{ v.type === 'PERCENT' ? v.value + '%' : formatVnd(v.value) }}</span>
    </div>

    <div class="product-grid" v-loading="loading">
      <el-card v-for="p in products" :key="p.id" class="product-card">
        <img :src="p.imageUrl || fallback" class="product-img" />
        <div class="product-name">{{ p.name }}</div>
        <div class="product-price">
          <b>{{ formatVnd(p.salePrice || p.price) }}</b>
          <span v-if="p.salePrice" class="old-price">{{ formatVnd(p.price) }}</span>
        </div>
        <div class="product-actions">
          <el-button size="small" :disabled="p.stock <= 0" type="primary" @click="addToCart(p)">Thêm giỏ</el-button>
          <el-button size="small" :type="isWishlisted(p) ? 'danger' : 'default'" @click="toggleWishlist(p)">♥</el-button>
          <el-checkbox :model-value="isCompared(p)" @change="toggleCompare(p)">So sánh</el-checkbox>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="wishlistVisible" title="Sản phẩm yêu thích" width="600">
      <el-empty v-if="!wishlist.length" description="Chưa có sản phẩm yêu thích" />
      <div v-for="p in wishlist" :key="p.id" class="wishlist-row">
        <span>{{ p.name }}</span><b>{{ formatVnd(p.salePrice || p.price) }}</b>
        <el-button size="small" type="primary" @click="addToCart(p)">Thêm giỏ</el-button>
        <el-button size="small" text type="danger" @click="toggleWishlist(p)">Xóa</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="compareVisible" title="So sánh sản phẩm" width="700">
      <el-empty v-if="!compareList.length" description="Chưa chọn sản phẩm để so sánh (tối đa 3)" />
      <el-table v-else :data="compareRows">
        <el-table-column prop="label" label="" width="120" />
        <el-table-column v-for="p in compareList" :key="p.id" :prop="'p' + p.id" :label="p.name" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="loginPrompt" title="Cần đăng nhập" width="380">
      <p>Giỏ hàng của bạn đã được lưu tạm. Đăng nhập hoặc đăng ký để tiếp tục thanh toán.</p>
      <template #footer>
        <el-button @click="loginPrompt = false">Để sau</el-button>
        <el-button type="primary" @click="router.push('/login')">Đăng nhập</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { shopAPI } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()

const products = ref([])
const keyword = ref('')
const loading = ref(false)
const activeVouchers = ref([])
const fallback = 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=70'

const wishlist = ref(JSON.parse(localStorage.getItem('guest_wishlist') || '[]'))
const compareList = ref(JSON.parse(localStorage.getItem('guest_compare') || '[]'))
const guestCart = ref(JSON.parse(localStorage.getItem('guest_cart') || '[]'))
const wishlistVisible = ref(false)
const compareVisible = ref(false)
const loginPrompt = ref(false)

const cartCount = computed(() => guestCart.value.reduce((s, i) => s + i.quantity, 0))
const compareRows = computed(() => {
  const fields = [['name', 'Tên'], ['price', 'Giá gốc'], ['salePrice', 'Giá KM'], ['brand', 'Thương hiệu'], ['stock', 'Tồn kho']]
  return fields.map(([key, label]) => {
    const row = { label }
    compareList.value.forEach(p => { row['p' + p.id] = p[key] ?? '-' })
    return row
  })
})

function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }

async function load() {
  loading.value = true
  try {
    const [p, v] = await Promise.all([
      shopAPI.products({ keyword: keyword.value || undefined }),
      shopAPI.publicVouchers()
    ])
    products.value = p.data || []
    activeVouchers.value = v.data || []
  } finally { loading.value = false }
}

function persist() {
  localStorage.setItem('guest_wishlist', JSON.stringify(wishlist.value))
  localStorage.setItem('guest_compare', JSON.stringify(compareList.value))
  localStorage.setItem('guest_cart', JSON.stringify(guestCart.value))
}

function isWishlisted(p) { return wishlist.value.some(i => i.id === p.id) }
function toggleWishlist(p) {
  if (isWishlisted(p)) wishlist.value = wishlist.value.filter(i => i.id !== p.id)
  else wishlist.value.push(p)
  persist()
}

function isCompared(p) { return compareList.value.some(i => i.id === p.id) }
function toggleCompare(p) {
  if (isCompared(p)) { compareList.value = compareList.value.filter(i => i.id !== p.id); persist(); return }
  if (compareList.value.length >= 3) { ElMessage.warning('Chỉ so sánh tối đa 3 sản phẩm'); return }
  compareList.value.push(p); persist()
}

async function addToCart(p) {
  if (auth.isAuthenticated) {
    try { await shopAPI.addCart(p.id); ElMessage.success('Đã thêm vào giỏ hàng') } catch { }
    return
  }
  const existing = guestCart.value.find(i => i.id === p.id)
  if (existing) existing.quantity++
  else guestCart.value.push({ ...p, quantity: 1 })
  persist()
  ElMessage.success('Đã thêm vào giỏ (khách vãng lai)')
}

function goToCart() {
  if (auth.isAuthenticated) { router.push('/app/shop'); return }
  if (cartCount.value === 0) { ElMessage.info('Giỏ hàng đang trống'); return }
  loginPrompt.value = true
}

onMounted(load)
</script>

<style scoped>
.shop-header { display: flex; align-items: center; gap: 16px; padding: 16px 24px; }
.brand { font-family: var(--font-display, inherit); font-weight: 700; text-decoration: none; color: inherit; }
.header-actions { margin-left: auto; display: flex; gap: 10px; align-items: center; }
.promo-bar { background: #fff7e6; padding: 8px 24px; font-size: 0.85rem; }
.promo-tag { margin-left: 10px; font-weight: 600; }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; padding: 20px 24px; }
.product-card { text-align: center; }
.product-img { width: 100%; height: 140px; object-fit: cover; border-radius: 6px; }
.product-name { margin-top: 8px; font-weight: 600; }
.old-price { text-decoration: line-through; color: #999; margin-left: 6px; font-size: 0.85rem; }
.product-actions { margin-top: 8px; display: flex; gap: 6px; justify-content: center; align-items: center; flex-wrap: wrap; }
.wishlist-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #eee; }
</style>