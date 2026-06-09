<template>
  <div ref="homeRef" class="kb-home">
    <div class="home-hero">
      <div class="hero-icon">
        <svg fill="none" height="22" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
             stroke-width="1.5" viewBox="0 0 24 24" width="22">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
        </svg>
      </div>
      <h1 class="hero-title">Knowledge Base</h1>
      <p class="hero-sub">Browse categories or ask AI anything about your knowledge base</p>
    </div>

    <section class="home-section">
      <h2 class="section-title">Categories</h2>
      <div class="category-grid">
        <button
            v-for="cat in categories"
            :key="cat.id"
            class="cat-card"
            @click="$emit('select-category', cat.id)"
        >
          <div class="cat-card-icon" v-html="cat.icon"/>
          <div class="cat-card-body">
            <span class="cat-card-name">{{ cat.name }}</span>
            <span class="cat-card-desc">{{ cat.description }}</span>
          </div>
          <span class="cat-card-count">{{ cat.docCount }}</span>
        </button>
      </div>
    </section>

    <section v-if="recentDocs.length > 0" class="home-section">
      <h2 class="section-title">Recent Documents</h2>
      <div class="doc-list">
        <button
            v-for="doc in recentDocs"
            :key="doc.id"
            class="doc-row"
            @click="$emit('select-doc', doc.id)"
        >
          <svg fill="none" height="12" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
               stroke-width="1.8" viewBox="0 0 24 24" width="12">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          <div class="doc-row-body">
            <span class="doc-row-title">{{ doc.title }}</span>
            <span class="doc-row-summary">{{ doc.summary }}</span>
          </div>
          <span class="doc-row-meta">{{ formatTime(doc.updatedAt) }}</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script lang="ts" setup>
import {ref, onMounted, onUnmounted} from 'vue'
import {gsap} from 'gsap'
import type {KbCategory, KbDocument} from '@/types'

defineProps<{
  categories: KbCategory[]
  recentDocs: KbDocument[]
}>()

defineEmits<{
  'select-category': [id: string]
  'select-doc': [id: string]
}>()

const homeRef = ref<HTMLElement>()
let ctx: gsap.Context | null = null

function formatTime(ts: number): string {
  const d = new Date(ts)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`
  return d.toLocaleDateString()
}

onMounted(() => {
  ctx = gsap.context(() => {
    const tl = gsap.timeline({defaults: {ease: 'power3.out', duration: 0.45}})
    tl.from('.hero-icon', {autoAlpha: 0, scale: 0.7, duration: 0.35}, 0)
        .from('.hero-title', {autoAlpha: 0, y: 14}, 0.12)
        .from('.hero-sub', {autoAlpha: 0, y: 10}, 0.2)
        .from('.cat-card', {autoAlpha: 0, y: 16, stagger: 0.06}, 0.3)
        .from('.doc-row', {autoAlpha: 0, y: 12, stagger: 0.04}, 0.45)
  }, homeRef.value)
})

onUnmounted(() => {
  ctx?.revert()
})
</script>

<style scoped>
.kb-home {
  max-width: 720px;
  margin: 0 auto;
  padding: 48px 32px 80px;
}

.home-hero {
  text-align: center;
  margin-bottom: 52px;
  animation: fadeInUp 0.5s ease both;
}

.hero-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  background: #f0f0f2;
  border-radius: 16px;
  color: #1d1d1f;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #1d1d1f;
  margin-bottom: 10px;
  line-height: 1.15;
}

.hero-sub {
  font-size: 15px;
  color: #86868b;
  line-height: 1.5;
  max-width: 400px;
  margin: 0 auto;
}

.home-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #aeaeb2;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 14px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}

.cat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e5e5e7;
  border-radius: 14px;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: all 0.15s ease;
}

.cat-card:hover {
  border-color: #d2d2d7;
  background: #fafafa;
  transform: translateY(-1px);
}

.cat-card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #f0f0f2;
  flex-shrink: 0;
  font-size: 16px;
}

.cat-card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cat-card-name {
  font-size: 13px;
  font-weight: 600;
  color: #1d1d1f;
}

.cat-card-desc {
  font-size: 11px;
  color: #86868b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat-card-count {
  font-size: 10px;
  font-weight: 500;
  color: #aeaeb2;
  background: #f0f0f2;
  padding: 1px 8px;
  border-radius: 999px;
  flex-shrink: 0;
}

.doc-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.doc-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px 14px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #474747;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: all 0.1s ease;
}

.doc-row:hover {
  background: #f0f0f2;
  color: #1d1d1f;
}

.doc-row-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.doc-row-title {
  font-size: 13px;
  font-weight: 500;
  color: inherit;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-row-summary {
  font-size: 11px;
  color: #86868b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-row-meta {
  font-size: 10px;
  color: #aeaeb2;
  flex-shrink: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
