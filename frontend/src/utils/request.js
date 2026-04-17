import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 15000,
  withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 阻止跟踪请求
    const url = config.url || ''
    if (url.includes('zybTrackerStatisticsAction') || url.includes('rumt-zh.com')) {
      console.log('阻止跟踪请求:', url)
      return Promise.reject(new Error('Blocked tracking request'))
    }
    
    // 添加请求调试日志
    console.log('API请求:', {
      url: url,
      method: config.method,
      params: config.params,
      data: config.data
    })
    
    // 只有当请求体不是FormData时才设置Content-Type为application/json
    if (config.method === 'post' && !(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json'
    }
    
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    console.log('API响应:', {
      url: response.config.url,
      method: response.config.method,
      code: res.code,
      message: res.message,
      data: res.data
    })
    
    if (res.code !== 200) {
      console.error('响应错误详细信息:', {
        url: response.config.url,
        method: response.config.method,
        status: response.status,
        statusText: response.statusText,
        code: res.code,
        message: res.message,
        requestData: response.config.data
      })
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  error => {
    console.error('网络请求错误详细信息:', {
      config: error.config,
      response: error.response ? {
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data
      } : '无响应',
      message: error.message,
      stack: error.stack
    })
    
    // 根据不同的错误类型显示不同的错误消息
    let errorMsg = '网络错误'
    if (error.response) {
      // 服务器返回错误
      errorMsg = error.response.data?.message || error.response.statusText || '服务器错误'
    } else if (error.request) {
      // 请求已发出但未收到响应
      errorMsg = '服务器无响应，请检查网络连接'
    } else {
      // 请求配置出错
      errorMsg = error.message || '请求错误'
    }
    
    ElMessage.error(errorMsg)
    return Promise.reject(error)
  }
)

export default request