<template>
  <div ref="askViewRef" class="ask-view">
    <div ref="scrollRef" class="ask-scroll">
      <div v-if="chatStore.currentMessages.length === 0" class="ask-empty">
        <div class="empty-icon">
          <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
               stroke-width="1.5" viewBox="0 0 24 24" width="18">
            <circle cx="12" cy="12" r="10"/>
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" x2="12.01" y1="17" y2="17"/>
          </svg>
        </div>
        <h2 class="empty-title">Ask your knowledge base</h2>
        <p class="empty-sub">I'll search the knowledge base and cite my sources</p>
        <div class="empty-suggestions">
          <button v-for="s in suggestions" :key="s" class="suggest-pill" @click="sendMessage(s)">
            {{ s }}
          </button>
        </div>
      </div>

      <div v-else class="thread">
        <div v-for="msg in chatStore.currentMessages" :key="msg.id" :class="['msg-block', msg.role]">
          <div v-if="msg.role === 'user'" class="user-msg">
            <div class="user-bubble">{{ msg.content }}</div>
          </div>

          <div v-else class="ai-msg">
            <div class="ai-head">
              <div class="ai-avatar">
                <svg fill="none" height="11" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                     stroke-width="2" viewBox="0 0 24 24" width="11">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                  <path d="M2 17l10 5 10-5"/>
                  <path d="M2 12l10 5 10-5"/>
                </svg>
              </div>
              <span class="ai-name">SmartDoc</span>
              <div v-if="msg.isStreaming" class="stream-badge">generating...</div>
            </div>
            <div class="markdown-body" v-html="getRendered(msg)"/>
            <div v-if="msg.isStreaming" class="stream-cursor-block">
              <span class="stream-dot"/>
            </div>
            <div v-if="sources.length > 0 && !msg.isStreaming" class="sources-block">
              <button class="sources-toggle" @click="toggleSources(msg.id)">
                <svg fill="none" height="10" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                     stroke-width="2" viewBox="0 0 24 24" width="10">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 16 16 12 12 8"/>
                  <line x1="8" x2="16" y1="12" y2="12"/>
                </svg>
                <span>{{ sources.length }} sources</span>
              </button>
              <Transition name="sources-fade">
                <div v-if="openSources.has(msg.id)" class="sources-list">
                  <div v-for="(src, si) in sources" :key="si" class="source-item">
                    <div class="source-title">
                      <svg fill="none" height="9" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                           stroke-width="2" viewBox="0 0 24 24" width="9">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                      </svg>
                      <span>{{ src.title }}</span>
                    </div>
                    <p class="source-snippet">{{ src.snippet }}</p>
                  </div>
                </div>
              </Transition>
            </div>
          </div>
        </div>
        <div ref="bottomRef" class="bottom-sentinel"/>
      </div>
    </div>

    <div class="ask-input-bar">
      <div :class="{ focused: inputFocused }" class="ask-input-shell">
        <input
            ref="inputRef"
            v-model="inputText"
            :disabled="chatStore.isLoading"
            class="ask-input"
            placeholder="Ask anything about your knowledge base..."
            @blur="inputFocused = false"
            @focus="inputFocused = true"
            @keydown="handleKeydown"
        />
        <button
            :class="{ active: inputText.trim() || chatStore.isLoading }"
            class="ask-send-btn"
            @click="handleSend"
        >
          <svg v-if="!chatStore.isLoading" fill="none" height="14" stroke="currentColor" stroke-linecap="round"
               stroke-linejoin="round" stroke-width="2.5" viewBox="0 0 24 24" width="14">
            <line x1="12" x2="12" y1="19" y2="5"/>
            <polyline points="5 12 12 5 19 12"/>
          </svg>
          <svg v-else fill="currentColor" height="14" viewBox="0 0 24 24" width="14">
            <rect height="10" rx="2" width="10" x="7" y="7"/>
          </svg>
        </button>
      </div>
      <div class="ask-input-footer">
        <kbd>Enter</kbd><span>send</span><kbd>⇧ Enter</kbd><span>newline</span>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="activeZoomSvg" class="zoom-modal" @click="closeZoom">
        <div class="zoom-modal-content" @click.stop>
          <header class="zoom-modal-header">
            <div class="zoom-modal-title">
              <svg fill="none" height="15" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="15">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" x2="16.65" y1="21" y2="16.65"/>
              </svg>
              <span>Diagram Viewer</span>
            </div>
            <div class="zoom-modal-actions">
              <button class="zoom-ctrl-btn" title="Reset" @click="resetZoom">
                <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2"
                     viewBox="0 0 24 24" width="13">
                  <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
                  <path d="M3 3v5h5"/>
                </svg>
              </button>
              <button class="zoom-ctrl-btn" @click="zoomScale = Math.max(0.5, zoomScale - 0.25)">
                <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2"
                     viewBox="0 0 24 24" width="13">
                  <line x1="5" x2="19" y1="12" y2="12"/>
                </svg>
              </button>
              <span class="scale-label">{{ Math.round(zoomScale * 100) }}%</span>
              <button class="zoom-ctrl-btn" @click="zoomScale = Math.min(3, zoomScale + 0.25)">
                <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2"
                     viewBox="0 0 24 24" width="13">
                  <line x1="12" x2="12" y1="5" y2="19"/>
                  <line x1="5" x2="19" y1="12" y2="12"/>
                </svg>
              </button>
              <div class="zoom-divider"/>
              <button class="zoom-ctrl-btn accent" @click="downloadModalPng">PNG</button>
              <button class="zoom-ctrl-btn" @click="downloadModalSvg">SVG</button>
              <div class="zoom-divider"/>
              <button class="zoom-close-btn" @click="closeZoom">
                <svg fill="none" height="15" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" width="15">
                  <line x1="18" x2="6" y1="6" y2="18"/>
                  <line x1="6" x2="18" y1="6" y2="18"/>
                </svg>
              </button>
            </div>
          </header>
          <div
              class="zoom-viewport"
              style="cursor: grab; user-select: none;"
              @mousedown="handleMouseDown"
              @mouseleave="handleMouseUp"
              @mousemove="handleMouseMove"
              @mouseup="handleMouseUp"
              @wheel.prevent="handleWheel"
          >
            <div
                :style="{ transform: `translate(${panX}px, ${panY}px) scale(${zoomScale})` }"
                class="zoom-wrapper"
                v-html="activeZoomSvg"
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script lang="ts" setup>
import {nextTick, onMounted, onUnmounted, reactive, ref, watch} from 'vue'
import {gsap} from 'gsap'
import {useChatStore} from '@/stores/chat'
import type {ChatMessage, KbSourceRef} from '@/types'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import xml from 'highlight.js/lib/languages/xml'
import yaml from 'highlight.js/lib/languages/yaml'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import json from 'highlight.js/lib/languages/json'
import mermaid from 'mermaid'

hljs.registerLanguage('java', java)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('json', json)

mermaid.initialize({
  theme: 'base',
  themeVariables: {
    primaryColor: '#f5f5f7',
    primaryTextColor: '#1d1d1f',
    primaryBorderColor: '#d2d2d7',
    lineColor: '#aeaeb2',
    secondaryColor: '#ffffff',
    tertiaryColor: '#f5f5f7',
    background: '#ffffff',
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
    fontSize: '13px',
  },
  flowchart: {
    useMaxWidth: true,
    htmlLabels: true,
    curve: 'basis',
    padding: 16,
    nodeSpacing: 50,
    rankSpacing: 80,
  },
  startOnLoad: false,
})

const chatStore = useChatStore()
const scrollRef = ref<HTMLElement>()
const bottomRef = ref<HTMLElement>()
const inputRef = ref<HTMLInputElement>()
const inputText = ref('')
const inputFocused = ref(false)
const openSources = ref(new Set<string>())
const askViewRef = ref<HTMLElement>()
let askCtx: gsap.Context | null = null

const suggestions = [
  'How to configure Spring Boot datasource?',
  'Explain JWT authentication flow',
  'Best practices for REST API design',
  'Spring Boot transaction management',
]

const sources = ref<KbSourceRef[]>([])

let mermaidIdCounter = 0
let isStreamingMsg = false
let currentRenderingMsgId = ''

const md = new MarkdownIt({
  html: false, linkify: true, breaks: false, typographer: true,
  highlight(str: string, lang: string): string {
    if (lang === 'mermaid') {
      if (isStreamingMsg) {
        return `<div class="mermaid-container mermaid-streaming">
          <div class="code-hdr">
            <span class="code-lang">mermaid</span>
            <span class="code-line-count">Rendering...</span>
          </div>
          <pre class="mermaid-fallback">${md.utils.escapeHtml(str)}</pre>
        </div>`
      }
      const id = `mermaid-${++mermaidIdCounter}`
      const msgId = currentRenderingMsgId
      setTimeout(() => renderMermaid(msgId, id, str), 0)
      return `<div class="mermaid-container">
        <div class="mermaid-toolbar">
          <button class="mermaid-tool-btn zoom-btn" title="Zoom & Drag" type="button">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
            Zoom
          </button>
          <button class="mermaid-tool-btn export-png-btn" title="Export PNG" type="button">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            PNG
          </button>
          <button class="mermaid-tool-btn export-svg-btn" title="Export SVG" type="button">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v8"/><path d="m8 12 4 4 4-4"/>
            </svg>
            SVG
          </button>
        </div>
        <div id="${id}" class="mermaid-svg">${md.utils.escapeHtml(str)}</div>
      </div>`
    }
    const hl = (code: string, l: string) => {
      try {
        return hljs.highlight(code, {language: l, ignoreIllegals: true}).value
      } catch {
        return md.utils.escapeHtml(code)
      }
    }
    const detected = lang && hljs.getLanguage(lang) ? lang : (hljs.highlightAuto(str).language || '')
    const highlighted = detected ? hl(str, detected) : md.utils.escapeHtml(str)
    const langLabel = detected || 'text'
    const lines = str.split('\n').length
    return [
      `<div class="code-block">`,
      `<div class="code-hdr">`,
      `<span class="code-lang">${langLabel}</span>`,
      `<span class="code-line-count">${lines} lines</span>`,
      `<button class="code-cpy" type="button">Copy</button>`,
      `</div>`,
      `<code class="hljs">${highlighted}</code>`,
      `</div>`
    ].join('')
  }
})

const defaultLinkOpen = md.renderer.rules.link_open ||
    ((tokens: any, idx: any, options: any, _env: any, self: any) => self.renderToken(tokens, idx, options))
md.renderer.rules.link_open = (tokens: any, idx: any, options: any, env: any, self: any) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

const defaultTableOpen = md.renderer.rules.table_open ||
    ((tokens: any, idx: any, options: any, _env: any, self: any) => self.renderToken(tokens, idx, options))
md.renderer.rules.table_open = (tokens: any, idx: any, options: any, env: any, self: any) => {
  return '<div class="table-scroll">' + defaultTableOpen(tokens, idx, options, env, self)
}
const defaultTableClose = md.renderer.rules.table_close ||
    ((_tokens: any, _idx: any, _options: any, _env: any, _self: any) => '</table>')
md.renderer.rules.table_close = (tokens: any, idx: any, options: any, env: any, self: any) => {
  return defaultTableClose(tokens, idx, options, env, self) + '</div>'
}

const htmlCache = reactive(new Map<string, string>())

function getRendered(msg: ChatMessage): string {
  if (htmlCache.has(msg.id) && !msg.isStreaming) return htmlCache.get(msg.id)!
  isStreamingMsg = !!msg.isStreaming
  currentRenderingMsgId = msg.id
  const raw = msg.isStreaming ? sanitize(msg.content) : msg.content
  const html = md.render(normalize(raw))
  currentRenderingMsgId = ''
  isStreamingMsg = false
  const result = msg.isStreaming ? html + '<span class="cursor-blink"></span>' : html
  if (!msg.isStreaming) htmlCache.set(msg.id, result)
  return result
}

const MERMAID_CLASS_NAMES = ['warning', 'decision', 'highlight', 'success', 'primary', 'default', 'accent', 'error', 'warn', 'info', 'sub']

function preprocessMermaid(definition: string): string {
  let clean = definition.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  clean = clean.replace(/%%\{init:[\s\S]*?\}%%/gi, '')
  const lines = clean.split('\n')
  const firstLine = lines.find(l => l.trim() && !l.trim().startsWith('%%'))
  const isFlowchart = firstLine ? /^\s*(flowchart|graph)\b/i.test(firstLine.trim()) : true
  const processedLines: string[] = []
  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    if (line === undefined) continue
    const indent = /^\s*/.exec(line)![0] || ''
    let t = line.trim()
    if (!t || t.startsWith('%%')) continue
    if (!isFlowchart) {
      if (/^classDef\b/.test(t) || /^class\s+[A-Z]/.test(t)) continue
    }
    t = t.replace(/\b(flowchart|graph)\s*(TD|TB|LR|BT|RL)\b/gi, '$1 $2')
    t = t.replace(/\b(direction)\s*(TB|BT|LR|RL|TD)\b/gi, '$1 $2')
    t = t.replace(/\bsubgraph\s*([A-Za-z0-9_\u4e00-\u9fff[")])/gi, 'subgraph $1')
    t = t.replace(/\bclassDef\s*([a-zA-Z_][a-zA-Z0-9_]*)\s*(fill|stroke|color|font-size|fontFamily|fontStyle|stroke-width|stroke-dasharray|border-radius)/gi, 'classDef $1 $2')
    if (/^\s*class\s*[A-Z]/.test(t) && !/^\s*classDef/.test(t)) {
      const s = t.replace(/^\s*class\s+/, 'class')
      if (s.endsWith(';')) {
        const body = s.slice(5, -1)
        let fixed = false
        for (const cn of MERMAID_CLASS_NAMES) {
          const idx = body.lastIndexOf(cn)
          if (idx !== -1 && idx + cn.length === body.length) {
            if (idx > 0 && body[idx - 1] === ' ') {
              fixed = true
              break
            }
            t = `class ${body.slice(0, idx)} ${cn};`
            fixed = true
            break
          }
        }
        if (!fixed) t = s.slice(0, 5) + ' ' + s.slice(5)
      }
    }
    t = t.replace(/-->/g, ' --> ')
    processedLines.push(indent + t)
  }
  return processedLines.join('\n')
}

async function renderMermaid(msgId: string, id: string, definition: string) {
  const el = document.getElementById(id)
  if (!el) return
  const preprocessed = preprocessMermaid(definition)
  try {
    const {svg} = await mermaid.render(id + '-svg', preprocessed)
    el.innerHTML = svg
    if (msgId) {
      const cachedHtml = htmlCache.get(msgId)
      if (cachedHtml) {
        const placeholder = `<div id="${id}" class="mermaid-svg">${md.utils.escapeHtml(definition)}</div>`
        const replacement = `<div id="${id}" class="mermaid-svg">${svg}</div>`
        if (cachedHtml.includes(placeholder)) {
          htmlCache.set(msgId, cachedHtml.replace(placeholder, replacement))
        }
      }
    }
  } catch (err) {
    console.error('Mermaid render error:', err)
    el.innerHTML = `<pre class="mermaid-fallback">${md.utils.escapeHtml(definition)}</pre>`
  }
}

function sanitize(text: string): string {
  const c = (text.match(/```/g) || []).length
  return c % 2 !== 0 ? text + '\n```' : text
}

function normalize(text: string): string {
  return (text ?? '')
      .replace(/^(#{1,6})([^\s#])/gm, '$1 $2')
      .replace(/([^\n])(\s*#{2,6}\s)/g, '$1\n\n$2')
      .replace(/([^\n])(\s*[-+])(\s)/g, '$1\n$2$3')
      .replace(/([^\n])(\s*\d+\.)(?!\d)(?<!\d+\.\d+\.)(\s)/g, '$1\n$2$3')
      .replace(/([^\n])(\s*`{3,})/g, '$1\n\n$2')
      .replace(/(`{3,}[a-z]*\n?)([^\n])/g, '$1\n$2')
}

function toggleSources(id: string) {
  if (openSources.value.has(id)) openSources.value.delete(id)
  else openSources.value.add(id)
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function handleSend() {
  if (chatStore.isLoading) {
    chatStore.stopStreaming()
    return
  }
  const text = inputText.value.trim()
  if (!text) return
  sendMessage(text)
  inputText.value = ''
}

function sendMessage(text: string) {
  chatStore.sendMessage(text)
  nextTick(scrollToBottom)
}

function scrollToBottom() {
  if (bottomRef.value) bottomRef.value.scrollIntoView({behavior: 'smooth'})
}

watch(() => chatStore.currentMessages.length, () => nextTick(scrollToBottom))
watch(() => chatStore.currentMessages[chatStore.currentMessages.length - 1]?.content, () => {
  if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
})
watch(() => chatStore.currentSessionId, () => htmlCache.clear())

/* ── Zoom Modal ──────────────── */
const activeZoomSvg = ref('')
const zoomScale = ref(1.0)
const panX = ref(0)
const panY = ref(0)

let isDragging = false
let startX = 0
let startY = 0

function handleMouseDown(e: MouseEvent) {
  if (e.button !== 0) return
  isDragging = true
  startX = e.clientX - panX.value
  startY = e.clientY - panY.value
  const vp = e.currentTarget as HTMLElement
  vp.style.cursor = 'grabbing'
}

function handleMouseMove(e: MouseEvent) {
  if (!isDragging) return
  panX.value = e.clientX - startX
  panY.value = e.clientY - startY
}

function handleMouseUp(e: MouseEvent) {
  isDragging = false
  const vp = e.currentTarget as HTMLElement
  if (vp) vp.style.cursor = 'grab'
}

function resetZoom() {
  zoomScale.value = 1.0
  panX.value = 0
  panY.value = 0
}

function closeZoom() {
  activeZoomSvg.value = ''
  resetZoom()
}

const ZOOM_STEP = 0.1
const ZOOM_MIN = 0.25
const ZOOM_MAX = 5

function handleWheel(e: WheelEvent) {
  const vp = e.currentTarget as HTMLElement
  const rect = vp.getBoundingClientRect()
  const mx = e.clientX - rect.left - rect.width / 2
  const my = e.clientY - rect.top - rect.height / 2
  const prev = zoomScale.value
  const delta = e.deltaY < 0 ? ZOOM_STEP : -ZOOM_STEP
  const ns = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, prev + delta))
  const sd = ns / prev
  panX.value = mx - sd * (mx - panX.value)
  panY.value = my - sd * (my - panY.value)
  zoomScale.value = ns
}

function downloadModalSvg() {
  if (activeZoomSvg.value) triggerSvgDownload(activeZoomSvg.value, 'mermaid-diagram.svg')
}

function downloadModalPng() {
  if (activeZoomSvg.value) triggerPngDownload(activeZoomSvg.value, 'mermaid-diagram.png')
}

/* ── Mermaid & Copy Events ──── */
function handleCopyClick(e: MouseEvent) {
  const btn = (e.target as HTMLElement).closest<HTMLButtonElement>('.code-cpy')
  if (!btn) return
  const block = btn.closest('.code-block')
  const code = block?.querySelector('code')?.innerText ?? ''
  navigator.clipboard.writeText(code).then(() => {
    btn.textContent = 'Copied!'
    setTimeout(() => {
      btn.textContent = 'Copy'
    }, 1500)
  })
}

function handleMermaidClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  const zoomBtn = target.closest<HTMLButtonElement>('.zoom-btn')
  if (zoomBtn) {
    const container = zoomBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s;
          break
        }
      }
    }
    if (svgEl) {
      activeZoomSvg.value = svgEl.outerHTML;
      resetZoom()
    }
    return
  }
  const pngBtn = target.closest<HTMLButtonElement>('.export-png-btn')
  if (pngBtn) {
    const container = pngBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s;
          break
        }
      }
    }
    if (svgEl) triggerPngDownload(svgEl.outerHTML, 'mermaid-diagram.png')
    return
  }
  const svgBtn = target.closest<HTMLButtonElement>('.export-svg-btn')
  if (svgBtn) {
    const container = svgBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s;
          break
        }
      }
    }
    if (svgEl) triggerSvgDownload(svgEl.outerHTML, 'mermaid-diagram.svg')
    return
  }
}

function triggerSvgDownload(content: string, filename: string) {
  const blob = new Blob([content], {type: 'image/svg+xml;charset=utf-8'})
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url;
  a.download = filename
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function triggerPngDownload(svgContent: string, filename: string) {
  const img = new Image()
  const blob = new Blob([svgContent], {type: 'image/svg+xml;charset=utf-8'})
  const url = URL.createObjectURL(blob)
  img.onload = () => {
    const canvas = document.createElement('canvas')
    const sf = 2
    const w = img.naturalWidth || 800
    const h = img.naturalHeight || 600
    canvas.width = w * sf;
    canvas.height = h * sf
    const ctx = canvas.getContext('2d')
    if (ctx) {
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.scale(sf, sf);
      ctx.drawImage(img, 0, 0)
      try {
        const pngUrl = canvas.toDataURL('image/png')
        const a = document.createElement('a')
        a.href = pngUrl;
        a.download = filename
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a)
      } catch {
        triggerSvgDownload(svgContent, filename.replace('.png', '.svg'))
      }
    }
    URL.revokeObjectURL(url)
  }
  img.onerror = () => {
    triggerSvgDownload(svgContent, filename.replace('.png', '.svg'));
    URL.revokeObjectURL(url)
  }
  img.src = url
}

onMounted(() => {
  if (!scrollRef.value) return
  scrollRef.value.addEventListener('click', handleCopyClick)
  scrollRef.value.addEventListener('click', handleMermaidClick)

  askCtx = gsap.context(() => {
    const tl = gsap.timeline({defaults: {ease: 'power3.out', duration: 0.45}})
    tl.from('.ask-empty', {autoAlpha: 0, y: 20, duration: 0.4}, 0.1)
        .from('.ask-input-shell', {autoAlpha: 0, y: 14, duration: 0.35}, 0.3)
        .from('.ask-input-footer', {autoAlpha: 0, y: 8, duration: 0.3}, 0.45)
  }, askViewRef.value)
})

onUnmounted(() => {
  askCtx?.revert()
  if (!scrollRef.value) return
  scrollRef.value.removeEventListener('click', handleCopyClick)
  scrollRef.value.removeEventListener('click', handleMermaidClick)
})
</script>

<style scoped>
.ask-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.ask-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0 32px;
}

.ask-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 100%;
  padding: 60px 0;
  animation: fadeInUp 0.5s ease both;
}

.empty-icon {
  width: 48px;
  height: 48px;
  background: #f0f0f2;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.4px;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.empty-sub {
  font-size: 14px;
  color: #86868b;
  margin-bottom: 28px;
  max-width: 340px;
  line-height: 1.5;
}

.empty-suggestions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.suggest-pill {
  font-family: inherit;
  font-size: 12px;
  color: #86868b;
  background: #f0f0f2;
  border: 1px solid #e5e5e7;
  border-radius: 999px;
  padding: 5px 16px;
  cursor: pointer;
  transition: all 0.12s ease;
}

.suggest-pill:hover {
  color: #1d1d1f;
  border-color: #d2d2d7;
  background: #f5f5f7;
}

.thread {
  padding: 28px 0 20px;
  max-width: 700px;
  margin: 0 auto;
}

.msg-block {
  margin-bottom: 24px;
}

.user-msg {
  display: flex;
  justify-content: flex-end;
}

.user-bubble {
  display: inline-block;
  padding: 10px 18px;
  background: #1d1d1f;
  color: #ffffff;
  border-radius: 18px 18px 4px 18px;
  font-size: 14px;
  line-height: 1.5;
  max-width: 68%;
  word-break: break-word;
}

.ai-msg {
  padding: 4px 0 12px;
}

.ai-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  user-select: none;
}

.ai-avatar {
  width: 22px;
  height: 22px;
  background: #f0f0f2;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
  flex-shrink: 0;
}

.ai-name {
  font-size: 13px;
  font-weight: 600;
  color: #1d1d1f;
}

.stream-badge {
  font-size: 10px;
  color: #86868b;
  background: #f0f0f2;
  padding: 1px 8px;
  border-radius: 999px;
  animation: pulse 1.2s ease infinite;
}

.stream-cursor-block {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 8px 12px;
  background: #f0f0f2;
  border-radius: 8px;
}

.stream-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #1d1d1f;
  animation: pulse 1.2s ease infinite;
}

/* ── Sources ──────────────────── */
.sources-block {
  margin-top: 12px;
}

.sources-toggle {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  font-family: inherit;
  font-size: 11px;
  font-weight: 500;
  color: #86868b;
  background: #f5f5f7;
  border: 1px solid #e5e5e7;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.sources-toggle:hover {
  color: #1d1d1f;
  border-color: #d2d2d7;
  background: #f0f0f2;
}

.sources-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.source-item {
  padding: 8px 12px;
  background: #f5f5f7;
  border-radius: 8px;
  border: 1px solid #e8e8ed;
}

.source-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 3px;
}

.source-snippet {
  font-size: 11px;
  color: #86868b;
  line-height: 1.5;
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.sources-fade-enter-active,
.sources-fade-leave-active {
  transition: all 0.15s ease;
}

.sources-fade-enter-from,
.sources-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* ── Input Bar ────────────────── */
.ask-input-bar {
  flex-shrink: 0;
  padding: 12px 32px 20px;
  background: #ffffff;
  border-top: 1px solid #e8e8ed;
}

.ask-input-shell {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 6px 6px 16px;
  background: #f5f5f7;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  max-width: 700px;
  margin: 0 auto;
  transition: border-color 0.15s ease;
}

.ask-input-shell.focused {
  border-color: #aeaeb2;
}

.ask-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-family: inherit;
  font-size: 14px;
  color: #1d1d1f;
  line-height: 1.5;
}

.ask-input::placeholder {
  color: #aeaeb2;
}

.ask-send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: #d2d2d7;
  color: #86868b;
  cursor: default;
  flex-shrink: 0;
  transition: all 0.1s ease;
}

.ask-send-btn.active {
  background: #1d1d1f;
  color: #ffffff;
  cursor: pointer;
}

.ask-send-btn.active:hover {
  opacity: 0.85;
}

.ask-input-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  font-size: 11px;
  color: #aeaeb2;
}

.ask-input-footer kbd {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 3px;
  font-size: 9px;
  font-family: inherit;
  font-weight: 500;
  color: #aeaeb2;
  background: #f0f0f2;
  border: 1px solid #e5e5e7;
  border-radius: 3px;
  line-height: 1;
}

/* ── Markdown Body ────────────── */
.markdown-body {
  font-size: 14px;
  line-height: 1.7;
  color: #1d1d1f;
  word-break: break-word;
}

.markdown-body :deep(p) {
  margin: 0 0 10px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3), .markdown-body :deep(h4) {
  font-weight: 600;
  color: #1d1d1f;
  margin: 24px 0 8px;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

.markdown-body :deep(h1) {
  font-size: 22px;
}

.markdown-body :deep(h2) {
  font-size: 18px;
  border-bottom: 1px solid #e8e8ed;
  padding-bottom: 6px;
}

.markdown-body :deep(h3) {
  font-size: 16px;
}

.markdown-body :deep(h2:first-child), .markdown-body :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 20px;
  margin: 6px 0 10px;
}

.markdown-body :deep(li) {
  margin-bottom: 3px;
}

.markdown-body :deep(blockquote) {
  margin: 10px 0;
  padding: 10px 14px;
  border-left: 3px solid #1d1d1f;
  background: #f5f5f7;
  border-radius: 0 8px 8px 0;
  color: #474747;
  font-size: 13px;
}

.markdown-body :deep(.table-scroll) {
  width: 100%;
  overflow-x: auto;
  margin: 14px 0;
  border-radius: 8px;
  border: 1px solid #e8e8ed;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  min-width: 400px;
}

.markdown-body :deep(thead) {
  background: #f5f5f7;
}

.markdown-body :deep(th) {
  padding: 8px 12px;
  text-align: left;
  font-weight: 500;
  font-size: 11px;
  color: #707070;
  border-bottom: 1px solid #e8e8ed;
  white-space: nowrap;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.markdown-body :deep(td) {
  padding: 8px 12px;
  border-bottom: 1px solid #e8e8ed;
  color: #1d1d1f;
}

.markdown-body :deep(tr:last-child td) {
  border-bottom: none;
}

.markdown-body :deep(tr:hover td) {
  background: #f5f5f7;
}

.markdown-body :deep(code:not(.hljs)) {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 0.85em;
  background: #f0f0f2;
  color: #1d1d1f;
  padding: 1px 5px;
  border-radius: 4px;
}

.markdown-body :deep(a) {
  color: #0066cc;
  text-decoration: none;
  font-weight: 500;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(strong) {
  font-weight: 600;
}

.markdown-body :deep(em) {
  font-style: italic;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8ed;
  margin: 20px 0;
}

.markdown-body :deep(.code-block) {
  margin: 14px 0;
  background: #1d1d1f;
  border-radius: 10px;
  overflow: hidden;
}

.markdown-body :deep(.code-hdr) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #2c2c2e;
  user-select: none;
}

.markdown-body :deep(.code-lang) {
  font-family: 'SF Mono', monospace;
  font-size: 10px;
  font-weight: 500;
  color: #aeaeb2;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.markdown-body :deep(.code-cpy) {
  font-family: inherit;
  font-size: 10px;
  font-weight: 500;
  color: #aeaeb2;
  background: #3a3a3c;
  border: none;
  border-radius: 4px;
  padding: 2px 8px;
  cursor: pointer;
  opacity: 0;
  margin-left: auto;
  transition: all 0.1s ease;
}

.markdown-body :deep(.code-block:hover .code-cpy) {
  opacity: 1;
}

.markdown-body :deep(.code-cpy:hover) {
  background: #48484a;
  color: #ffffff;
}

.markdown-body :deep(.code-block code.hljs) {
  display: block;
  padding: 12px 14px;
  font-family: 'SF Mono', monospace;
  font-size: 12px;
  line-height: 1.7;
  overflow-x: auto;
  color: #f5f5f7;
  tab-size: 2;
}

.markdown-body :deep(.cursor-blink) {
  display: inline-block;
  width: 2px;
  height: 1.1em;
  background: #1d1d1f;
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 0.8s steps(2) infinite;
}

/* ── Mermaid ──────────────────────── */
.markdown-body :deep(.mermaid-container) {
  position: relative;
  margin: 14px 0;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e5e5e7;
  overflow: visible;
  transition: border-color 0.15s ease;
}

.markdown-body :deep(.mermaid-container:hover) {
  border-color: #d2d2d7;
}

.markdown-body :deep(.mermaid-svg) {
  overflow: auto;
  max-height: 70vh;
  padding: 20px;
  -webkit-overflow-scrolling: touch;
}

.markdown-body :deep(.mermaid-svg svg) {
  display: block;
  max-width: 100%;
  width: fit-content;
  min-width: min(100%, 400px);
  height: auto;
  font-family: Inter, -apple-system, BlinkMacSystemFont, sans-serif;
}

.markdown-body :deep(.mermaid-fallback) {
  font-family: 'SF Mono', monospace;
  font-size: 13px;
  color: #86868b;
  white-space: pre-wrap;
}

.markdown-body :deep(.mermaid-toolbar) {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s ease, transform 0.2s ease;
  z-index: 5;
  transform: translateY(-4px);
}

.markdown-body :deep(.mermaid-container:hover .mermaid-toolbar) {
  opacity: 1;
  transform: translateY(0);
}

.markdown-body :deep(.mermaid-tool-btn) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 5px 10px;
  font-family: inherit;
  font-size: 10px;
  font-weight: 600;
  line-height: 1;
  color: #474747;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid #d2d2d7;
  border-radius: 7px;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.12s ease;
  letter-spacing: 0.01em;
  text-transform: uppercase;
}

.markdown-body :deep(.mermaid-tool-btn.active) {
  color: #ffffff;
  background: #1d1d1f;
  border-color: #1d1d1f;
}

.markdown-body :deep(.mermaid-tool-btn:hover) {
  color: #1d1d1f;
  background: rgba(255, 255, 255, 1);
  border-color: #aeaeb2;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.markdown-body :deep(.mermaid-tool-btn:active) {
  transform: translateY(0);
  box-shadow: none;
}

.markdown-body :deep(.mermaid-tool-btn svg) {
  display: block;
  flex-shrink: 0;
}

/* ── Zoom Modal ──────────────── */
.zoom-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.zoom-modal-content {
  width: 90vw;
  height: 85vh;
  background: #ffffff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: modalScaleUp 0.344s cubic-bezier(0.25, 0.1, 0.25, 1) forwards;
}

@keyframes modalScaleUp {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.zoom-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #f5f5f7;
  border-bottom: 1px solid #e5e5e7;
  flex-shrink: 0;
}

.zoom-modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #1d1d1f;
}

.zoom-modal-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.zoom-ctrl-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 5px 10px;
  font-family: inherit;
  font-size: 11px;
  font-weight: 500;
  color: #474747;
  background: #ffffff;
  border: 1px solid #d2d2d7;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.zoom-ctrl-btn:hover {
  border-color: #aeaeb2;
  color: #1d1d1f;
  background: #fafafa;
}

.zoom-ctrl-btn.accent {
  background: #1d1d1f;
  border-color: #1d1d1f;
  color: #ffffff;
}

.zoom-ctrl-btn.accent:hover {
  opacity: 0.85;
}

.scale-label {
  font-family: 'SF Mono', monospace;
  font-size: 11px;
  color: #86868b;
  min-width: 40px;
  text-align: center;
}

.zoom-divider {
  width: 1px;
  height: 16px;
  background: #d2d2d7;
}

.zoom-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: #86868b;
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.1s ease;
}

.zoom-close-btn:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.zoom-viewport {
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #fafafa;
}

.zoom-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  transform-origin: center center;
  width: 100%;
  height: 100%;
}

.zoom-wrapper :deep(svg) {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
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

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}
</style>
