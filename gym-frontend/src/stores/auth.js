import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI, shopAPI } from '@/api'
import { ElMessage } from 'element-plus'

// ─────────────────────────────────────────────
// Decode JWT
// ─────────────────────────────────────────────
function parseJwt(token) {
  try {
    const parts = token.split('.')

    // JWT phải có đúng 3 phần
    if (parts.length !== 3) {
      return null
    }

    const base64 = parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/')

    return JSON.parse(atob(base64))
  } catch (err) {
    console.error('JWT Parse Error:', err)
    return null
  }
}

// ─────────────────────────────────────────────
// Kiểm tra token còn hạn
// ─────────────────────────────────────────────
function isTokenValid(token) {
  if (!token) return false

  const payload = parseJwt(token)

  if (!payload) return false
  if (!payload.exp) return false

  return payload.exp * 1000 > Date.now()
}

export const useAuthStore = defineStore('auth', () => {

  // ───────────────────────────────────────────
  // Lấy token
  // ───────────────────────────────────────────
  const token = ref(
      sessionStorage.getItem('token') || ''
  )

  // ───────────────────────────────────────────
  // Lấy user an toàn
  // ───────────────────────────────────────────
  let storedUser = null

  try {
    const rawUser = sessionStorage.getItem('user')

    storedUser =
        rawUser &&
        rawUser !== 'undefined'
            ? JSON.parse(rawUser)
            : null
  } catch (err) {
    console.error('USER PARSE ERROR:', err)
    storedUser = null
  }

  const user = ref(storedUser)

  const loading = ref(false)

  // ───────────────────────────────────────────
  // Nếu token lỗi → logout luôn
  // ───────────────────────────────────────────
  if (!isTokenValid(token.value)) {
    token.value = ''
    user.value = null

    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  // ───────────────────────────────────────────
  // Computed
  // ───────────────────────────────────────────
  const isLoggedIn = computed(() => {
    return !!token.value && isTokenValid(token.value)
  })

  const isAdmin = computed(() => {
    return user.value?.role === 'ROLE_ADMIN'
  })

  const isUser = computed(() => {
    return user.value?.role === 'ROLE_USER'
  })
  const isStaff = computed(() => {
  return user.value?.role === 'ROLE_STAFF'
})

  // ───────────────────────────────────────────
  // Check token
  // ───────────────────────────────────────────
  function checkToken() {
    const valid = isTokenValid(token.value)

    if (!valid) {
      logout()
      return false
    }

    return true
  }

  // ───────────────────────────────────────────
  // Login
  // ───────────────────────────────────────────
  async function mergeGuestData() {
      // Gộp giỏ hàng khách vãng lai (nếu có) vào tài khoản vừa đăng nhập
      try {
        const guestCart = JSON.parse(localStorage.getItem('guest_cart') || '[]')
        const remaining = [...guestCart]
        for (const item of guestCart) {
          try {
            await shopAPI.addCart(item.id, item.quantity, item.variantId ?? null)
            remaining.splice(remaining.indexOf(item), 1)
            // Persist each success so a later failure cannot duplicate earlier items.
            if (remaining.length) localStorage.setItem('guest_cart', JSON.stringify(remaining))
            else localStorage.removeItem('guest_cart')
          } catch (e) {
            console.warn('Không thể gộp sản phẩm vào giỏ hàng:', item.id, e)
          }
        }
        if (remaining.length) ElMessage.warning('Một số sản phẩm chưa thể chuyển vào giỏ tài khoản. Giỏ tạm vẫn được giữ lại.')
      } catch (e) {
        console.warn('Không thể gộp giỏ hàng khách vãng lai:', e)
      }

      try {
        for (const key of ['guest_wishlist', 'guest_recent']) {
          const pending = JSON.parse(localStorage.getItem(key) || '[]')
          for (const item of [...pending]) {
            try { await shopAPI.setCustomerState(item.id, key === 'guest_wishlist' ? {wishlisted:true} : {viewed:true});pending.splice(pending.indexOf(item),1);localStorage.setItem(key,JSON.stringify(pending)) } catch { }
          }
        }
      } catch { }
  }

  async function login(credentials) {
    loading.value = true

    try {
      const res = await authAPI.login(credentials)

      const authData = res.data

      if (!authData?.token) {
        throw new Error('Token không tồn tại')
      }

      if (!isTokenValid(authData.token)) {
        throw new Error('Token không hợp lệ')
      }

      token.value = authData.token

      user.value = {
        userId: authData.userId,
        fullName: authData.fullName,
        email: authData.email,
        role: authData.role,
        emailVerified: authData.emailVerified
      }

      // Save sessionStorage
      sessionStorage.setItem('token', token.value)

      sessionStorage.setItem(
          'user',
          JSON.stringify(user.value)
      )

      await mergeGuestData()

      ElMessage.success('Đăng nhập thành công!')

      return authData

    } catch (err) {
      console.error('LOGIN ERROR:', err)

      logout()

      ElMessage.error(
          err?.response?.data?.message ||
          err.message ||
          'Đăng nhập thất bại'
      )

      throw err

    } finally {
      loading.value = false
    }
  }

  // ───────────────────────────────────────────
  // Register
  // ───────────────────────────────────────────
  async function register(payload) {
    loading.value = true

    try {
      const res = await authAPI.register(payload)

      const authData = res.data

      if (!authData?.token) {
        throw new Error('Token không tồn tại')
      }

      if (!isTokenValid(authData.token)) {
        throw new Error('Token không hợp lệ')
      }

      token.value = authData.token

      user.value = {
        userId: authData.userId,
        fullName: authData.fullName,
        email: authData.email,
        role: authData.role,
        emailVerified: authData.emailVerified
      }

      sessionStorage.setItem('token', token.value)

      sessionStorage.setItem(
          'user',
          JSON.stringify(user.value)
      )

      await mergeGuestData()
      ElMessage.success('Đăng ký thành công!')

      return authData

    } catch (err) {
      console.error('REGISTER ERROR:', err)

      logout()

      ElMessage.error(
          err?.response?.data?.message ||
          err.message ||
          'Đăng ký thất bại'
      )

      throw err

    } finally {
      loading.value = false
    }
  }

  // ───────────────────────────────────────────
  // Logout
  // ───────────────────────────────────────────
  function logout() {
    token.value = ''
    user.value = null

    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  return {
    token,
    user,
    loading,

    isLoggedIn,
    isAdmin,
    isUser,

    checkToken,

    login,
    register,
    logout,
    isStaff
  }
})
