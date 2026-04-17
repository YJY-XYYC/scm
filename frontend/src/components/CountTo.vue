<template>
  <span class="count-to-span">{{ displayValue }}</span>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  startVal: {
    type: Number,
    default: 0
  },
  endVal: {
    type: Number,
    default: 0
  },
  duration: {
    type: Number,
    default: 2000
  },
  decimals: {
    type: Number,
    default: 0
  },
  prefix: {
    type: String,
    default: ''
  }
})

const displayValue = ref(props.startVal)
let animationFrame = null
let startTime = null

const animate = (timestamp) => {
  if (!startTime) startTime = timestamp
  const progress = timestamp - startTime
  
  if (progress < props.duration) {
    const currentValue = props.startVal + (props.endVal - props.startVal) * (progress / props.duration)
    displayValue.value = props.prefix + currentValue.toFixed(props.decimals)
    animationFrame = requestAnimationFrame(animate)
  } else {
    displayValue.value = props.prefix + props.endVal.toFixed(props.decimals)
  }
}

const startAnimation = () => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
  startTime = null
  animationFrame = requestAnimationFrame(animate)
}

watch(() => props.endVal, () => {
  startAnimation()
})

onMounted(() => {
  startAnimation()
})
</script>

<style scoped>
:deep(span) {
  color: white !important;
  font-weight: normal;
  text-decoration: none;
}

/* 添加全局样式确保在任何地方都生效 */
</style>

<style>
.count-to-span {
  color: white !important;
}
</style>