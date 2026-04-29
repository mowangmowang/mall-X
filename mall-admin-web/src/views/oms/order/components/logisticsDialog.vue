<script setup lang="ts">
import { ref, computed } from 'vue'

// 定义组件的props
const props = defineProps({
  modelValue: Boolean,
  orderStatus: {
    type: Number,
    default: 0
  },
  deliveryTime: {
    type: String,
    default: ''
  }
})

// 定义组件的emits
const emit = defineEmits(['update:modelValue'])

// 根据订单状态生成物流记录
const generateLogisticsList = () => {
  const now = new Date()
  const baseLogs = [
    { name: '订单已提交', time: formatDate(new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000)) },
    { name: '订单付款成功', time: formatDate(new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000)) }
  ]
  
  // 已发货或已完成状态，添加物流信息
  if (props.orderStatus >= 2) {
    const deliveryDate = props.deliveryTime || formatDate(new Date(now.getTime() - 12 * 60 * 60 * 1000))
    baseLogs.push(
      { name: '快件已由【顺丰快递】揽收', time: deliveryDate },
      { name: '快件运输中', time: formatDate(new Date(now.getTime() - 6 * 60 * 60 * 1000)) },
      { name: '快件已到达目的地网点', time: formatDate(new Date(now.getTime() - 2 * 60 * 60 * 1000)) }
    )
  }
  
  // 已完成状态，添加签收信息
  if (props.orderStatus === 3) {
    baseLogs.push({ name: '订单已签收，期待再次为您服务', time: formatDate(now) })
  }
  
  return baseLogs
}

// 格式化日期
const formatDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 物流列表数据
const logisticsList = ref(generateLogisticsList())

// 控制对话框显示的计算属性
const visible = computed({
  get() {
    return props.modelValue
  },
  set(visible) {
    emit('update:modelValue', visible)
  }
})

// 发出输入事件
const emitInput = (val: boolean) => {
  emit('update:modelValue', val)
}

// 处理关闭对话框
const handleClose = () => {
  emitInput(false)
}
</script>

<template>
  <el-dialog title="订单跟踪" v-model="visible" :before-close="handleClose" width="40%">
    <el-steps direction="vertical" :active="6" finish-status="success" space="50px">
      <el-step v-for="item in logisticsList" :key="item.name" :title="item.name" :description="item.time"></el-step>
    </el-steps>
  </el-dialog>
</template>

<style></style>
