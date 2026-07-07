<script setup>
import { computed, ref } from 'vue'
import ChatRoom from './components/ChatRoom.vue'

const apps = [
  {
    key: 'home',
    name: '应用主页',
    description: '选择一个 AI 应用开始对话',
  },
  {
    key: 'love',
    name: 'AI 恋爱大师',
    description: '情感沟通、恋爱建议和关系分析助手',
    theme: 'love',
    endpoint: '/ai/love_app/chat/sse',
    withChatId: true,
    placeholder: '描述你的情感问题，例如：第一次约会应该聊什么？',
  },
  {
    key: 'manus',
    name: 'AI 超级智能体',
    description: '通用任务规划、问题拆解和执行建议助手',
    theme: 'dream',
    endpoint: '/ai/momoball/chat',
    withChatId: false,
    greeting: '你好我是MoMoBall,请输入你的问题，我会实时回复你。',
    placeholder: '告诉智能体你想完成的任务，例如：帮我规划一场旅行',
  },
]

const currentKey = ref('home')

const chatApps = computed(() => apps.filter((app) => app.key !== 'home'))
</script>

<template>
  <main class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">AI</span>
        <div>
          <h1>MoMoBallAgent</h1>
          <p>智能应用控制台</p>
        </div>
      </div>

      <nav class="nav-list" aria-label="应用导航">
        <button
          v-for="app in apps"
          :key="app.key"
          class="nav-item"
          :class="{ active: currentKey === app.key }"
          type="button"
          @click="currentKey = app.key"
        >
          <strong>{{ app.name }}</strong>
          <span>{{ app.description }}</span>
        </button>
      </nav>
    </aside>

    <section v-if="currentKey === 'home'" class="home-page">
      <div class="hero-card">
        <p class="eyebrow">请选择应用</p>
        <h2>一个入口，两个实时 AI 对话场景</h2>
        <p>
          主页用于切换不同应用。进入聊天页后，系统会创建独立会话，并通过 SSE
          实时展示后端返回内容。
        </p>
      </div>

      <div class="app-grid">
        <article v-for="app in chatApps" :key="app.key" class="app-card" :class="`theme-${app.theme}`">
          <div>
            <h3>{{ app.name }}</h3>
            <p>{{ app.description }}</p>
          </div>
          <button type="button" @click="currentKey = app.key">进入应用</button>
        </article>
      </div>
    </section>

    <ChatRoom
      v-for="app in chatApps"
      v-show="currentKey === app.key"
      :key="app.key"
      :title="app.name"
      :description="app.description"
      :endpoint="app.endpoint"
      :theme="app.theme"
      :with-chat-id="app.withChatId"
      :greeting="app.greeting"
      :placeholder="app.placeholder"
      @back="currentKey = 'home'"
    />
  </main>
</template>
