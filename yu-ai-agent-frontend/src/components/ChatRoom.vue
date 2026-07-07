<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { buildSseUrl } from '../api'

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
  endpoint: {
    type: String,
    required: true,
  },
  theme: {
    type: String,
    default: 'love',
  },
  withChatId: {
    type: Boolean,
    default: true,
  },
  greeting: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '请输入消息',
  },
})

defineEmits(['back'])

const chatId = ref(createChatId())
const inputMessage = ref('')
const messages = ref([
  {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: getGreeting(),
  },
])
const isStreaming = ref(false)
const errorMessage = ref('')
const messagesEl = ref(null)
let eventSource = null

const displayChatId = computed(() => (props.withChatId ? chatId.value : '当前接口无需 chatId'))
const canSend = computed(() => inputMessage.value.trim().length > 0 && !isStreaming.value)

function createChatId() {
  return `chat_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
}

function getGreeting() {
  return props.greeting || `你好，我是${props.title}。请输入你的问题，我会实时回复你。`
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesEl.value) {
      messagesEl.value.scrollTop = messagesEl.value.scrollHeight
    }
  })
}

function closeStream() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

function appendAssistantChunk(chunk) {
  const lastMessage = messages.value[messages.value.length - 1]

  if (lastMessage?.role === 'assistant' && lastMessage.streaming) {
    lastMessage.content += chunk
  } else {
    messages.value.push({
      id: crypto.randomUUID(),
      role: 'assistant',
      content: chunk,
      streaming: true,
    })
  }

  scrollToBottom()
}

function finishAssistantMessage() {
  const lastMessage = messages.value[messages.value.length - 1]

  if (lastMessage?.role === 'assistant') {
    lastMessage.streaming = false
  }
}

function handleStreamError(message = '连接中断，请稍后重试') {
  errorMessage.value = message
  isStreaming.value = false
  finishAssistantMessage()
  closeStream()
}

function sendMessage() {
  const message = inputMessage.value.trim()

  if (!message || isStreaming.value) {
    return
  }

  errorMessage.value = ''
  inputMessage.value = ''
  messages.value.push({
    id: crypto.randomUUID(),
    role: 'user',
    content: message,
  })
  messages.value.push({
    id: crypto.randomUUID(),
    role: 'assistant',
    content: '',
    streaming: true,
  })
  scrollToBottom()

  closeStream()
  isStreaming.value = true

  const params = props.withChatId ? { message, chatId: chatId.value } : { message }
  eventSource = new EventSource(buildSseUrl(props.endpoint, params))

  eventSource.onmessage = (event) => {
    if (!event.data || event.data === '[DONE]') {
      isStreaming.value = false
      finishAssistantMessage()
      closeStream()
      return
    }

    appendAssistantChunk(event.data)
  }

  eventSource.onerror = () => {
    handleStreamError()
  }
}

function startNewChat() {
  closeStream()
  chatId.value = createChatId()
  isStreaming.value = false
  errorMessage.value = ''
  inputMessage.value = ''
  messages.value = [
    {
      id: crypto.randomUUID(),
      role: 'assistant',
      content: getGreeting(),
    },
  ]
}

onBeforeUnmount(() => {
  closeStream()
})
</script>

<template>
  <section class="chat-page" :class="`theme-${theme}`">
    <div class="anime-girl" :class="`anime-girl-${theme}`" aria-hidden="true">
      <span class="girl-shadow"></span>
      <span class="girl-hair girl-hair-back"></span>
      <span class="girl-tail girl-tail-left"></span>
      <span class="girl-tail girl-tail-right"></span>
      <span class="girl-long-strand girl-long-strand-left"></span>
      <span class="girl-long-strand girl-long-strand-right"></span>
      <span class="girl-hair-highlight girl-hair-highlight-left"></span>
      <span class="girl-hair-highlight girl-hair-highlight-right"></span>
      <span class="girl-ear girl-ear-left"></span>
      <span class="girl-ear girl-ear-right"></span>
      <span class="girl-body"></span>
      <span class="girl-collar"></span>
      <span class="girl-bow"></span>
      <span class="girl-sleeve girl-sleeve-left"></span>
      <span class="girl-sleeve girl-sleeve-right"></span>
      <span class="girl-arm girl-arm-left"></span>
      <span class="girl-arm girl-arm-right"></span>
      <span class="girl-skirt-line girl-skirt-line-one"></span>
      <span class="girl-skirt-line girl-skirt-line-two"></span>
      <span class="girl-leg girl-leg-left"></span>
      <span class="girl-leg girl-leg-right"></span>
      <span class="girl-shoe girl-shoe-left"></span>
      <span class="girl-shoe girl-shoe-right"></span>
      <span class="girl-neck"></span>
      <span class="girl-face">
        <span class="girl-eye girl-eye-left"></span>
        <span class="girl-eye girl-eye-right"></span>
        <span class="girl-eye-shine girl-eye-shine-left"></span>
        <span class="girl-eye-shine girl-eye-shine-right"></span>
        <span class="girl-blush girl-blush-left"></span>
        <span class="girl-blush girl-blush-right"></span>
        <span class="girl-mouth"></span>
      </span>
      <span class="girl-bang girl-bang-left"></span>
      <span class="girl-bang girl-bang-right"></span>
      <span class="girl-bang girl-bang-center"></span>
      <span class="girl-ribbon girl-ribbon-left"></span>
      <span class="girl-ribbon girl-ribbon-right"></span>
      <span class="girl-headset girl-headset-left"></span>
      <span class="girl-headset girl-headset-right"></span>
      <span class="girl-halo"></span>
      <span class="girl-mascot">
        <span class="mascot-eye mascot-eye-left"></span>
        <span class="mascot-eye mascot-eye-right"></span>
      </span>
      <span class="girl-sparkle girl-sparkle-one"></span>
      <span class="girl-sparkle girl-sparkle-two"></span>
    </div>

    <header class="chat-header">
      <button class="ghost-button" type="button" @click="$emit('back')">返回主页</button>
      <div>
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
        <span>会话：{{ displayChatId }}</span>
      </div>
      <button class="secondary-button" type="button" @click="startNewChat">新会话</button>
    </header>

    <div ref="messagesEl" class="messages">
      <article
        v-for="message in messages"
        :key="message.id"
        class="message-row"
        :class="message.role"
      >
        <div class="avatar">{{ message.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">
          <p v-if="message.content">{{ message.content }}</p>
          <p v-else class="typing">正在思考...</p>
        </div>
      </article>
    </div>

    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

    <form class="composer" @submit.prevent="sendMessage">
      <textarea
        v-model="inputMessage"
        :placeholder="placeholder"
        rows="2"
        @keydown.enter.exact.prevent="sendMessage"
      />
      <button type="submit" :disabled="!canSend">
        {{ isStreaming ? '回复中...' : '发送' }}
      </button>
    </form>
  </section>
</template>
