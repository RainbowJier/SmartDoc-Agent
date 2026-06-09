<template>
  <div ref="kpRef" class="knowledge-portal">

    <!-- ── Top Bar ──────────────────────────── -->
    <header ref="topbarRef" class="topbar">
      <div class="topbar-left">
        <div class="topbar-brand">
          <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
               stroke-width="2" viewBox="0 0 24 24" width="14">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
          <span class="topbar-brand-text">SmartDoc</span>
        </div>
        <div class="topbar-search">
          <svg class="search-icon" fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2"
               viewBox="0 0 24 24" width="13">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" x2="16.65" y1="21" y2="16.65"/>
          </svg>
          <input
              ref="searchInputRef"
              v-model="searchQuery"
              class="search-field"
              placeholder="Search knowledge base..."
              @blur="searchFocused = false"
              @focus="searchFocused = true"
              @keydown.enter="handleSearch"
          />
          <button v-if="searchQuery" class="search-clear" @click="searchQuery = ''">
            <svg fill="none" height="10" stroke="currentColor" stroke-linecap="round" stroke-width="2"
                 viewBox="0 0 24 24" width="10">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
          </button>
        </div>
      </div>
      <div class="topbar-right">

        <button class="topbar-action" @click="handleNew">
          <svg fill="none" height="12" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
               width="12">
            <line x1="12" x2="12" y1="5" y2="19"/>
            <line x1="5" x2="19" y1="12" y2="12"/>
          </svg>
          <span>New</span>
        </button>
      </div>
    </header>

    <!-- ── Body ─────────────────────────────── -->
    <div class="body-layout">
      <KbSidebar
          :active-category="activeCategoryId"
          :active-mode="activeMode"
          :active-session-id="chatStore.currentSessionId"
          :categories="categories"
          :sessions="chatStore.sessions"
          @navigate="setMode"
          @select-category="handleSelectCategory"
          @select-session="handleSelectSession"
          @delete-session="handleDeleteSession"
          @new-session="handleNewSession"
          @clear-all-sessions="handleClearAllSessions"
      />

      <div class="content-col">
        <main class="content-scroll">
          <KbHome
              v-if="activeMode === 'home'"
              :categories="categories"
              :recent-docs="recentDocs"
              @select-category="handleSelectCategory"
              @select-doc="handleSelectDoc"
          />

          <KbAskView
              v-else-if="activeMode === 'ask'"
          />

          <div v-else-if="activeMode === 'search'" class="mode-placeholder">
            <div class="placeholder-icon">
              <svg fill="none" height="20" stroke="currentColor" stroke-linecap="round" stroke-width="2"
                   viewBox="0 0 24 24" width="20">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" x2="16.65" y1="21" y2="16.65"/>
              </svg>
            </div>
            <h2 class="placeholder-title">Search Results</h2>
            <p class="placeholder-sub">Search for "{{ searchQuery }}" will appear here</p>
          </div>

          <div v-else-if="activeMode === 'doc'" class="mode-placeholder">
            <div class="placeholder-icon">
              <svg fill="none" height="20" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                   stroke-width="1.8" viewBox="0 0 24 24" width="20">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" x2="8" y1="13" y2="13"/>
                <line x1="16" x2="8" y1="17" y2="17"/>
              </svg>
            </div>
            <h2 class="placeholder-title">Document View</h2>
            <p class="placeholder-sub">Document content will be rendered here with full markdown support</p>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, onMounted, onUnmounted} from 'vue'
import {gsap} from 'gsap'
import KbSidebar from '@/components/KbSidebar.vue'
import KbHome from '@/components/KbHome.vue'
import KbAskView from '@/components/KbAskView.vue'
import {useChatStore} from '@/stores/chat'
import type {KbCategory, KbDocument} from '@/types'

const chatStore = useChatStore()
const searchQuery = ref('')
const searchFocused = ref(false)
const activeMode = ref('home')
const activeCategoryId = ref('')
const searchInputRef = ref<HTMLInputElement>()

const categories: KbCategory[] = [
  {id: 'guides', name: 'Guides', icon: '📘', description: 'Getting started, setup, configuration guides', docCount: 12},
  {id: 'api-ref', name: 'API Reference', icon: '🔗', description: 'Endpoint documentation and parameters', docCount: 28},
  {
    id: 'best-practices',
    name: 'Best Practices',
    icon: '⭐',
    description: 'Patterns, conventions, anti-patterns',
    docCount: 15
  },
  {id: 'tutorials', name: 'Tutorials', icon: '📖', description: 'Step-by-step walkthroughs', docCount: 9},
  {id: 'security', name: 'Security', icon: '🔒', description: 'Authentication, authorization, encryption', docCount: 11},
  {id: 'deployment', name: 'Deployment', icon: '🚀', description: 'CI/CD, Docker, cloud deployment', docCount: 7},
]

const recentDocs: KbDocument[] = [
  {
    id: 'd1',
    title: 'Spring Boot Project Setup Guide',
    summary: 'How to initialize a new Spring Boot project with Maven',
    categoryId: 'guides',
    tags: [],
    updatedAt: Date.now() - 3600000
  },
  {
    id: 'd2',
    title: 'JWT Authentication Implementation',
    summary: 'Complete JWT token-based auth with Spring Security',
    categoryId: 'security',
    tags: [],
    updatedAt: Date.now() - 7200000
  },
  {
    id: 'd3',
    title: 'REST API Design Best Practices',
    summary: 'Naming conventions, status codes, versioning strategies',
    categoryId: 'best-practices',
    tags: [],
    updatedAt: Date.now() - 86400000
  },
  {
    id: 'd4',
    title: 'Docker Compose for Spring Boot',
    summary: 'Containerize your Spring Boot app with Docker',
    categoryId: 'deployment',
    tags: [],
    updatedAt: Date.now() - 172800000
  },
]

function setMode(mode: string) {
  if (mode === 'ask') {
    chatStore.createSession()
  }
  activeMode.value = mode
  if (mode === 'home') {
    activeCategoryId.value = ''
  }
}

function handleSearch() {
  const q = searchQuery.value.trim()
  if (!q) return
  activeMode.value = 'search'
}

function handleSelectCategory(id: string) {
  activeCategoryId.value = id
  activeMode.value = 'search'
  searchQuery.value = categories.find(c => c.id === id)?.name || ''
}

function handleSelectDoc(id: string) {
  activeMode.value = 'doc'
}

function handleNew() {
  chatStore.createSession()
  activeMode.value = 'ask'
}

async function handleSelectSession(id: string) {
  await chatStore.selectSession(id)
  activeMode.value = 'ask'
}

async function handleDeleteSession(id: string) {
  await chatStore.deleteSession(id)
}

function handleNewSession() {
  chatStore.createSession()
  activeMode.value = 'ask'
}

async function handleClearAllSessions() {
  await chatStore.clearAllSessions()
  chatStore.createSession()
}

const kpRef = ref<HTMLElement>()
const topbarRef = ref<HTMLElement>()
let kpCtx: gsap.Context | null = null

onMounted(async () => {
  await chatStore.loadSessions()
  if (chatStore.sessions.length === 0) {
    chatStore.createSession()
  }

  kpCtx = gsap.context(() => {
    const tl = gsap.timeline({defaults: {ease: 'power2.out'}})
    tl.from('.topbar', {y: -16, autoAlpha: 0, duration: 0.35}, 0)
        .from('.kb-sidebar', {x: -16, autoAlpha: 0, duration: 0.4}, 0.1)
        .from('.content-col', {autoAlpha: 0, y: 12, duration: 0.4}, 0.15)
  }, kpRef.value)
})

onUnmounted(() => {
  kpCtx?.revert()
})
</script>

<style scoped>
.knowledge-portal {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #ffffff;
  color: #1d1d1f;
}

/* ── Top Bar ──────────────────── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #f5f5f7;
  border-bottom: 1px solid #e5e5e7;
  flex-shrink: 0;
  z-index: 20;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.topbar-brand {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #1d1d1f;
  flex-shrink: 0;
}

.topbar-brand-text {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.topbar-search {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  max-width: 360px;
  padding: 5px 10px;
  background: #e8e8ed;
  border-radius: 8px;
  transition: background 0.12s ease;
}

.topbar-search:focus-within {
  background: #ffffff;
  box-shadow: 0 0 0 1px #aeaeb2;
}

.search-icon {
  color: #86868b;
  flex-shrink: 0;
}

.search-field {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-family: inherit;
  font-size: 12px;
  color: #1d1d1f;
  min-width: 0;
}

.search-field::placeholder {
  color: #aeaeb2;
}

.search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border: none;
  border-radius: 50%;
  background: #aeaeb2;
  color: #ffffff;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.1s ease;
}

.search-clear:hover {
  background: #86868b;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.topbar-action {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  font-family: inherit;
  font-size: 11px;
  font-weight: 500;
  color: #86868b;
  background: transparent;
  border: 1px solid #d2d2d7;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.topbar-action:hover {
  color: #1d1d1f;
  border-color: #aeaeb2;
  background: #e8e8ed;
}

.topbar-action.active {
  color: #ffffff;
  background: #1d1d1f;
  border-color: #1d1d1f;
}

/* ── Body Layout ──────────────── */
.body-layout {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

.content-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.content-scroll {
  flex: 1;
  overflow-y: auto;
}

/* ── Placeholder Pages ────────── */
.mode-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 100%;
  padding: 60px 32px;
  animation: fadeInUp 0.4s ease both;
}

.placeholder-icon {
  width: 52px;
  height: 52px;
  background: #f0f0f2;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
  margin-bottom: 20px;
}

.placeholder-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.36px;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.placeholder-sub {
  font-size: 14px;
  color: #86868b;
  line-height: 1.5;
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
