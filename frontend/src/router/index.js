import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const router = createRouter({
  history: createWebHistory('/scm/'),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/register',
      component: () => import('@/views/register/index.vue')
    },
    {
      path: '/',
      component: Layout,
      redirect: '/blank',
      children: [
        {
          path: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'system',
          children: [
            {
              path: 'user',
              component: () => import('@/views/system/user/index.vue'),
              meta: { title: '用户管理' }
            },
            {
              path: 'menu',
              component: () => import('@/views/system/menu/index.vue'),
              meta: { title: '菜单管理' }
            },
            {
              path: 'role',
              component: () => import('@/views/system/role/index.vue'),
              meta: { title: '角色管理' }
            },
            {
              path: 'dict',
              component: () => import('@/views/system/dict/index.vue'),
              meta: { title: '字典管理' }
            },
            {
              path: 'dict-item',
              component: () => import('@/views/system/dict-item/index.vue'),
              meta: { title: '字典项管理' }
            }
          ]
        },
        {          path: 'product',          children: [            {              path: 'index',              component: () => import('@/views/product/index.vue'),              meta: { title: '商品列表' }            },            {              path: 'category',              component: () => import('@/views/product/category.vue'),              meta: { title: '商品分类' }            },            {              path: 'detail/:id',              component: () => import('@/views/product/detail.vue'),              meta: { title: '商品详情' }            },            {              path: 'selection',              component: () => import('@/views/product/selection.vue'),              meta: { title: '商品选购' }            }          ]        },
        {          path: 'blank',          component: () => import('@/views/blank/index.vue'),          meta: { title: '' }        },
        {
          path: 'order',
          children: [
            {
              path: 'index',
              component: () => import('@/views/order/index.vue'),
              meta: { title: '订单列表' }
            },
            {
              path: 'detail/:id',
              component: () => import('@/views/order/detail.vue'),
              meta: { title: '订单详情' }
            },
            {
              path: 'statistics',
              component: () => import('@/views/order/statistics.vue'),
              meta: { title: '订单统计' }
            }
          ]
        },
        {
          path: 'supplier',
          children: [
            {
              path: 'index',
              component: () => import('@/views/supplier/index.vue'),
              meta: { title: '供应商管理' }
            },
            {
              path: 'detail/:id',
              component: () => import('@/views/supplier/detail.vue'),
              meta: { title: '供应商详情' }
            }
          ]
        },
        {
          path: 'inventory',
          children: [
            {
              path: 'index',
              component: () => import('@/views/inventory/index.vue'),
              meta: { title: '库存管理' }
            }
          ]
        },
        {
          path: 'material',
          children: [
            {
              path: 'index',
              component: () => import('@/views/material/index.vue'),
              meta: { title: '物料管理' }
            }
          ]
        },
        {
          path: 'sales',
          children: [
            {
              path: 'index',
              component: () => import('@/views/sales/index.vue'),
              meta: { title: '销售额管理' }
            }
          ]
        },
        {
          path: 'production',
          children: [
            {
              path: 'index',
              component: () => import('@/views/production/index.vue'),
              meta: { title: '生产计划管理' }
            }
          ]
        },
        // 其他业务路由...
        {
          path: 'profile',
          component: () => import('@/views/profile/index.vue'),
          meta: { title: '个人中心' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router