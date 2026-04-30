<script setup>
import { ref } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const imageLoaded = ref(false)
const handleImageLoad = () => {
  imageLoaded.value = true
}
</script>

<template>
  <article class="product-card" role="article" :aria-label="`产品：${product.name}`">
    <div class="product-image-wrapper">
      <div v-if="!imageLoaded" class="image-placeholder" role="status" aria-label="图片加载中"></div>
      <img 
        :src="product.pic" 
        :alt="product.name" 
        class="product-image" 
        :class="{ 'loaded': imageLoaded }"
        @load="handleImageLoad"
        loading="lazy"
      />
    </div>
    <div class="product-info">
      <span class="category">{{ product.productCategoryName }}</span>
      <h3 class="product-name">{{ product.name }}</h3>
      <span class="price" aria-label="价格">￥{{ product.price }}</span>
    </div>
  </article>
</template>

<style scoped>
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
  position: relative;
}

.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal), opacity var(--transition-normal);
  opacity: 0;
}

.product-image.loaded {
  opacity: 1;
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
</style>
