import axios, {type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse} from 'axios'
import type {ApiResponse} from '@/types'

const http: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 30_000,
    headers: {'Content-Type': 'application/json'}
})

http.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        return config
    },
    (error: AxiosError) => {
        return Promise.reject(error)
    }
)

http.interceptors.response.use(
    (response: AxiosResponse<ApiResponse>) => {
        const {code, msg} = response.data
        if (code !== 200) {
            return Promise.reject(new Error(msg || `Request failed with code ${code}`))
        }
        return response
    },
    (error: AxiosError) => {
        if (error.code === 'ERR_CANCELED') {
            return Promise.reject(error)
        }
        if (error.response) {
            const msg = `HTTP ${error.response.status}: ${error.response.statusText}`
            return Promise.reject(new Error(msg))
        }
        if (error.request) {
            return Promise.reject(new Error('Network error, please check your connection'))
        }
        return Promise.reject(error)
    }
)

export async function get<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
    const res = await http.get<ApiResponse<T>>(url, {params})
    const {code, msg, data} = res.data
    if (code !== 200) throw new Error(msg || `Request failed with code ${code}`)
    return data
}

export async function post<T = unknown>(url: string, body?: unknown): Promise<T> {
    const res = await http.post<ApiResponse<T>>(url, body)
    const {code, msg, data} = res.data
    if (code !== 200) throw new Error(msg || `Request failed with code ${code}`)
    return data
}

export async function del<T = unknown>(url: string, data?: unknown): Promise<T> {
    const res = await http.delete<ApiResponse<T>>(url, data !== undefined ? {data} : undefined)
    const {code, msg, data: responseData} = res.data
    if (code !== 200) throw new Error(msg || `Request failed with code ${code}`)
    return responseData
}

export async function put<T = unknown>(url: string, body?: unknown): Promise<T> {
    const res = await http.put<ApiResponse<T>>(url, body)
    const {code, msg, data} = res.data
    if (code !== 200) throw new Error(msg || `Request failed with code ${code}`)
    return data
}

export default http
