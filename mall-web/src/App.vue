<script setup>
import { ref, onMounted } from 'vue'
import { fetchHomeContent } from './api/home'
import NavBar from './components/NavBar.vue'
import HeroBanner from './components/HeroBanner.vue'
import ProductCard from './components/ProductCard.vue'

const products = ref([
  { id: 1, name: 'Minimalist Ceramic Vase', price: '299', pic: 'https://images.unsplash.com/photo-1578500494198-246f612d3b3d?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Decor' },
  { id: 2, name: 'Linen Lounge Chair', price: '1899', pic: 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Furniture' },
  { id: 3, name: 'Matte Desk Lamp', price: '459', pic: 'https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Lighting' },
  { id: 4, name: 'Cotton Throw Blanket', price: '159', pic: 'https://images.unsplash.com/photo-1580136579312-94651dfd596d?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Textiles' },
])

const banner = ref('https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=800&auto=format&fit=crop&q=80')
const loading = ref(false)
const error = ref(null)

const loadHomeContent = async () => {
  loading.value = true
  error.value = null
  try {
    const data = await fetchHomeContent()
    if (data.hotProductList && data.hotProductList.length > 0) {
      products.value = data.hotProductList
    }
    if (data.advertiseList && data.advertiseList.length > 0) {
      banner.value = data.advertiseList[0].pic
    }
  } catch (err) {
    error.value = '加载失败，请稍后重试'
    console.error('Failed to fetch home content:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHomeContent()
})
</script>

<template>
  <div class="app-layout">
    <!-- Navbar -->
    <NavBar />

    <!-- Hero Section -->
    <HeroBanner :banner="banner" />

    <!-- Loading State -->
    <div v-if="loading" class="loading-container" role="status" aria-live="polite">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container" role="alert" aria-live="assertive">
      <p>{{ error }}</p>
      <button @click="loadHomeContent" class="btn">重试</button>
    </div>

    <!-- Products Section -->
    <main v-else class="products-section container" role="main">
      <div class="section-header">
        <h2 class="section-title">New Arrivals</h2>
        <a href="#" class="view-all" aria-label="查看全部产品">View All →</a>
      </div>
      
      <div class="product-grid" role="list">
        <ProductCard v-for="product in products" :key="product.id" :product="product" />
      </div>
    </main>
    
    <!-- Footer placeholder -->
    <footer class="footer container">
      <p>&copy; 2026 Mall Web. Designed for Simplicity.</p>
    </footer>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Products */
.products-section {
  padding-bottom: 6rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 2.5rem;
}

.section-title {
  font-size: 2rem;
}

.view-all {
  font-size: 0.9rem;
  font-weight: 600;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 2.5rem;
}

.footer {
  margin-top: auto;
  text-align: center;
  padding: 2rem 0;
  border-top: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: 0.875rem;
}

/* Loading State */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  min-height: 300px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color);
  border-top: 3px solid var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Error State */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  min-height: 300px;
  text-align: center;
}

.error-container p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}
</style>
