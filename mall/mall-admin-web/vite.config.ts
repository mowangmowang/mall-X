import { fileURLToPath, URL } from 'node:url'
import path from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import ElementPlus from 'unplugin-element-plus/vite'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'

// https://vite.dev/config/
export default defineConfig({
  base: './',
  plugins: [
    vue(),
    vueDevTools(),
    // 配置element-plus组件自动导入
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      // 配置element-plus采用sass样式配色系统
      resolvers: [ElementPlusResolver({ importStyle: 'sass' })],
    }),
    // 覆盖element-plus默认主题色
    ElementPlus({
      useSource: true,
    }),
    // 创建SVG图标创建插件
    createSvgIconsPlugin({
      // 指定存放SVG图标的目录
      iconDirs: [path.resolve(process.cwd(), 'src/icons/svg')],
      // 定义图标 ID 的生成规则
      symbolId: 'icon-[dir]-[name]',
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  optimizeDeps: {
    include: [
      'element-plus',
      'element-plus/es',
      'element-plus/es/components/config-provider/style/index',
      'element-plus/es/components/base/style/index',
      'element-plus/es/components/menu/style/index',
      'element-plus/es/components/button/style/index',
      'element-plus/es/components/icon/style/index',
      'element-plus/es/components/avatar/style/index',
      'element-plus/es/components/tag/style/index',
      'element-plus/es/components/sub-menu/style/index',
      'element-plus/es/components/menu-item/style/index',
      'element-plus/es/components/message/style/index',
      'element-plus/es/components/message-box/style/index',
      'element-plus/es/components/breadcrumb/style/index',
      'element-plus/es/components/breadcrumb-item/style/index',
      'element-plus/es/components/skeleton/style/index',
      'element-plus/es/components/date-picker/style/index',
      'element-plus/es/components/row/style/index',
      'element-plus/es/components/col/style/index',
      'element-plus/es/components/loading/style/index',
      'element-plus/es/components/dialog/style/index',
      'element-plus/es/components/pagination/style/index',
      'element-plus/es/components/table/style/index',
      'element-plus/es/components/table-column/style/index',
      'element-plus/es/components/card/style/index',
      'element-plus/es/components/form/style/index',
      'element-plus/es/components/select/style/index',
      'element-plus/es/components/option/style/index',
      'element-plus/es/components/form-item/style/index',
      'element-plus/es/components/input/style/index',
      'element-plus/es/components/switch/style/index',
      'element-plus/es/components/cascader/style/index',
      'element-plus/es/components/steps/style/index',
      'element-plus/es/components/step/style/index',
      'element-plus/es/components/transfer/style/index',
      'element-plus/es/components/checkbox-group/style/index',
      'element-plus/es/components/checkbox/style/index',
      'element-plus/es/components/radio-group/style/index',
      'element-plus/es/components/radio-button/style/index',
      'element-plus/es/components/tabs/style/index',
      'element-plus/es/components/tab-pane/style/index',
      'element-plus/es/components/upload/style/index',
      'element-plus/es/components/radio/style/index',
    ],
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 指定覆盖主题色的scss文件
        additionalData: `
          @use "@/styles/element/index.scss" as *;
          @use "@/styles/var.scss" as *;
        `,
        // 屏蔽在使用el-col时出现的控制台警告信息
        silenceDeprecations: ['legacy-js-api', 'if-function'],
      },
    },
  },
})
