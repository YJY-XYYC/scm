<template>
  <div class="image-preview-wrapper">
    <!-- 使用插槽接收需要预览的内容 -->
    <div class="preview-trigger" @click="showPreview">
      <slot></slot>
    </div>
    
    <!-- 预览对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="title"
      width="90%"
      center
      destroy-on-close
      @close="handleClose"
    >
      <div class="preview-container">
        <img :src="previewUrl" class="preview-image" alt="预览图片" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ImagePreview',
  props: {
    // 预览图片的URL
    src: {
      type: String,
      required: true
    },
    // 预览对话框标题
    title: {
      type: String,
      default: '图片预览'
    }
  },
  data() {
    return {
      dialogVisible: false,
      // 存储处理后的预览URL
      previewUrl: ''
    }
  },
  watch: {
    // 监听src变化，更新预览URL
    src(newVal) {
      this.previewUrl = this.processImageUrl(newVal)
    }
  },
  mounted() {
    // 初始化预览URL
    this.previewUrl = this.processImageUrl(this.src)
  },
  methods: {
    // 显示预览
    showPreview() {
      this.dialogVisible = true
      // 防止预览时页面滚动
      document.body.style.overflow = 'hidden'
    },
    // 关闭预览
    handleClose() {
      this.dialogVisible = false
      // 恢复页面滚动
      document.body.style.overflow = ''
    },
    // 处理图片URL，确保可以正确预览
    processImageUrl(url) {
      // 如果是相对路径，确保路径正确
      if (url && url.startsWith('/api')) {
        return url
      }
      return url
    }
  }
}
</script>

<style scoped>
.image-preview-wrapper {
  display: inline-block;
  position: relative;
}

.preview-trigger {
  cursor: pointer;
  display: inline-block;
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

.preview-trigger:hover::before {
  content: '';
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid rgb(255, 255, 255);
  margin-bottom: 1px;
  z-index: 1000;
}

/* 对话框背景设置为透明 */
:deep(.el-dialog) {
  background-color: transparent;
  box-shadow: none;
}

:deep(.el-dialog__body) {
  background-color: transparent;
  padding: 0;
}

:deep(.el-dialog__header) {
  background-color: transparent;
  color: white;
  padding: 10px 10px 0;
}

/* 对话框标题文字设置为白色 */
:deep(.el-dialog__title) {
  color: white;
}

:deep(.el-dialog__close) {
  /* background-color: #48494c; */
  color: white;
  font-size: 40px;
  width: 40px;
  height: 40px;
  line-height: 40px;
  border-radius: 50%;
}

:deep(.el-dialog__headerbtn) {
  width: 40px;
  height: 40px;
  margin-top: 10px;
  margin-right: 100px;
  position: absolute;
  top: 0;
  right: 0;
}
</style>