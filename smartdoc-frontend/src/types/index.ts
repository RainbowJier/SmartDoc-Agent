export interface ChatMessage {
    id: string
    role: 'user' | 'assistant'
    content: string
    timestamp: number
    isStreaming?: boolean
}

export interface ChatSession {
    sessionId: string
    title: string | null
    createdAt: number
    lastActiveAt: number
    messageCount: number
    status: string
}

export interface ChatRequest {
    message: string
    sessionId: string
}

export interface ApiResponse<T = unknown> {
    code: number
    msg: string
    data: T
}

export interface KbCategory {
    id: string
    name: string
    icon: string
    description: string
    docCount: number
}

export interface KbDocument {
    id: string
    title: string
    summary: string
    categoryId: string
    tags: string[]
    updatedAt: number
}

export interface KbSourceRef {
    title: string
    snippet: string
    score?: number
}
