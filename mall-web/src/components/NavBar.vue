<script setup>
defineProps({
  navLinks: {
    type: Array,
    default: () => [
      { name: 'Home', href: '#', active: true },
      { name: 'Category', href: '#', active: false },
      { name: 'Brands', href: '#', active: false },
      { name: 'Cart', href: '#', active: false },
      { name: 'Profile', href: '#', active: false }
    ]
  }
})
</script>

<template>
  <nav class="navbar" role="navigation" aria-label="主导航">
    <div class="container nav-content">
      <div class="logo" role="banner">
        Mall
      </div>
      <ul class="nav-links" role="menubar">
        <li v-for="(link, index) in navLinks" :key="index" role="none">
          <a 
            :href="link.href" 
            :class="{ active: link.active }"
            role="menuitem"
            :aria-current="link.active ? 'page' : undefined"
          >
            {{ link.name }}
          </a>
        </li>
      </ul>
      <button class="btn btn-outline login-btn" aria-label="登录">Sign In</button>
    </div>
  </nav>
</template>

<style scoped>
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

@media (max-width: 900px) {
  .nav-links {
    display: none;
  }
}
</style>
