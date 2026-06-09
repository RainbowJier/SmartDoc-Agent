<template>
  <aside class="kb-sidebar">
    <div class="sidebar-section">
      <button
          :class="{ active: activeMode === 'home' }"
          class="nav-item"
          @click="$emit('navigate', 'home')"
      >
        <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
             stroke-width="1.8" viewBox="0 0 24 24" width="14">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        <span>Home</span>
      </button>
    </div>

    <div class="sidebar-divider"/>

    <div class="sidebar-section">
      <span class="section-label">Categories</span>
      <div class="category-list">
        <button
            v-for="cat in categories"
            :key="cat.id"
            :class="{ active: activeCategory === cat.id }"
            class="cat-item"
            @click="$emit('select-category', cat.id)"
        >
          <span class="cat-icon" v-html="cat.icon"/>
          <span class="cat-name">{{ cat.name }}</span>
          <span class="cat-count">{{ cat.docCount }}</span>
        </button>
      </div>
    </div>

    <div class="sidebar-divider"/>

    <!-- 会话管理 section — always visible -->
    <div class="sidebar-section">
      <div class="section-header">
        <span class="section-label">会话管理</span>
        <div class="section-actions" @click.stop>
          <button class="action-icon-btn" title="New session" @click="$emit('new-session')">
            <svg fill="none" height="10" stroke="currentColor" stroke-linecap="round" stroke-width="1.8"
                 viewBox="0 0 24 24" width="10">
              <line x1="12" x2="12" y1="5" y2="19"/>
              <line x1="5" x2="19" y1="12" y2="12"/>
            </svg>
          </button>
          <button class="action-icon-btn" title="Clear all sessions" @click="$emit('clear-all-sessions')">
            <svg fill="none" height="10" stroke="currentColor" stroke-linecap="round" stroke-width="1.8"
                 viewBox="0 0 24 24" width="10">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </div>
      <div v-if="sessions.length === 0" class="empty-sessions">
        <span class="empty-hint">No sessions yet</span>
      </div>
      <div v-else class="session-list">
        <div
            v-for="s in sessions"
            :key="s.sessionId"
            :class="{ active: s.sessionId === activeSessionId }"
            class="session-item"
            @click="$emit('select-session', s.sessionId)"
        >
          <span class="session-title">{{ s.title || 'New Chat' }}</span>
          <button
              class="session-del-btn"
              title="Delete session"
              @click.stop="$emit('delete-session', s.sessionId)"
          >
            <svg fill="none" height="8" stroke="currentColor" stroke-linecap="round" stroke-width="1.8"
                 viewBox="0 0 24 24" width="8">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import type {KbCategory, ChatSession} from '@/types'

defineProps<{
  categories: KbCategory[]
  sessions: ChatSession[]
  activeMode: string
  activeSessionId?: string
  activeCategory?: string
}>()

defineEmits<{
  navigate: [mode: string]
  'select-category': [id: string]
  'select-session': [id: string]
  'delete-session': [id: string]
  'new-session': []
  'clear-all-sessions': []
}>()
</script>

<style scoped>
.kb-sidebar {
  width: 220px;
  min-width: 220px;
  display: flex;
  flex-direction: column;
  background: var(--surface-card, #f5f5f7);
  border-right: 1px solid var(--color-silver-mist, #e5e5e7);
  height: 100%;
  overflow: hidden;
  padding: 12px 0;
}

.sidebar-section {
  padding: 0 10px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #474747;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: all 0.12s ease;
  margin-bottom: 1px;
}

.nav-item:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.nav-item.active {
  background: #1d1d1f;
  color: #ffffff;
}

.sidebar-divider {
  height: 1px;
  background: #e5e5e7;
  margin: 10px 12px;
}

.section-label {
  display: block;
  padding: 6px 12px 4px;
  font-size: 10px;
  font-weight: 600;
  color: #aeaeb2;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.cat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #474747;
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.1s ease;
}

.cat-item:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.cat-item.active {
  background: #e8e8ed;
  color: #1d1d1f;
  font-weight: 600;
}

.cat-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  opacity: 0.5;
  font-size: 14px;
}

.cat-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat-count {
  font-size: 10px;
  color: #aeaeb2;
  font-weight: 500;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 4px;
}

.section-actions {
  display: flex;
  align-items: center;
}

.action-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #aeaeb2;
  cursor: pointer;
  transition: all 0.1s ease;
}

.action-icon-btn:hover {
  background: #e8e8ed;
  color: #474747;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  max-height: 300px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #474747;
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.1s ease;
  width: 100%;
}

.session-item:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.session-item.active {
  background: #1d1d1f;
  color: #ffffff;
}

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-del-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: inherit;
  opacity: 0;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.1s ease;
}

.session-item:hover .session-del-btn {
  opacity: 0.5;
}

.session-item:hover .session-del-btn:hover {
  opacity: 1;
  background: rgba(0, 0, 0, 0.1);
}

.session-item.active .session-del-btn {
  color: #ffffff;
}

.empty-sessions {
  padding: 8px 12px;
}

.empty-hint {
  font-size: 11px;
  color: #aeaeb2;
}
</style>
