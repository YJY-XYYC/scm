import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import request from '@/utils/request'

// Element Plus 主题配置
const elementPlusConfig = {
  size: 'default',
  zIndex: 3000,
  // 全局主题配置
}

// 全局错误处理增强
window.onerror = function(message, source, lineno, colno, error) {
  // 忽略已知的跟踪相关错误
  const ignorePatterns = [
    'v[w] is not a function',
    'zybTrackerStatisticsAction',
    'rumt-zh.com',
    'message channel closed',
    'A listener indicated an asynchronous response'
  ];
  
  if (message && ignorePatterns.some(pattern => message.includes(pattern))) {
    console.log('忽略已知的跟踪相关错误:', message);
    return true; // 阻止错误冒泡
  }
  // 记录其他错误
  console.error('全局错误捕获:', { message, source, lineno, colno, error });
  return false;
};

// 扩展忽略的Promise错误
window.addEventListener('unhandledrejection', function(event) {
  const message = event.reason?.message || String(event.reason);
  const ignorePatterns = [
    'message channel closed',
    'A listener indicated an asynchronous response',
    'channel closed before a response was received'
  ];
  
  if (ignorePatterns.some(pattern => message.includes(pattern))) {
    console.log('忽略已知的跟踪相关Promise错误:', message);
    event.preventDefault(); // 阻止默认处理
  }
});

// 拦截postMessage和消息通道相关操作
const originalPostMessage = window.postMessage;
window.postMessage = function(message, targetOrigin, transfer) {
  // 检查是否与跟踪脚本相关的消息
  const messageStr = String(message);
  if (messageStr.includes('hybridaction') || messageStr.includes('zybTracker') || messageStr.includes('rumt')) {
    console.log('拦截可疑的postMessage调用:', message);
    return;
  }
  return originalPostMessage.apply(this, arguments);
};

// 拦截MessageChannel创建
const originalMessageChannel = window.MessageChannel;
if (originalMessageChannel) {
  window.MessageChannel = function() {
    console.log('拦截MessageChannel创建');
    // 返回一个模拟的MessageChannel对象
    return {
      port1: {
        postMessage: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        start: () => {}
      },
      port2: {
        postMessage: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        start: () => {}
      }
    };
  };
  window.MessageChannel.prototype = originalMessageChannel.prototype;
}

// 拦截跟踪脚本加载 - 重写document.createElement
document.createElement = (function(originalCreateElement) {
  return function(tagName) {
    if (tagName.toLowerCase() === 'script') {
      // 创建一个包装的script元素
      const script = originalCreateElement.call(this, 'script');
      
      // 重写setAttribute方法
      const originalSetAttribute = script.setAttribute;
      script.setAttribute = function(name, value) {
        // 拦截包含跟踪相关关键词的脚本源
        if (name.toLowerCase() === 'src' && value) {
          if (value.includes('rumt-zh.com') || value.includes('hybridaction')) {
            console.log('拦截跟踪脚本:', value);
            return; // 阻止设置src属性
          }
        }
        return originalSetAttribute.call(this, name, value);
      };
      
      // 重写src属性的直接赋值
      Object.defineProperty(script, 'src', {
        set: function(value) {
          if (value && (value.includes('rumt-zh.com') || value.includes('hybridaction'))) {
            console.log('拦截跟踪脚本(src直接赋值):', value);
            return;
          }
          originalSetAttribute.call(this, 'src', value);
        },
        get: function() {
          return this.getAttribute('src');
        }
      });
      
      return script;
    }
    return originalCreateElement.call(this, tagName);
  };
})(document.createElement);

// 拦截DOM操作方法以阻止跟踪脚本注入
const originalAppendChild = Element.prototype.appendChild;
Element.prototype.appendChild = function(child) {
  // 检查是否是script元素且包含跟踪相关内容
  if (child.tagName && child.tagName.toLowerCase() === 'script') {
    const src = child.getAttribute('src');
    if (src && (src.includes('rumt-zh.com') || src.includes('hybridaction'))) {
      console.log('拦截跟踪脚本通过appendChild注入:', src);
      return child; // 返回但不实际添加
    }
  }
  return originalAppendChild.call(this, child);
};

const originalInsertBefore = Element.prototype.insertBefore;
Element.prototype.insertBefore = function(newNode, referenceNode) {
  // 检查是否是script元素且包含跟踪相关内容
  if (newNode.tagName && newNode.tagName.toLowerCase() === 'script') {
    const src = newNode.getAttribute('src');
    if (src && (src.includes('rumt-zh.com') || src.includes('hybridaction'))) {
      console.log('拦截跟踪脚本通过insertBefore注入:', src);
      return newNode; // 返回但不实际添加
    }
  }
  return originalInsertBefore.call(this, newNode, referenceNode);
};

// 模拟JSONP回调函数，防止"v[w] is not a function"错误
const originalEval = window.eval;
window.eval = function(code) {
  // 检查是否包含JSONP回调注册
  if (code.includes('zybTrackerStatisticsAction')) {
    console.log('拦截JSONP回调相关代码执行');
    return undefined;
  }
  return originalEval.call(this, code);
};

// 拦截JSONP回调函数的注册
Object.defineProperty(window, '__cb__zybTrackerStatisticsAction', {
  set: function() {
    console.log('拦截JSONP回调函数注册');
  },
  get: function() {
    return function() { console.log('模拟JSONP回调函数'); };
  }
});

// 拦截XMLHttpRequest请求
const originalXHROpen = XMLHttpRequest.prototype.open;
const originalXHRSend = XMLHttpRequest.prototype.send;

XMLHttpRequest.prototype.open = function(method, url) {
  // 保存请求的URL
  this._requestUrl = url;
  return originalXHROpen.apply(this, arguments);
};

XMLHttpRequest.prototype.send = function() {
  // 检查请求URL是否包含跟踪相关关键词
  if (this._requestUrl && (this._requestUrl.includes('hybridaction') || this._requestUrl.includes('rumt-zh.com'))) {
    console.log('拦截XMLHttpRequest请求:', this._requestUrl);
    
    // 创建一个自定义的事件对象，不直接修改XMLHttpRequest的只读属性
    const customEvent = {
      target: {
        status: 200,
        statusText: 'OK',
        readyState: 4,
        responseText: JSON.stringify({ success: true }),
        response: JSON.stringify({ success: true })
      }
    };
    
    // 触发事件处理程序
    setTimeout(() => {
      // 检查是否有onload事件处理程序
      if (this.onload) {
        try {
          this.onload(customEvent);
        } catch (e) {
          console.error('onload事件处理错误:', e);
        }
      }
      
      // 检查是否有onreadystatechange事件处理程序
      if (this.onreadystatechange) {
        try {
          this.onreadystatechange(customEvent);
        } catch (e) {
          console.error('onreadystatechange事件处理错误:', e);
        }
      }
    }, 0);
    
    // 阻止原始请求发送
    return;
  }
  
  // 正常发送其他请求
  return originalXHRSend.apply(this, arguments);
};

// 拦截fetch请求
const originalFetch = window.fetch;
window.fetch = function(url, options) {
  // 检查请求URL是否包含跟踪相关关键词
  if (url && (url.toString().includes('hybridaction') || url.toString().includes('rumt-zh.com'))) {
    console.log('拦截fetch请求:', url);
    // 返回模拟的成功响应
    return Promise.resolve(new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    }));
  }
  return originalFetch.apply(this, arguments);
};

// 拦截动态创建的script元素的执行
const originalWrite = document.write;
document.write = function(markup) {
  // 检查是否包含跟踪相关的script标签
  if (markup && (markup.includes('rumt-zh.com') || markup.includes('hybridaction'))) {
    console.log('拦截通过document.write注入的跟踪脚本');
    return;
  }
  return originalWrite.call(this, markup);
};

document.writeln = function(markup) {
  // 检查是否包含跟踪相关的script标签
  if (markup && (markup.includes('rumt-zh.com') || markup.includes('hybridaction'))) {
    console.log('拦截通过document.writeln注入的跟踪脚本');
    return;
  }
  return document.write(markup + '\n');
};

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局配置
app.config.globalProperties.$request = request

// 使用插件
app.use(pinia)
app.use(router)
app.use(ElementPlus, elementPlusConfig)

app.mount('#app')