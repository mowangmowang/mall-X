<template>
	<view>
		<!-- 遮罩层 -->
		<view class="ai-mask" :class="visible ? 'show' : ''" @click="handleClose"></view>
		<!-- 聊天面板 -->
		<view class="ai-chat-panel" :class="visible ? 'show' : ''">
			<view class="chat-header">
				<text class="chat-title">{{title}}</text>
				<text class="chat-close" @click="handleClose">✕</text>
			</view>
			<scroll-view class="chat-messages" scroll-y :scroll-into-view="scrollToId" ref="msgScroll">
				<view v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
					<view class="avatar" v-if="msg.role === 'ai'">AI</view>
					<view class="bubble">{{msg.content}}</view>
					<view class="avatar" v-if="msg.role === 'user'">我</view>
				</view>
				<view v-if="loading" class="message-row ai">
					<view class="avatar">AI</view>
					<view class="bubble typing">
						<text class="dot">.</text>
						<text class="dot">.</text>
						<text class="dot">.</text>
					</view>
				</view>
				<view id="msg-bottom"></view>
			</scroll-view>
			<view class="chat-input-area">
				<input class="chat-input" v-model="inputText" :placeholder="placeholder"
				 @confirm="sendMessage" :disabled="loading" />
				<button class="send-btn" @click="sendMessage" :disabled="loading || !inputText.trim()">发送</button>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		props: {
			visible: {
				type: Boolean,
				default: false
			},
			title: {
				type: String,
				default: 'AI购物助手'
			},
			placeholder: {
				type: String,
				default: '请输入您的问题...'
			},
			onSend: {
				type: Function,
				default: null
			}
		},
		data() {
			return {
				messages: [],
				inputText: '',
				loading: false
			}
		},
		computed: {
			scrollToId() {
				return 'msg-bottom';
			}
		},
		watch: {
			visible(val) {
				if (val && this.messages.length === 0) {
					this.messages.push({
						role: 'ai',
						content: '您好！我是AI购物助手，有什么可以帮助您的？'
					});
				}
			}
		},
		methods: {
			async sendMessage() {
				const text = this.inputText.trim();
				if (!text || this.loading) return;

				this.inputText = '';
				this.messages.push({ role: 'user', content: text });
				this.loading = true;

				if (this.onSend) {
					try {
						const reply = await this.onSend(text);
						this.messages.push({ role: 'ai', content: reply || '抱歉，暂时无法回答，请稍后再试。' });
					} catch (e) {
						this.messages.push({ role: 'ai', content: '抱歉，我遇到了一些问题，请稍后再试。' });
					}
				}
				this.loading = false;
			},
			handleClose() {
				this.$emit('close');
			}
		}
	}
</script>

<style lang="scss">
	.ai-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.4);
		z-index: 998;
		opacity: 0;
		pointer-events: none;
		transition: opacity 0.3s;

		&.show {
			opacity: 1;
			pointer-events: auto;
		}
	}

	.ai-chat-panel {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		height: 75vh;
		max-height: 600px;
		background: #fff;
		border-radius: 24upx 24upx 0 0;
		z-index: 999;
		display: flex;
		flex-direction: column;
		transform: translateY(100%);
		transition: transform 0.3s ease;

		&.show {
			transform: translateY(0);
		}

		.chat-header {
			display: flex;
			align-items: center;
			justify-content: center;
			padding: 24upx 30upx;
			border-bottom: 1px solid #f0f0f0;
			position: relative;

			.chat-title {
				font-size: 32upx;
				font-weight: 600;
				color: #171717;
			}

			.chat-close {
				position: absolute;
				right: 30upx;
				top: 50%;
				transform: translateY(-50%);
				font-size: 32upx;
				color: #999;
				padding: 10upx;
			}
		}

		.chat-messages {
			flex: 1;
			padding: 20upx 30upx;
			overflow-y: auto;
			background: #f8f8f8;

			.message-row {
				display: flex;
				margin-bottom: 24upx;
				align-items: flex-start;

				&.ai {
					.avatar {
						background: #171717;
						color: #fff;
						margin-right: 16upx;
					}
					.bubble {
						background: #fff;
						color: #333;
						border-top-left-radius: 4upx;
					}
				}

				&.user {
					flex-direction: row-reverse;

					.avatar {
						background: #e8e8e8;
						color: #666;
						margin-left: 16upx;
					}
					.bubble {
						background: #171717;
						color: #fff;
						border-top-right-radius: 4upx;
					}
				}

				.avatar {
					width: 56upx;
					height: 56upx;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					font-size: 22upx;
					font-weight: 600;
					flex-shrink: 0;
				}

				.bubble {
					max-width: 70%;
					padding: 16upx 20upx;
					border-radius: 12upx;
					font-size: 28upx;
					line-height: 1.6;
					word-break: break-all;
				}

				.typing {
					display: flex;
					align-items: center;
					padding: 20upx 24upx;

					.dot {
						font-size: 40upx;
						line-height: 1;
						animation: typingAnim 1.4s infinite;
						&:nth-child(2) { animation-delay: 0.2s; }
						&:nth-child(3) { animation-delay: 0.4s; }

						@keyframes typingAnim {
							0%, 60%, 100% { opacity: 0.3; }
							30% { opacity: 1; }
						}
					}
				}
			}
		}

		.chat-input-area {
			display: flex;
			align-items: center;
			padding: 16upx 30upx;
			border-top: 1px solid #f0f0f0;
			background: #fff;

			.chat-input {
				flex: 1;
				height: 72upx;
				border: 1px solid #e0e0e0;
				border-radius: 36upx;
				padding: 0 24upx;
				font-size: 28upx;
				background: #f5f5f5;
			}

			.send-btn {
				width: 120upx;
				height: 72upx;
				line-height: 72upx;
				margin-left: 16upx;
				background: #171717;
				color: #fff;
				font-size: 28upx;
				border-radius: 36upx;
				text-align: center;
				padding: 0;

				&[disabled] {
					opacity: 0.5;
				}
			}
		}
	}
</style>
