import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
    history: createWebHistory(),

    routes: [

        // ─────────────────────────────────────────
        // PUBLIC
        // ─────────────────────────────────────────
        {
            path: '/',
            name: 'Home',
            component: () => import('@/views/HomeView.vue'),
            meta: { guest: true, allowAuthenticated: true }
        },

        {
            path: '/login',
            component: () =>
                import('@/views/auth/LoginView.vue'),

            meta: {
                guest: true
            }
        },

        {
            path: '/register',
            component: () =>
                import('@/views/auth/RegisterView.vue'),

            meta: {
                guest: true
            }
        },

        // ─────────────────────────────────────────
        // USER
        // ─────────────────────────────────────────
        {
            path: '/app',

            component: () =>
                import('@/components/common/UserLayout.vue'),

            meta: {
                requiresAuth: true,
                role: 'ROLE_USER'
            },

            children: [
                {
                    path: '',
                    redirect: '/app/dashboard'
                },

                {
                    path: 'dashboard',
                    name: 'UserDashboard',
                    component: () =>
                        import('@/views/user/DashboardView.vue')
                },

                {
                    path: 'profile',
                    name: 'Profile',
                    component: () =>
                        import('@/views/user/ProfileView.vue')
                },

                {
                    path: 'plan',
                    name: 'WorkoutPlan',
                    component: () =>
                        import('@/views/user/WorkoutPlanView.vue')
                },

                {
                    path: 'sessions',
                    name: 'Sessions',
                    component: () =>
                        import('@/views/user/SessionsView.vue')
                },

                {
                    path: 'progress',
                    name: 'Progress',
                    component: () =>
                        import('@/views/user/ProgressView.vue')
                },

                {
                    path: 'foods',
                    name: 'FoodList',
                    component: () =>
                        import('@/views/user/FoodListView.vue')
                },

                {
                    path: 'shop',
                    name: 'UserShop',
                    component: () =>
                        import('@/views/user/ShopView.vue')
                },
                {
                    path: '/shop',
                    name: 'PublicShop',
                    component: () => import('@/views/PublicShopView.vue'),
                    meta: { guest: true, allowAuthenticated: true }
                },

                {
                    path: 'membership',
                    name: 'Membership',
                    component: () =>
                        import('@/views/user/MembershipView.vue')
                },

                {
                    path: 'payment/:invoiceId',
                    name: 'UserPaymentQR',
                    component: () =>
                        import('@/views/user/PaymentQR.vue')
                },

                {
                    path: 'exercises',
                    name: 'Exercises',
                    component: () =>
                        import('@/views/user/ExercisesView.vue')
                },

                {
                    path: 'ratings',
                    name: 'Ratings',
                    component: () =>
                        import('@/views/user/RatingsView.vue')
                },

                {
                    path: 'chat',
                    name: 'Chat',
                    component: () =>
                        import('@/views/user/ChatView.vue')
                }
            ]
        },

        // ─────────────────────────────────────────
        // ADMIN
        // ─────────────────────────────────────────
        {
            path: '/admin',

            component: () =>
                import('@/components/common/AdminLayout.vue'),

            meta: {
                requiresAuth: true,
                role: 'ROLE_ADMIN'
            },

            children: [
                {
                    path: '',
                    redirect: '/admin/dashboard'
                },

                {
                    path: 'dashboard',
                    name: 'AdminDashboard',
                    component: () =>
                        import('@/views/admin/AdminDashboard.vue')
                },

                {
                    path: 'users',
                    name: 'AdminUsers',
                    component: () =>
                        import('@/views/admin/UsersView.vue')
                },

                {
                    path: 'memberships',
                    redirect: '/admin/invoices'
                },
                {
                    path: 'invoices',
                    name: 'AdminInvoices',
                    component: () =>
                        import('@/views/admin/InvoiceHistoryView.vue')
                },

                {
                    path: 'exercises',
                    name: 'AdminExercises',
                    component: () =>
                        import('@/views/admin/ExercisesAdmin.vue')
                },

                {
                    path: 'foods',
                    name: 'AdminFoods',
                    component: () =>
                        import('@/views/admin/FoodsAdmin.vue')
                },
                { path: 'shop', name: 'AdminShop', component: () => import('@/views/admin/ShopAdmin.vue') },

                {
                    path: 'plans',
                    name: 'AdminPlans',
                    component: () =>
                        import('@/views/admin/PlansView.vue')
                },

                {
                    path: 'ratings',
                    name: 'AdminRatings',
                    component: () =>
                        import('@/views/admin/AdminRatingsView.vue')
                },

                {
                    path: 'support',
                    name: 'AdminSupport',
                    component: () =>
                        import('@/views/admin/SupportView.vue')
                },

                {
                    path: 'notify',
                    name: 'AdminNotify',
                    component: () =>
                        import('@/views/admin/NotifyView.vue')
                },
                { path: 'staff-schedule', name: 'AdminStaffSchedule', component: () => import('@/views/admin/StaffScheduleAdmin.vue') },
                { path: 'vouchers', name: 'AdminVouchers', component: () => import('@/views/admin/VoucherAdmin.vue') },
                {
                    path: 'system-configs',
                    name: 'AdminSystemConfigs',
                    component: () =>
                        import('@/views/admin/SystemConfigAdmin.vue')
                }

            ]
        },
        // ─────────────────────────────────────────
        // STAFF
        // ─────────────────────────────────────────
        {
            path: '/staff',
            component: () => import('@/components/common/StaffLayout.vue'),
            meta: {
                requiresAuth: true,
                role: 'ROLE_STAFF'
            },
            children: [
                { path: '', redirect: '/staff/pos' },
                { path: 'pos', name: 'StaffPos', component: () => import('@/views/staff/PosCounterView.vue') },
                { path: 'pos-orders', name: 'StaffPosOrders', component: () => import('@/views/staff/PosOrdersView.vue') },
                { path: 'orders', name: 'StaffOrders', component: () => import('@/views/staff/OnlineOrdersView.vue') },
                { path: 'schedule', name: 'StaffSchedule', component: () => import('@/views/staff/ShiftView.vue') }
            ]
        },
        // ─────────────────────────────────────────
        // 404
        // ─────────────────────────────────────────
        {
            path: '/:pathMatch(.*)*',
            redirect: '/login'
        }
    ]
})

// ─────────────────────────────────────────────
// ROUTER GUARD
// ─────────────────────────────────────────────
function homeByRole(auth) {
    if (auth.isAdmin) return '/admin/dashboard'
    if (auth.isStaff) return '/staff/pos'
    return '/app/dashboard'
}
router.beforeEach((to, from, next) => {

    const auth = useAuthStore()

    console.log('PATH:', to.path)
    console.log('TOKEN:', auth.token)
    console.log('USER:', auth.user)

    // ─────────────────────────────────────────
    // Route cần login
    // ─────────────────────────────────────────
    if (to.meta.requiresAuth) {

        const valid = auth.checkToken()

        console.log('TOKEN VALID:', valid)

        // Chưa login
        if (!valid) {
            return next('/login')
        }

        // Sai role
        if (
            to.meta.role &&
            auth.user?.role !== to.meta.role
        ) {
            return next(homeByRole(auth))
        }

        return next()
    }

    // ─────────────────────────────────────────
    // Guest route
    // ─────────────────────────────────────────
    if (to.meta.guest && !to.meta.allowAuthenticated) {

        const valid = auth.checkToken()

        if (valid) {
            return next(homeByRole(auth))
        }

        return next()
    }

    next()
})

export default router
