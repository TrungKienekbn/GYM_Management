import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
    baseURL: '/api',
    timeout: 15000
})

api.interceptors.request.use(config => {
    const token = sessionStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

api.interceptors.response.use(
    res => res.data,
    err => {
        const msg = err.response?.data?.message || 'Lỗi kết nối server'
        if (err.response?.status === 401) {
            sessionStorage.clear()
            window.location.href = '/login'
        } else if (err.response?.status !== 404 && !err.config?.silentError) {
            ElMessage.error(msg)
        }
        return Promise.reject(err)
    }
)

export default api

// ── Auth ─────────────────────────────────────
export const authAPI = {
    register: (data) => api.post('/auth/register', data),
    login:    (data) => api.post('/auth/login', data),
    resetPasswordWithPhone: (data) => api.post('/auth/reset-password-phone', data),
    verify:   (token) => api.get(`/auth/verify-email?token=${token}`)
}

// ── Profile ───────────────────────────────────
export const profileAPI = {
    get:    ()     => api.get('/profile'),
    save:   (data) => api.post('/profile', data),
    getById:(id)   => api.get(`/profile/${id}`)
}

// ── Workout Plans ─────────────────────────────
export const planAPI = {
    generate:          ()     => api.post('/workout-plans/generate'),
    generateWithGoal: (data) => api.post('/workout-plans/generate-with-goal', data),
    getActive:    ()     => api.get('/workout-plans/active', { silentError: true }),
    getAll:       ()     => api.get('/workout-plans'),
    createCustom: (data) => api.post('/workout-plans', data),
    adjustWeek:   (id, data) => api.post(`/workout-plans/${id}/adjust-week`, data),
    getTemplates:    ()    => api.get('/workout-plans/templates'),
    selectTemplate:  (id)  => api.post(`/workout-plans/templates/${id}/select`),
    setBaseWeight: (planExerciseId, payload) => api.patch(`/workout-plans/plan-exercises/${planExerciseId}/base-weight`, payload),
    suggestDays: (sessions) => api.get('/workout-plans/suggest-days', { params: { sessions } }),
    getFitnessImprovementTemplates: (sessions) =>
        api.get('/workout-plans/fitness-improvement-templates', { params: { sessions } }),
    startFitnessImprovement: (templateId) =>
        api.post(`/workout-plans/fitness-improvement/${templateId}/start`)
}

// ── MỚI (Patch 6): Endurance Test ─────────────
export const enduranceTestAPI = {
    submit:  (data) => api.post('/endurance-test', data),
    getMine: ()     => api.get('/endurance-test')
}

// ── Sessions ──────────────────────────────────
export const sessionAPI = {
    getAll:       ()           => api.get('/sessions'),
    getWeek:      ()           => api.get('/sessions/this-week'),
    getById:      (id)         => api.get(`/sessions/${id}`),
    // ── MỚI (bugfix): kiểm tra thứ tự TRƯỚC khi enroll, không tạo gì cả ──
    checkOrder: (planDayId, weekNumber, sessionDate) =>
        api.get('/sessions/order-check', { params: { planDayId, weekNumber, sessionDate } }),
    enroll:       (data)       => api.post('/sessions/enroll', data),
    checkOut:     (id, data)   => api.post(`/sessions/${id}/check-out`, data),
    skip:         (id, notes)  => api.post(`/sessions/${id}/skip`, { notes }),
    finishSkippedWeek: (id, data) => api.post(`/sessions/${id}/skip-week-review`, data),
    delete:       (id)         => api.delete(`/sessions/${id}`),
    getWeekProgress: (planId, weekNumber) => api.get(`/sessions/week-progress?planId=${planId}&weekNumber=${weekNumber}`)
}
export const weeklyReviewAPI = {
    checkEligibility: (planId, weekNumber) =>
        api.get(`/weekly-reviews/eligibility?planId=${planId}&weekNumber=${weekNumber}`),
    submit:   (data) => api.post('/weekly-reviews', data),
    getMy:    ()     => api.get('/weekly-reviews/my')
}

export const petAPI = {
    get: () => api.get('/pet')
}

// ── Progress ──────────────────────────────────
export const progressAPI = {
    getAll:   ()       => api.get('/progress'),
    getLatest:()       => api.get('/progress/latest', { silentError: true }),
    add:      (data)   => api.post('/progress', data),
    update:   (id, d)  => api.put(`/progress/${id}`, d)
}

// ── Memberships ───────────────────────────────
export const membershipAPI = {
    getAll:         ()     => api.get('/memberships'),
    getActive:      ()     => api.get('/memberships/active', { silentError: true })
}
export const petCosmeticAPI = {
    getCatalog: ()     => api.get('/pet/cosmetics'),
    equip:      (code) => api.post(`/pet/cosmetics/${code}/equip`)
}

// thêm method mới bên cạnh create() cũ trong invoiceAPI:
export const invoiceAPI = {
    create:         (membershipType)     => api.post('/invoices', { membershipType }),
    createCosmetic: (cosmeticItemCode)   => api.post('/invoices', { cosmeticItemCode }),
    getAll:        ()   => api.get('/invoices'),
    getOne:        (id) => api.get(`/invoices/${id}`),
    regenerateQr:  (id) => api.post(`/invoices/${id}/regenerate-qr`),
    cancel:        (id) => api.post(`/invoices/${id}/cancel`)
}


// ── Exercises ─────────────────────────────────
export const exerciseAPI = {
    getAll:   (params) => api.get('/exercises', { params }),
    getById:  (id)     => api.get(`/exercises/${id}`),
    create:   (data)   => api.post('/exercises', data),
    update:   (id, d)  => api.put(`/exercises/${id}`, d),
    delete:   (id)     => api.delete(`/exercises/${id}`)
    ,restore: (id)     => api.patch(`/exercises/${id}/restore`)
}
export const injuryAreaAPI = {
    getAll: () => api.get('/injury-areas'),
    create: (label) => api.post('/injury-areas', { label }),
    update: (id, label) => api.put(`/injury-areas/${id}`, { label }),
    delete: (id) => api.delete(`/injury-areas/${id}`)
}
// ── Foods (Món ăn) ────────────────────────────
export const foodAPI = {
    getAll:   (params) => api.get('/foods', { params }), // params: { keyword, goal }
    getById:  (id)     => api.get(`/foods/${id}`),
    create:   (data)   => api.post('/foods', data),
    update:   (id, d)  => api.put(`/foods/${id}`, d),
    delete:   (id)     => api.delete(`/foods/${id}`)
}

// ── Ratings ───────────────────────────────────
export const ratingAPI = {
    add:          (formData)     => api.post('/ratings', formData, { timeout: 120000 }),
    update:       (id, formData) => api.put(`/ratings/${id}`, formData, { timeout: 120000 }),
    remove:       (id)           => api.delete(`/ratings/${id}`),
    getPublic:    ()             => api.get('/ratings/public'),
    getMy:        ()             => api.get('/ratings/my'),
    getAverages:  ()             => api.get('/ratings/averages'),
    getAll:       ()             => api.get('/ratings/admin/all'),
    adminReply:   (id, formData) => api.post(`/ratings/admin/${id}/reply`, formData, { timeout: 120000 })
}

export const shopAPI = {
    catalog: params => api.get('/shop/catalog', { params }),
    detail: id => api.get('/shop/products/' + id),
    addresses: () => api.get('/shop/addresses'),
    saveAddress: data => data.id ? api.put('/shop/addresses/' + data.id, data) : api.post('/shop/addresses', data),
    deleteAddress: id => api.delete('/shop/addresses/' + id),
    customerState: () => api.get('/shop/customer-state'),
    setCustomerState: (id, data) => api.put('/shop/customer-state/' + id, data),
    paymentMethods: () => api.get('/shop/payments/methods'),
    startPayment: id => api.post('/shop/payments/' + id + '/start'),
    uploadImage: file => { const body = new FormData(); body.append('file', file); return api.post('/shop/images', body) },
    products: (params) => api.get('/shop/products', { params }),
    cart: () => api.get('/shop/cart'),
    addCart: (productId, quantity = 1, variantId = null) => api.post('/shop/cart', { productId, quantity, variantId }),
    updateCart: (id, quantity) => api.put(`/shop/cart/${id}`, { quantity }),
    removeCart: (id) => api.delete(`/shop/cart/${id}`),
    checkout: (data) => api.post('/shop/orders', data),
    orders: () => api.get('/shop/orders'),
    order: (id) => api.get(`/shop/orders/${id}`),
    cancelOrder: (id) => api.post(`/shop/orders/${id}/cancel`),
    validateVoucher: (code, items) => api.post('/shop/vouchers/validate', { code, items }),
    bestVoucher: (items) => api.post('/shop/vouchers/best', { items }),
        publicVouchers: () => api.get('/shop/vouchers/public'),
        lookupOrder: (orderId, phone) => api.post('/shop/orders/lookup', { orderId, phone }),
        requestGuestCancelOtp: (orderId, email) => api.post('/shop/orders/guest-cancel/request-otp', { orderId, email }),
        cancelGuestOrder: (orderId, email, otp) => api.post('/shop/orders/guest-cancel', { orderId, email, otp }),
    productReviews: (productId, params) => api.get(`/shop/products/${productId}/reviews`, { params }),
    submitReview: (data) => api.post('/shop/reviews', data)
}
export const adminShopAPI = {
    inventory: params => api.get('/shop/admin/inventory', { params }),
    statistics: params => api.get('/shop/admin/statistics', { params }),
    products: () => api.get('/shop/admin/products'),
    create: (data) => api.post('/shop/admin/products', data),
    update: (id, data) => api.put(`/shop/admin/products/${id}`, data),
    hide: (id) => api.delete(`/shop/admin/products/${id}`),
    orders: () => api.get('/shop/admin/orders'),
    status: (id, status) => api.put(`/shop/admin/orders/${id}/status`, { status })
}

// ── Chat với bot ──────────────────────────────
export const chatAPI = {
    send:        (message)  => api.post('/chat', { message }),
    sendFile:    (formData) => api.post('/chat/attachments', formData, { timeout: 120000 }),
    getHistory:  ()         => api.get('/chat/history'),
    suggestions: ()         => api.get('/chat/suggestions'),
    clear:       ()         => api.delete('/chat/history')
}

// ── Chat với admin (User) ─────────────────────
export const supportAPI = {
    request:   (formData)    => api.post('/support/request', formData, { timeout: 120000 }),
    sessions:  ()            => api.get('/support/sessions'),
    messages:  (id)          => api.get(`/support/sessions/${id}/messages`),
    send:      (id, content) => api.post(`/support/sessions/${id}/messages`, { content }),
    sendFile:  (id, formData)=> api.post(`/support/sessions/${id}/attachments`, formData, { timeout: 120000 }),
    close:     (id)          => api.post(`/support/sessions/${id}/close`),
    rate:      (id, data)    => api.post(`/support/sessions/${id}/rating`, data)
}

// ── Chat với user (Admin) ─────────────────────
export const adminSupportAPI = {
    sessions: ()             => api.get('/admin/support/sessions'),
    start:    (formData)     => api.post('/admin/support/start', formData, { timeout: 120000 }),
    accept:   (id)           => api.post(`/admin/support/${id}/accept`),
    reject:   (id)           => api.post(`/admin/support/${id}/reject`),
    close:    (id)           => api.post(`/admin/support/${id}/close`),
    messages: (id)           => api.get(`/admin/support/${id}/messages`),
    send:     (id, content)  => api.post(`/admin/support/${id}/messages`, { content }),
    sendFile: (id, formData) => api.post(`/admin/support/${id}/attachments`, formData, { timeout: 120000 })
}

// ── Notifications ─────────────────────────────
export const notifAPI = {
    getAll:       () => api.get('/notifications'),
    getUnread:    () => api.get('/notifications/unread-count'),
    markAllRead:  () => api.post('/notifications/mark-all-read'),
    delete:       (id) => api.delete(`/notifications/${id}`)
}

// ── Dashboard ─────────────────────────────────
export const dashboardAPI = {
    get: () => api.get('/dashboard')
}

// ── Admin ─────────────────────────────────────
export const adminAPI = {
    dashboard:          ()           => api.get('/admin/dashboard'),
    getUsers:           ()           => api.get('/admin/users'),
    getUserById:        (id)         => api.get(`/admin/users/${id}`),
    getUserProfile:     (id)         => api.get(`/admin/users/${id}/profile`),
    toggleStatus:       (id, status) => api.put(`/admin/users/${id}/status`, { status }),
    resetPassword:      (id, pwd)    => api.put(`/admin/users/${id}/reset-password`, { newPassword: pwd }),
    deleteUser:         (id)         => api.delete(`/admin/users/${id}`),
    getMemberships:     ()           => api.get('/admin/memberships'),
    getPending:         ()           => api.get('/admin/memberships/pending'),
    refund:             (id)         => api.put(`/admin/memberships/${id}/refund`),
    getUserMemberships: (uid)        => api.get(`/admin/memberships/user/${uid}`),
    getInvoices:        ()     => api.get('/admin/invoices'),
    getUserInvoices:    (uid)  => api.get(`/admin/invoices/user/${uid}`),
    getRevenue:         ()           => api.get('/admin/stats/revenue'),
    getPlans:           ()           => api.get('/admin/workout-plans'),
    getUserPlans:       (uid)        => api.get(`/admin/workout-plans/user/${uid}`),
    deletePlan:         (id)         => api.delete(`/admin/workout-plans/${id}`),
    getTemplates:       ()           => api.get('/admin/workout-plans/templates'),
    createTemplate:     (data)       => api.post('/admin/workout-plans/templates', data),
    updateTemplate:     (id, data)   => api.put(`/admin/workout-plans/templates/${id}`, data),
    deleteTemplate:     (id)         => api.delete(`/admin/workout-plans/templates/${id}`),
    getRecommendedSchedules: () => api.get('/admin/training-config/schedules'),
    saveRecommendedSchedule: (sessions, days) => api.put(`/admin/training-config/schedules/${sessions}`, { days }),
    getMuscleSplit: (goal, sessions) => api.get('/admin/training-config/splits', { params: { goal, sessions } }),
    saveMuscleSplit: (data) => api.put('/admin/training-config/splits', data),
    broadcast:          (data)       => api.post('/admin/notifications/broadcast', data),
    sendToUser:         (uid, data)  => api.post(`/admin/notifications/user/${uid}`, data)
}
// ── System Configs (Công thức hệ thống) ───────
export const systemConfigAPI = {
    getAll:  ()          => api.get('/admin/system-configs'),
    update:  (key, data) => api.put(`/admin/system-configs/${key}`, data)
}
export const posAPI = {
    products: (params) => api.get('/shop/products', { params }),
    checkout: (data) => api.post('/shop/pos/orders', data),
    orders: () => api.get('/shop/pos/orders'),
    order: (id) => api.get(`/shop/pos/orders/${id}`),
    validateVoucher: (code, items) => api.post('/shop/vouchers/validate', { code, items }),
    bestVoucher: (items) => api.post('/shop/vouchers/best', { items })
}
export const shiftAPI = {
    assign: (data) => api.post('/shifts', data),
    all: () => api.get('/shifts'),
    mine: () => api.get('/shifts/mine'),
    checkIn: (id, cashAtStart) => api.post(`/shifts/${id}/check-in`, { cashAtStart }),
    checkOut: (id, cashCounted, handoverNote) => api.post(`/shifts/${id}/check-out`, { cashCounted, handoverNote })
}
export const voucherAdminAPI = {
    all: () => api.get('/shop/admin/vouchers'),
    create: (data) => api.post('/shop/admin/vouchers', data),
    update: (id, data) => api.put(`/shop/admin/vouchers/${id}`, data),
    remove: (id) => api.delete(`/shop/admin/vouchers/${id}`)
}
export const attributeAdminAPI = {
    all: () => api.get('/shop/admin/attributes'),
    create: (name) => api.post('/shop/admin/attributes', { name }),
    remove: (id) => api.delete(`/shop/admin/attributes/${id}`),
    addValue: (attributeId, value) => api.post(`/shop/admin/attributes/${attributeId}/values`, { value }),
    removeValue: (id) => api.delete(`/shop/admin/attributes/values/${id}`)
}

export const variantAdminAPI = {
    forProduct: (productId) => api.get(`/shop/products/${productId}/variants`),
    create: (productId, data) => api.post(`/shop/admin/products/${productId}/variants`, data),
    update: (id, data) => api.put(`/shop/admin/products/variants/${id}`, data),
    remove: (id) => api.delete(`/shop/admin/products/variants/${id}`)
}
