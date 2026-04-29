<script lang="ts" setup>
import { computed } from 'vue'
import { SwitchButton } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb/index.vue'
import Hamburger from '@/components/Hamburger/index.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

// 定义组件名称
defineOptions({
  name: 'Navbar'
})

const appStore = useAppStore()
const userStore = useUserStore()

const sidebar = computed(() => appStore.sidebar)
const userInfo = computed(() => userStore.userInfo)
const avatar = computed(() => userInfo.value.avatar)
const username = computed(() => userInfo.value.username)
const roles = computed(() => userInfo.value.roles)

// 判断是否有有效的头像 URL
const hasValidAvatar = computed(() => {
  return avatar.value && avatar.value.trim() !== '' && avatar.value !== 'null'
})

// 头像加载失败时的降级处理
const avatarLoadError = (e: Event) => {
  const target = e.target as HTMLImageElement
  if (target) {
    target.style.display = 'none'
  }
}

// 处理开关侧边栏操作
const handleToggleSideBar = () => {
  appStore.toggleSideBar()
}

// 处理用户登出
const handleLogout = async () => {
  await userStore.userLogout()
  // 为了重新实例化vue-router对象 避免bug
  location.reload()
}
</script>

<template>
  <div class="navbar-wrapper">
    <el-menu class="navbar" mode="horizontal">
      <hamburger class="hamburger-container" :toggle-click="handleToggleSideBar" :is-active="sidebar.opened"></hamburger>
      <div class="breadcrumb-container">
        <breadcrumb></breadcrumb>
      </div>
    </el-menu>
    
    <!-- 用户信息区域（独立于 el-menu） -->
    <div class="user-info-container">
      <!-- 用户名和角色直接显示 -->
      <div class="user-info-display">
        <span class="username">{{ username || '未登录' }}</span>
        <el-tag size="small" type="primary" effect="plain">{{ roles?.[0] || '管理员' }}</el-tag>
      </div>
      
      <!-- 头像（后端icon或首字母）+ 退出登录 -->
      <div class="avatar-section">
        <el-avatar 
          v-if="hasValidAvatar" 
          :size="32" 
          :src="avatar" 
          class="user-avatar"
          @error="avatarLoadError"
        >
          {{ username?.charAt(0)?.toUpperCase() || 'U' }}
        </el-avatar>
        <el-avatar v-else :size="32" class="user-avatar">
          {{ username?.charAt(0)?.toUpperCase() || 'U' }}
        </el-avatar>
        <el-button 
          type="danger" 
          link 
          size="small" 
          @click="handleLogout"
          class="logout-btn"
        >
          <el-icon><SwitchButton /></el-icon>
          退出
        </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.navbar-wrapper {
  display: flex;
  align-items: center;
  height: 50px;
  position: relative;
}

.navbar {
  height: 50px;
  line-height: 50px;
  border-radius: 0px !important;
  display: flex;
  align-items: center;
  padding: 0 20px;
  position: relative;
  z-index: 1;
  flex: 1;

  .hamburger-container {
    line-height: 58px;
    height: 50px;
    flex-shrink: 0;
    padding: 0 10px;
  }

  .breadcrumb-container {
    flex: 1;
    margin-left: 10px;
    overflow: hidden;
  }
}

.user-info-container {
  flex-shrink: 0;
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 20px;
  height: 50px;

  .user-info-display {
    display: flex;
    align-items: center;
    gap: 8px;

    .username {
      font-size: 14px;
      color: #303133;
      font-weight: 500;
    }
  }

  .avatar-section {
    display: flex;
    align-items: center;
    gap: 10px;

    .user-avatar {
      background-color: #409EFF;
      color: #fff;
      font-weight: 500;
    }

    .logout-btn {
      color: #F56C6C;
      font-weight: 500;
      padding: 0;
      
      &:hover {
        color: #F2403F;
      }
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .navbar-wrapper {
    padding: 0 10px;
  }
  
  .navbar {
    .breadcrumb-container {
      display: none;
    }
  }

  .user-info-container {
    padding: 0 10px;
    
    .user-info-display {
      display: none;
    }
  }
}
</style>
