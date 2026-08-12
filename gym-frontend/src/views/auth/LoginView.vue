<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="bg-overlay"/>
      <div class="login-showcase">
        <div class="showcase-kicker">NỀN TẢNG LUYỆN TẬP CÁ NHÂN</div>
        <h1>TẬP ĐÚNG.<br/><span>TIẾN BỘ THẬT.</span></h1>
        <p class="showcase-lead">GymPro đồng hành cùng bạn từ buổi tập đầu tiên đến khi chạm mục tiêu — với giáo án phù hợp và tiến độ rõ ràng mỗi tuần.</p>
        <div class="showcase-features">
          <div><span>01</span><b>Giáo án cá nhân hóa</b><small>Theo mục tiêu, thể lực và lịch rảnh của bạn</small></div>
          <div><span>02</span><b>Theo dõi từng buổi</b><small>Ghi nhận khối lượng, thể lực và mức hoàn thành</small></div>
          <div><span>03</span><b>Bài tập & dinh dưỡng</b><small>Thư viện hướng dẫn và món ăn được đề xuất</small></div>
        </div>
        <div class="showcase-quote">“Mỗi buổi tập tốt là một bước gần hơn tới phiên bản mạnh mẽ nhất của bạn.”</div>
      </div>
    </div>
    <div class="auth-panel fade-in">
      <div class="auth-logo">
        <span class="display" style="font-size:2.8rem;color:var(--c-text)">GYM</span>
        <span class="display accent" style="font-size:2.8rem">PRO</span>
      </div>
      <p style="color:var(--c-text2);font-size:0.875rem;margin-bottom:32px">Đăng nhập vào tài khoản của bạn</p>

      <el-form :model="form" label-position="top">
        <el-form-item label="Email">
          <el-input v-model="form.email" placeholder="you@email.com" type="email" size="large"/>
        </el-form-item>
        <el-form-item label="Mật khẩu">
          <el-input v-model="form.password" placeholder="••••••••" type="password" size="large" show-password @keyup.enter="handleLogin"/>
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%;margin-top:8px;font-size:1rem;height:46px" :loading="auth.loading" @click="handleLogin">
          ĐĂNG NHẬP
        </el-button>
      </el-form>

      <button class="forgot-link" type="button" @click="forgotDialog=true">Quên mật khẩu?</button>

      <div style="text-align:center;color:var(--c-text2);font-size:0.875rem;margin-top:20px">
        Chưa có tài khoản? <router-link to="/register" style="color:var(--c-accent);font-weight:600">Đăng ký ngay</router-link>
      </div>

      <div class="demo-box">
        <div style="font-size:0.72rem;font-weight:700;color:var(--c-text3);letter-spacing:0.1em;margin-bottom:4px">TÀI KHOẢN THỬ NGHIỆM</div>
        <div style="font-size:0.78rem;color:var(--c-text2)">Quản trị viên: <strong>admin@gym.com</strong> / <strong>admin123</strong></div>
        <div class="demo-account"><b>Demo 1 · VIP tăng cơ tại phòng gym</b><span><strong>fulltest@gym.com</strong> / <strong>password</strong></span></div>
        <div class="demo-account"><b>Demo 2 · Gói thường giảm cân tại nhà</b><span><strong>fulltest2@gym.com</strong> / <strong>password</strong></span></div>
      </div>
    </div>
    <el-dialog v-model="forgotDialog" title="ĐẶT LẠI MẬT KHẨU" width="400px" align-center append-to-body>
      <el-form label-position="top">
        <el-form-item label="Email"><el-input v-model="forgot.email" type="email" /></el-form-item>
        <el-form-item label="4 số cuối số điện thoại"><el-input v-model="forgot.lastFourDigits" maxlength="4" inputmode="numeric" /></el-form-item>
        <el-form-item label="Mật khẩu mới"><el-input v-model="forgot.newPassword" type="password" show-password maxlength="72" /></el-form-item>
        <div style="font-size:.76rem;color:var(--c-text3)">Nhập sai 5 lần sẽ tạm khóa chức năng trong 15 phút.</div>
      </el-form>
      <template #footer><el-button @click="forgotDialog=false">Hủy</el-button><el-button type="primary" @click="resetPassword">Đổi mật khẩu</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authAPI } from '@/api'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const router = useRouter()
const form = reactive({ email: '', password: '' })
const forgotDialog = ref(false)
const forgot = reactive({ email:'', lastFourDigits:'', newPassword:'' })

async function handleLogin() {
  if (!form.email || !form.password) return
  try {
    const data = await auth.login(form)
    router.push(data.role === 'ROLE_ADMIN' ? '/admin' : '/app')
  } catch {}
}
async function resetPassword() {
  if (!forgot.email || !/^\d{4}$/.test(forgot.lastFourDigits) || forgot.newPassword.length < 6) {
    ElMessage.warning('Vui lòng nhập đủ email, 4 số cuối và mật khẩu mới tối thiểu 6 ký tự')
    return
  }
  await authAPI.resetPasswordWithPhone(forgot)
  ElMessage.success('Đã đổi mật khẩu. Hãy đăng nhập bằng mật khẩu mới.')
  form.email = forgot.email
  forgotDialog.value = false
}
</script>

<style scoped>
.auth-page { min-height:100vh; display:flex; }
.auth-bg {
  flex:1; position:relative; overflow:hidden;
  background: linear-gradient(135deg, #0F172A 0%, #172033 55%, #24324A 100%);
  display:flex; flex-direction:column; align-items:center; justify-content:center;
}
.bg-overlay {
  position:absolute; inset:0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}
.login-showcase{position:relative;z-index:1;width:min(720px,78%);color:#fff8f3}.showcase-kicker{font-size:.8rem;letter-spacing:.18em;color:#efb35f;font-weight:700}.login-showcase h1{font-size:clamp(3.2rem,5.3vw,6.5rem);line-height:.9;margin:22px 0 26px;letter-spacing:-.03em}.login-showcase h1 span{color:#efb35f}.showcase-lead{max-width:650px;color:#f0d9c7;font-size:1.08rem;line-height:1.65}.showcase-features{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-top:38px}.showcase-features>div{padding:18px;background:#fff1;border:1px solid #fff2;border-radius:12px;backdrop-filter:blur(5px)}.showcase-features span{display:block;color:#efb35f;font-weight:700;font-size:.75rem}.showcase-features b{display:block;margin:8px 0 5px}.showcase-features small{color:#e8d5c0;line-height:1.45}.showcase-quote{margin-top:32px;padding-left:16px;border-left:3px solid #d4892a;color:#d9bea8;font-style:italic}
@media(max-width:768px) { .auth-bg { display:none; } }

.auth-panel {
  width:440px; min-height:100vh; background:var(--c-card);
  display:flex; flex-direction:column; justify-content:center;
  padding:48px 40px; box-shadow:-8px 0 32px rgba(107,66,38,0.2);
}
.demo-box {
  margin-top:24px; padding:12px 16px;
  background:var(--c-card2); border:1px solid var(--c-border2);
  border-radius:var(--radius-lg); text-align:center;
}
.forgot-link{border:0;background:none;color:var(--c-accent);cursor:pointer;margin-top:12px;align-self:flex-end;font-family:inherit}
.demo-account{display:flex;flex-direction:column;gap:2px;margin-top:8px;padding-top:8px;border-top:1px solid var(--c-border2);font-size:.76rem;color:var(--c-text2)}
.demo-account b{color:var(--c-text)}
</style>
