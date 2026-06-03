<script setup>
import { ref } from 'vue'

const props = defineProps({
  banner: {
    type: String,
    required: true
  }
})

const imageLoaded = ref(false)
const handleImageLoad = () => {
  imageLoaded.value = true
}
</script>

<template>
  <header class="hero container" role="banner">
    <div class="hero-banner-wrapper">
      <div v-if="!imageLoaded" class="image-placeholder" role="status" aria-label="图片加载中"></div>
      <img 
        :src="banner" 
        alt="促销横幅" 
        class="hero-banner-image"
        :class="{ 'loaded': imageLoaded }"
        @load="handleImageLoad"
        loading="lazy"
      />
    </div>
  </header>
</template>

<style scoped>
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
  position: relative;
  min-height: 300px;
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

.hero-banner-image {
  width: 100%;
  height: auto;
  display: block;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.hero-banner-image.loaded {
  opacity: 1;
}

@media (max-width: 900px) {
  .hero {
    padding: 2rem 1rem 3rem 1rem;
  }
}
</style>
