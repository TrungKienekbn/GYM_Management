<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>THỐNG KÊ QUẢN TRỊ</h2>
      <span class="mono" style="font-size:0.8rem;color:var(--c-text-inv2)">{{ today }}</span>
    </div>

    <div v-if="loading"><el-skeleton :rows="6" animated style="background:var(--c-card);padding:24px;border-radius:12px"/></div>

    <template v-else>
      <!-- Revenue stats -->
      <div class="grid-4" style="margin-bottom:24px">
        <div class="stat-card accent-card">
          <div class="label">TỔNG DOANH THU</div>
          <div class="value">{{ formatM(stats.totalRevenue) }}</div>
          <div class="sub">đồng</div>
          <div class="icon"></div>
        </div>
        <div class="stat-card">
          <div class="label">THÁNG NÀY</div>
          <div class="value">{{ formatM(stats.monthRevenue) }}</div>
          <div class="sub">đồng</div>
          <div class="icon"></div>
        </div>
        <div class="stat-card">
          <div class="label">TỔNG NGƯỜI DÙNG</div>
          <div class="value">{{ stats.totalUsers || 0 }}</div>
          <div class="sub">{{ stats.activeUsers || 0 }} đang hoạt động</div>
          <div class="icon"></div>
        </div>
        <div class="stat-card">
          <div class="label">THÀNH VIÊN TT</div>
          <div class="value">{{ stats.paidMembers || 0 }}</div>
          <div class="sub">/ {{ stats.totalMembers || 0 }} đăng ký</div>
          <div class="icon"></div>
        </div>
      </div>

      <!-- Quick action cards -->
      <div class="grid-3" style="margin-bottom:24px">
        <div class="quick-card" @click="$router.push('/admin/invoices')">
          <div class="q-icon"></div>
          <div class="q-title display">CHỜ CHUYỂN KHOẢN</div>
          <div class="q-count">{{ pendingCount }}</div>
          <div class="q-desc muted">Webhook sẽ tự động xác nhận</div>
        </div>
        <div class="quick-card" @click="$router.push('/admin/users')">
          <div class="q-icon"></div>
          <div class="q-title display">QUẢN LÝ NGƯỜI DÙNG</div>
          <div class="q-count">{{ stats.totalUsers || 0 }}</div>
          <div class="q-desc muted">Tài khoản trong hệ thống</div>
        </div>
        <div class="quick-card" @click="$router.push('/admin/ratings')">
          <div class="q-icon"></div>
          <div class="q-title display">ĐÁNH GIÁ MỚI</div>
          <div class="q-count">{{ recentRatingCount }}</div>
          <div class="q-desc muted">Chờ phản hồi</div>
        </div>
      </div>

      <!-- Recent transactions table -->
      <el-card>
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>GIAO DỊCH GẦN ĐÂY</span>
            <el-button text @click="$router.push('/admin/invoices')" style="color:var(--c-accent)">Xem tất cả →</el-button>
          </div>
        </template>
        <el-table :data="recentInvoices" stripe>
          <el-table-column label="Khách hàng" prop="userName" min-width="150"/>
          <el-table-column label="Email" prop="userEmail" min-width="180"/>
          <el-table-column label="Gói" prop="membershipType" width="110" align="center"/>
          <el-table-column label="Giá (đ)" width="140" align="right">
            <template #default="{row}">{{ Number(row.price).toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="Loại" prop="invoiceType" width="130" align="center"/>
          <el-table-column label="Trạng thái" width="130" align="center">
            <template #default="{row}">
              <span class="badge" :class="payBadge(row.status)">{{ row.status }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminAPI, ratingAPI } from '@/api'
import dayjs from 'dayjs'

const stats             = ref({})
const recentInvoices    = ref([])
const recentRatingCount = ref(0)
const loading           = ref(true)
const today             = dayjs().format('dddd, DD/MM/YYYY')

const pendingCount = computed(() => recentInvoices.value.filter(i => i.status === 'PENDING').length)

async function load() {
  loading.value = true
  try {
    const [rev, mem, rat] = await Promise.all([
      adminAPI.getRevenue(), adminAPI.getInvoices(), ratingAPI.getAll().catch(()=>({data:[]}))
    ])
    stats.value             = rev.data || {}
    recentInvoices.value    = (mem.data || []).slice(0, 10)
    recentRatingCount.value = (rat.data || []).filter(r => !r.adminReply).length
  } finally { loading.value = false }
}

function formatM(n) {
  if (!n) return '0'
  if (n >= 1_000_000) return (n/1_000_000).toFixed(1) + 'M'
  return Number(n).toLocaleString()
}
function payBadge(s) { return { PAID:'badge-success', PENDING:'badge-warning', FAILED:'badge-danger', REFUNDED:'badge-info' }[s]||'' }

onMounted(load)
</script>

<style scoped>
.quick-card {
  background:var(--c-card); border:1px solid var(--c-border2);
  border-radius:var(--radius-lg); padding:24px; cursor:pointer;
  transition:all var(--transition); text-align:center; box-shadow:var(--shadow);
}
.quick-card:hover { border-color:var(--c-accent); box-shadow:var(--shadow-lg); transform:translateY(-2px); }
.q-icon  { font-size:2rem; margin-bottom:8px; }
.q-title { font-size:0.9rem; color:var(--c-text); margin-bottom:4px; }
.q-count { font-family:var(--font-display); font-size:2.5rem; color:var(--c-accent); line-height:1; margin:4px 0; }
.q-desc  { font-size:0.78rem; }
</style>
