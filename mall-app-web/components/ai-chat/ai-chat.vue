<template>
	<view>
		<!-- 遮罩层 -->
		<view class="ai-mask" :class="visible ? 'show' : ''" @click="handleClose"></view>
		<!-- 聊天面板 -->
		<view class="ai-chat-panel" :class="visible ? 'show' : ''">
			<view class="chat-header">
				<view class="header-left">
					<text class="chat-title">{{title}}</text>
					<text class="ai-badge">智能助手</text>
				</view>
				<view class="chat-close" @click="handleClose" aria-label="关闭聊天">
					<text class="close-icon">✕</text>
				</view>
			</view>
			<scroll-view class="chat-messages" scroll-y :scroll-into-view="scrollToId" ref="msgScroll">
				<!-- AI免责声明 -->
				<view v-if="messages.length === 1" class="ai-disclaimer">
					<text class="disclaimer-icon">ℹ️</text>
					<text class="disclaimer-text">AI 助手可能会提供不准确的信息，请以实际商品信息为准。</text>
				</view>
				
				<!-- AI 消息 -->
				<view v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
					<view v-if="msg.role === 'ai'" class="avatar">AI</view>
					<view class="bubble" :class="{'bubble-error': msg.isError}">{{msg.content}}</view>
					<view v-if="msg.role === 'user'" class="avatar">我</view>
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
				<input 
					class="chat-input" 
					:class="{ 'input-disabled': loading }"
					v-model="inputText" 
					:placeholder="loading ? 'AI思考中...' : placeholder"
					confirm-type="send"
					@confirm="sendMessage" 
					:disabled="loading"
					:adjust-position="true"
				/>
				<button 
					class="send-btn" 
					:class="{ 'btn-loading': loading }"
					@click="sendMessage" 
					:disabled="loading || !inputText.trim()"
					aria-label="发送消息"
				>
					<text v-if="loading" class="loading-icon">⏳</text>
					<text v-else>发送</text>
				</button>
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
				if (val) {
					// 监听返回键
					// #ifdef APP-PLUS
					uni.onBackPress(() => {
						this.handleClose();
						return true; // 阻止默认返回行为
					});
					// #endif
					
					if (this.messages.length === 0) {
						this.messages.push({
							role: 'ai',
							content: '您好！我是AI购物助手，有什么可以帮助您的？'
						});
					}
				} else {
					// 移除返回键监听
					// #ifdef APP-PLUS
					uni.offBackPress();
					// #endif
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
						// 构建对话历史上下文（最近5轮）
						const recentMessages = this.messages.slice(-10); // 取最近10条消息（5轮对话）
						const conversationHistory = recentMessages
							.filter((msg, idx) => idx < recentMessages.length - 1) // 排除当前最新消息
							.map(msg => `${msg.role === 'user' ? '用户' : 'AI'}: ${msg.content}`)
							.join('\n');

						console.log('对话历史:', conversationHistory);

						// 传递当前问题和对话历史
						const reply = await this.onSend(text, conversationHistory);
						this.messages.push({ role: 'ai', content: reply || '抱歉，暂时无法回答，请稍后再试。' });
					} catch (e) {
						// 显示明确的错误提示
						uni.showToast({
							title: '网络请求失败，请检查网络连接',
							icon: 'none',
							duration: 2000
						});
						// 同时保留聊天中的提示
						this.messages.push({ 
							role: 'ai', 
							content: '抱歉，我暂时无法回答您的问题，请稍后再试。',
							isError: true
						});
					}
				}
				this.loading = false;
			},
			handleClose() {
				// 移除返回键监听
				// #ifdef APP-PLUS
				uni.offBackPress();
				// #endif
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
		height: 70vh;
		min-height: 400px;
		max-height: 80vh;
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
		
		@media (max-height: 600px) {
			height: 60vh;
		}

		.chat-header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 24upx 30upx;
			border-bottom: 1px solid #f0f0f0;
			position: relative;

			.header-left {
				display: flex;
				align-items: center;
				gap: 12upx;
			}

			.chat-title {
				font-size: 32upx;
				font-weight: 600;
				color: #171717;
			}
			
			.ai-badge {
				font-size: 20upx;
				padding: 4upx 12upx;
				background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
				color: #fff;
				border-radius: 20upx;
				font-weight: 500;
			}

			.chat-close {
				padding: 20upx;
				min-width: 88upx;
				min-height: 88upx;
				display: flex;
				align-items: center;
				justify-content: center;
				cursor: pointer;
				border-radius: 50%;
				transition: background 0.2s;
				
				.close-icon {
					font-size: 32upx;
					color: #999;
				}
				
				&:active {
					background: #f0f0f0;
				}
			}
		}

		.chat-messages {
			flex: 1;
			padding: 20upx 30upx;
			overflow-y: auto;
			background: #f8f8f8;
			
			.ai-disclaimer {
				margin: 0 0 20upx 0;
				padding: 20upx;
				background: #fff8e1;
				border-radius: 12upx;
				display: flex;
				align-items: flex-start;
				border-left: 4upx solid #f57c00;
				
				.disclaimer-icon {
					font-size: 28upx;
					margin-right: 12upx;
					line-height: 1.5;
				}
				
				.disclaimer-text {
					flex: 1;
					font-size: 24upx;
					color: #f57c00;
					line-height: 1.5;
				}
			}

			.message-row {
				display: flex;
				margin-bottom: 24upx;
				align-items: flex-start;

				// AI 消息：左对齐
				&.ai {
					justify-content: flex-start;
					
					.avatar {
						background: #171717;
						color: #fff;
						margin-right: 16upx;
						order: 1;
					}
					
					.bubble {
						background: #fff;
						color: #333;
						border-top-left-radius: 4upx;
						order: 2;
						
						&.bubble-error {
							background: #ffebee;
							color: #c62828;
							border: 1px solid #ef9a9a;
						}
					}
				}

				// 用户消息：右对齐
				&.user {
					justify-content: flex-end;
					
					.avatar {
						background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
						color: #fff;
						margin-left: 16upx;
						order: 2;
					}
					
					.bubble {
						background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
						color: #fff;
						border-top-right-radius: 4upx;
						order: 1;
					}
				}

				.avatar {
					width: 64upx;
					height: 64upx;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					font-size: 24upx;
					font-weight: 600;
					flex-shrink: 0;
				}

				.bubble {
					max-width: 70%;
					min-width: 80upx;
					padding: 20upx 24upx;
					border-radius: 16upx;
					font-size: 28upx;
					line-height: 1.6;
					word-break: break-word;
					box-shadow: 0 2upx 8upx rgba(0, 0, 0, 0.08);
					
					@media (min-width: 768px) {
						max-width: 50%;
					}
				}

				.typing {
					display: flex;
					align-items: center;
					padding: 24upx 28upx;

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
				transition: all 0.2s;
				
				&.input-disabled {
					opacity: 0.6;
					background: #e8e8e8;
					cursor: not-allowed;
				}
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
				transition: all 0.2s;
				display: flex;
				align-items: center;
				justify-content: center;
				min-width: 88upx;
				
				.loading-icon {
					font-size: 32upx;
					animation: rotate 1s linear infinite;
				}
				
				&.btn-loading {
					opacity: 0.7;
					background: #666;
				}

				&[disabled] {
					opacity: 0.5;
				}
				
				@keyframes rotate {
					from { transform: rotate(0deg); }
					to { transform: rotate(360deg); }
				}
			}
		}
	}
</style>
