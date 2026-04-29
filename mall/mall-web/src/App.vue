<script setup>
import { ref, onMounted } from 'vue'
import { fetchHomeContent } from './api/home'

const products = ref([
  { id: 1, name: 'Minimalist Ceramic Vase', price: '299', pic: 'https://images.unsplash.com/photo-1578500494198-246f612d3b3d?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Decor' },
  { id: 2, name: 'Linen Lounge Chair', price: '1899', pic: 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Furniture' },
  { id: 3, name: 'Matte Desk Lamp', price: '459', pic: 'https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Lighting' },
  { id: 4, name: 'Cotton Throw Blanket', price: '159', pic: 'https://images.unsplash.com/photo-1580136579312-94651dfd596d?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3', productCategoryName: 'Textiles' },
])

const banner = ref('https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=800&auto=format&fit=crop&q=80')

onMounted(async () => {
  try {
    const data = await fetchHomeContent()
    if (data.hotProductList && data.hotProductList.length > 0) {
      products.value = data.hotProductList
    }
    if (data.advertiseList && data.advertiseList.length > 0) {
      banner.value = data.advertiseList[0].pic
    }
  } catch (error) {
    console.error('Failed to fetch home content:', error)
  }
})
</script>

<template>
  <div class="app-layout">
    <!-- Navbar -->
    <nav class="navbar">
      <div class="container nav-content">
        <div class="logo">
          Mall
        </div>
        <ul class="nav-links">
          <li><a href="#" class="active">Home</a></li>
          <li><a href="#">Category</a></li>
          <li><a href="#">Brands</a></li>
          <li><a href="#">Cart</a></li>
          <li><a href="#">Profile</a></li>
        </ul>
        <button class="btn btn-outline login-btn">Sign In</button>
      </div>
    </nav>

    <!-- Hero Section -->
    <header class="hero container">
      <div class="hero-banner-wrapper">
        <img :src="banner" alt="Promotional Banner" class="hero-banner-image" />
      </div>
    </header>

    <!-- Products Section -->
    <main class="products-section container">
      <div class="section-header">
        <h2 class="section-title">New Arrivals</h2>
        <a href="#" class="view-all">View All →</a>
      </div>
      
      <div class="product-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-image-wrapper">
            <img :src="product.pic" :alt="product.name" class="product-image" />
          </div>
          <div class="product-info">
            <span class="category">{{ product.productCategoryName }}</span>
            <h3 class="product-name">{{ product.name }}</h3>
            <span class="price">￥{{ product.price }}</span>
          </div>
        </div>
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

/* Navbar */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border-color);
  padding: 1rem 0;
}

.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: -1px;
}

.nav-links {
  display: flex;
  gap: 2.5rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-links a {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.nav-links a.active, .nav-links a:hover {
  color: var(--text-primary);
  font-weight: 600;
}

/* Hero Section */
.hero {
  padding: 2rem 2rem 4rem 2rem;
  display: block;
}

.hero-banner-wrapper {
  width: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  background: #f4f4f5;
}

.hero-banner-image {
  width: 100%;
  height: auto;
  display: block;
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

.product-card {
  background: var(--surface-color);
  border-radius: var(--radius-md);
  padding: 1rem;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.product-image-wrapper {
  border-radius: var(--radius-sm);
  overflow: hidden;
  height: 280px;
  margin-bottom: 1.25rem;
  background: #f4f4f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.product-card:hover .product-image {
  transform: scale(1.03);
}

.product-info {
  text-align: center;
}

.category {
  font-size: 0.75rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.product-name {
  font-size: 1rem;
  font-weight: 500;
  margin: 0.5rem 0;
}

.price {
  font-size: 1.1rem;
  font-weight: 600;
}

.footer {
  margin-top: auto;
  text-align: center;
  padding: 2rem 0;
  border-top: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: 0.875rem;
}

@media (max-width: 900px) {
  .hero {
    padding: 2rem 1rem 3rem 1rem;
  }
  .nav-links {
    display: none;
  }
}
</style>
