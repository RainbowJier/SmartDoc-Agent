import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import type {ChatMessage, ChatSession} from '@/types'
import {
    batchDeleteSessions as apiBatchDeleteSessions,
    clearAllSessions as apiClearAllSessions,
    clearSession as apiClearSession,
    getChatHistory,
    getSessionList,
    regenerateTitle as apiRegenerateTitle,
    renameSession as apiRenameSession,
    streamChat
} from '@/api/chat'

function generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substring(2)
}

export const useChatStore = defineStore('chat', () => {
    const sessions = ref<ChatSession[]>([])
    const currentSessionId = ref<string>('')
    const messages = ref<Map<string, ChatMessage[]>>(new Map())
    const isLoading = ref(false)
    const isSessionsLoading = ref(false)

    let abortController: AbortController | null = null

    const currentMessages = computed(() => {
        return messages.value.get(currentSessionId.value) || []
    })

    async function loadSessions() {
        isSessionsLoading.value = true
        try {
            const list = await getSessionList()
            sessions.value = list
            if (!currentSessionId.value && list.length > 0) {
                currentSessionId.value = list[0].sessionId
            }
        } catch {
            // fallback to local sessions
        } finally {
            isSessionsLoading.value = false
        }
    }

    function createSession(): string {
        const id = generateId()
        const session: ChatSession = {
            sessionId: id,
            title: null,
            createdAt: Date.now(),
            lastActiveAt: Date.now(),
            messageCount: 0,
            status: 'active'
        }
        sessions.value.unshift(session)
        messages.value.set(id, [])
        currentSessionId.value = id
        return id
    }

    function findSessionIndex(id: string): number {
        return sessions.value.findIndex((s) => s.sessionId === id)
    }

    function ensureSession(): string {
        if (!currentSessionId.value) {
            return createSession()
        }
        return currentSessionId.value
    }

    async function selectSession(id: string) {
        currentSessionId.value = id
        if (messages.value.has(id)) {
            return
        }
        messages.value.set(id, [])
        try {
            const history = await getChatHistory(id)
            if (history.length === 0) return
            const msgs: ChatMessage[] = history.map((h) => ({
                id: generateId(),
                role: h.role as 'user' | 'assistant',
                content: h.content ?? '',
                timestamp: Date.now()
            }))
            messages.value.set(id, msgs)
        } catch {
            // backend may not have history for new sessions
        }
    }

    async function deleteSession(id: string) {
        try {
            await apiClearSession(id)
        } catch {
            // ignore
        }
        const idx = findSessionIndex(id)
        if (idx !== -1) sessions.value.splice(idx, 1)
        messages.value.delete(id)
        if (currentSessionId.value === id) {
            currentSessionId.value = sessions.value[0]?.sessionId || ''
        }
    }

    async function sendMessage(content: string) {
        const sessionId = ensureSession()

        const userMsg: ChatMessage = {
            id: generateId(),
            role: 'user',
            content,
            timestamp: Date.now()
        }
        const current = messages.value.get(sessionId) || []
        messages.value.set(sessionId, [...current, userMsg])

        const assistantMsg: ChatMessage = {
            id: generateId(),
            role: 'assistant',
            content: '',
            timestamp: Date.now(),
            isStreaming: true
        }
        messages.value.set(sessionId, [...(messages.value.get(sessionId) || []), assistantMsg])
        isLoading.value = true

        abortController = streamChat(
            {message: content, sessionId},

            (token) => {
                const msgs = messages.value.get(sessionId) || []
                const last = msgs[msgs.length - 1]
                if (last && last.role === 'assistant') {
                    last.content += token
                    messages.value.set(sessionId, [...msgs])
                }
            },

            async () => {
                const msgs = messages.value.get(sessionId) || []
                const last = msgs[msgs.length - 1]
                if (last && last.role === 'assistant') {
                    last.isStreaming = false
                    messages.value.set(sessionId, [...msgs])
                }
                isLoading.value = false
                // refresh session list to pick up AI-generated title
                await loadSessions()
            },

            (error) => {
                const msgs = messages.value.get(sessionId) || []
                const last = msgs[msgs.length - 1]
                if (last && last.role === 'assistant') {
                    last.content = `Error: ${error.message}`
                    last.isStreaming = false
                    messages.value.set(sessionId, [...msgs])
                }
                isLoading.value = false
            }
        )
    }

    function stopStreaming() {
        abortController?.abort()
        isLoading.value = false
    }

    async function renameSession(sessionId: string, title: string) {
        try {
            await apiRenameSession(sessionId, title)
            const idx = findSessionIndex(sessionId)
            if (idx !== -1) sessions.value[idx].title = title
        } catch {
            // ignore
        }
    }

    async function regenerateTitle(sessionId: string) {
        try {
            const title = await apiRegenerateTitle(sessionId)
            const idx = findSessionIndex(sessionId)
            if (idx !== -1) sessions.value[idx].title = title
        } catch {
            // ignore
        }
    }

    async function batchDeleteSessionIds(ids: string[]) {
        try {
            await apiBatchDeleteSessions(ids)
        } catch {
            // ignore
        }
        const idSet = new Set(ids)
        sessions.value = sessions.value.filter((s) => !idSet.has(s.sessionId))
        for (const id of ids) {
            messages.value.delete(id)
        }
        if (ids.includes(currentSessionId.value)) {
            currentSessionId.value = sessions.value[0]?.sessionId || ''
        }
    }

    async function clearAllSessions() {
        try {
            await apiClearAllSessions()
        } catch {
            // ignore
        }
        sessions.value = []
        messages.value.clear()
        currentSessionId.value = ''
    }

    return {
        sessions,
        currentSessionId,
        isLoading,
        isSessionsLoading,
        currentMessages,
        loadSessions,
        createSession,
        selectSession,
        deleteSession,
        sendMessage,
        stopStreaming,
        clearAllSessions
    }
})
