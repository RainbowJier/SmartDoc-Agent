<template>
  <div ref="nfPage" class="not-found-page">
    <div ref="nfContent" class="nf-content">
      <div class="nf-icon">
        <svg fill="none" height="20" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
             stroke-width="1.8"
             viewBox="0 0 24 24" width="20">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" x2="12" y1="8" y2="12"/>
          <line x1="12" x2="12.01" y1="16" y2="16"/>
        </svg>
      </div>
      <span class="nf-code">404</span>
      <h2 class="nf-title">Page not found</h2>
      <p class="nf-desc">The page you're looking for doesn't exist or has been moved.</p>
      <router-link :to="{ name: 'chat' }" class="nf-link">
        <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
             stroke-width="2"
             viewBox="0 0 24 24" width="14">
          <line x1="19" x2="5" y1="12" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
        <span>Back to Chat</span>
      </router-link>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, onMounted, onUnmounted} from 'vue'
import {gsap} from 'gsap'

const nfPage = ref<HTMLElement>()
const nfContent = ref<HTMLElement>()
let ctx: gsap.Context | null = null

onMounted(() => {
  ctx = gsap.context(() => {
    const tl = gsap.timeline({defaults: {ease: 'power3.out', duration: 0.5}})
    tl.from('.nf-icon', {autoAlpha: 0, scale: 0.6, duration: 0.4}, 0)
        .from('.nf-code', {autoAlpha: 0, y: 16}, 0.15)
        .from('.nf-title', {autoAlpha: 0, y: 12}, 0.25)
        .from('.nf-desc', {autoAlpha: 0, y: 12}, 0.32)
        .from('.nf-link', {autoAlpha: 0, y: 10}, 0.4)
  }, nfPage.value)
})

onUnmounted(() => {
  ctx?.revert()
})
</script>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100vh;
  background: var(--surface-canvas);
}

.nf-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 360px;
}

.nf-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--surface-card);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-24);
  color: var(--color-graphite);
  border: 1px solid var(--color-silver-mist);
}

.nf-code {
  font-family: var(--font-text);
  font-size: 80px;
  font-weight: var(--weight-bold);
  letter-spacing: -0.04em;
  line-height: 0.9;
  color: var(--color-ink);
  margin-bottom: var(--spacing-12);
}

.nf-title {
  font-family: var(--font-text);
  font-size: var(--text-heading-sm);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-heading-sm);
  color: var(--color-ink);
  margin-bottom: var(--spacing-8);
}

.nf-desc {
  font-size: var(--text-body-sm);
  color: var(--color-graphite);
  line-height: 1.6;
  margin-bottom: var(--spacing-32);
}

.nf-link {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-8);
  padding: var(--spacing-10) var(--spacing-20);
  background: var(--color-azure);
  border: none;
  border-radius: var(--radius-pill);
  color: var(--color-snow);
  font-family: var(--font-text);
  font-weight: var(--weight-medium);
  font-size: var(--text-body-sm);
  text-decoration: none;
  cursor: pointer;
  transition: opacity var(--duration-fast) var(--ease-primary);
}

.nf-link:hover {
  opacity: 0.85;
}
</style>
