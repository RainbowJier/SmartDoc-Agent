import {get, put, post} from '@/api'
import type {ChatRequest, ChatSession} from '@/types'

export function streamChat(
    request: ChatRequest,
    onToken: (token: string) => void,
    onComplete: () => void,
    onError: (error: Error) => void
): AbortController {
    const controller = new AbortController()

    fetch('/api/chat', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(request),
        signal: controller.signal
    })
        .then(async (response) => {
            if (!response.ok) throw new Error(`HTTP ${response.status}`)

            const reader = response.body!.getReader()
            const decoder = new TextDecoder()
            let buffer = ''

            let eventData: string[] = []

            while (true) {
                const {done, value} = await reader.read()
                if (done) break

                buffer += decoder.decode(value, {stream: true})
                const lines = buffer.split('\n')
                buffer = lines.pop() || ''

                for (const line of lines) {
                    if (line.startsWith('data: ')) {
                        eventData.push(line.substring(6))
                    } else if (line.startsWith('data:')) {
                        eventData.push(line.substring(5))
                    } else if (line === '' && eventData.length > 0) {
                        const data = eventData.join('\n')
                        eventData = []
                        if (data === '[DONE]') {
                            onComplete()
                        } else {
                            onToken(data)
                        }
                    }
                }
            }

            if (eventData.length > 0) {
                const data = eventData.join('\n')
                if (data === '[DONE]') {
                    onComplete()
                } else if (data) {
                    onToken(data)
                }
            }
            onComplete()
        })
        .catch((err) => {
            if (err.name !== 'AbortError') {
                onError(err)
            }
        })

    return controller
}

export async function getChatHistory(sessionId: string): Promise<{ role: string; content: string }[]> {
    return get(`/chat/history/${encodeURIComponent(sessionId)}`)
}

export async function clearSession(sessionId: string): Promise<void> {
    await get(`/chat/session/clear/${encodeURIComponent(sessionId)}`)
}

export async function getSessionList(): Promise<ChatSession[]> {
    return get(`/chat/sessions`)
}

export async function renameSession(sessionId: string, title: string): Promise<void> {
    await put('/chat/session/title', {sessionId, title})
}

export async function regenerateTitle(sessionId: string): Promise<string> {
    return post('/chat/session/title/regenerate', {sessionId})
}

export async function batchDeleteSessions(sessionIds: string[]): Promise<void> {
    await post('/chat/sessions/batch', {sessionIds})
}

export async function clearAllSessions(): Promise<void> {
    await post('/chat/sessions/clear')
}
