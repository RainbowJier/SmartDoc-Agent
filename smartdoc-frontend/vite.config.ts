// ============================================================================
// vite.config.ts - Vite 开发服务器与构建配置
// ============================================================================
//
// 【教学要点】Vite 是 Vue 3 推荐的构建工具，核心优势：
//   1. 开发模式下使用原生 ESM，无需打包即可热更新，启动速度极快
//   2. 生产构建使用 Rollup，输出优化后的静态资源
//
// 本文件的关键配置项：
//   - plugins: 注册 Vue 单文件组件 (.vue) 支持
//   - resolve.alias: 配置 @ 别名指向 src/，避免写相对路径 ../../../
//   - server.proxy: 开发服务器反向代理，解决前后端跨域问题
//
// 【代理原理】前端运行在 localhost:5173，后端运行在 localhost:8080。
//   浏览器的同源策略会阻止 5173 直接请求 8080。
//   Vite 的 proxy 配置让 /api 开头的请求被转发到后端，浏览器以为
//   请求的是同一个域，从而绕过 CORS 限制。
//   生产环境中通常由 Nginx 等反向代理完成同样的工作。
// ============================================================================

import {fileURLToPath, URL} from 'node:url'
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    // 注册 Vue 插件，使 Vite 能解析 .vue 单文件组件
    plugins: [vue()],

    // 路径别名：@ 代表 src 目录，在 import 时使用 @/xxx 代替相对路径
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },

    // 开发服务器配置
    server: {
        host: '0.0.0.0',  // 监听所有网络接口，局域网内可访问
        port: 5173,        // 前端开发服务器端口

        // 【核心】反向代理配置 —— 将前端 /api 请求转发到后端 Spring Boot 服务
        proxy: {
            '/api': {
                target: 'http://localhost:8080',  // 后端服务地址
                changeOrigin: true                 // 修改请求头中的 Origin，避免后端拒绝
            }
        }
    }
})
