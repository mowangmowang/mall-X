<template>
	<view class="content">
		<!-- 订单信息 -->
		<view class="section">
			<view class="section-header">订单信息</view>
			<view class="info-row">
				<text class="label">订单编号</text>
				<text class="value">{{orderSn}}</text>
			</view>
		</view>

		<!-- 商品信息 -->
		<view class="section">
			<view class="section-header">商品信息</view>
			<view class="product-info">
				<image :src="productPic" mode="aspectFill"></image>
				<view class="product-right">
					<text class="product-name clamp">{{productName}}</text>
					<text class="product-attr">{{formatAttr(productAttr)}}</text>
					<view class="product-meta">
						<text class="product-price">¥{{productPrice}}</text>
						<text class="product-count">x {{productCount}}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 退货原因 -->
		<view class="section">
			<view class="info-row">
				<text class="label">退货原因</text>
				<picker mode="selector" :range="reasonList" @change="onReasonChange">
					<view class="picker-value">{{reason || '请选择退货原因'}}</view>
				</picker>
				<text class="yticon icon-you"></text>
			</view>
		</view>

		<!-- 退款金额 -->
		<view class="section">
			<view class="info-row">
				<text class="label">退款金额</text>
				<text class="value price">¥{{refundAmount}}</text>
			</view>
		</view>

		<!-- 问题描述 -->
		<view class="section">
			<view class="section-header">
				<text>问题描述</text>
			</view>
			<!-- AI 建议提示按钮 -->
			<view class="ai-suggest-tip" @click="openAiSuggest">
				<text class="ai-tip-icon">✨</text>
				<text class="ai-tip-text">不知道如何描述？让 AI 帮你生成问题描述</text>
				<text class="yticon icon-you"></text>
			</view>
			<textarea 
				class="desc-input" 
				:class="{ 'highlight-change': highlightDescription }"
				v-model="description" 
				placeholder="请描述退货原因，最多500字" 
				maxlength="500" 
			/>
			<text class="word-count">{{description.length}}/500</text>
		</view>

		<!-- 凭证图片 -->
		<view class="section">
			<view class="section-header">凭证图片</view>
			<view class="upload-list">
				<view class="upload-item" v-for="(img, index) in proofPicsList" :key="index">
					<image :src="img" mode="aspectFill"></image>
					<text class="del-btn" @click="removePic(index)">Ã—</text>
				</view>
				<view class="upload-btn" v-if="proofPicsList.length < 3" @click="chooseImage">
					<text class="plus">+</text>
					<text class="upload-tip">上传凭证</text>
				</view>
			</view>
			<text class="upload-hint">最多上传3张图片，仅支持jpg/png格式</text>
		</view>

		
		<!-- AI建议弹窗 -->
		<view class="popup" :class="showAiSuggest ? 'show' : 'none'">
			<view class="mask" @click="closeAiSuggest"></view>
			<view class="layer">
				<view class="ai-suggest-header">
					<view class="ai-suggest-title">AI售后建议</view>
					<text class="close-icon" @click="closeAiSuggest">✕</text>
				</view>
				<textarea class="ai-issue-input" v-model="aiIssue" placeholder="请描述您遇到的问题..." maxlength="500" />
				<view class="ai-actions">
					<button class="ai-cancel-btn" @click="closeAiSuggest">取消</button>
					<button class="ai-submit-btn" :disabled="aiSuggesting" @click="getAiSuggest">
						{{aiSuggesting ? '建议生成中...' : '获取建议'}}
					</button>
				</view>
			</view>
		</view>

<!-- 提交按钮 -->
		<view class="footer">
			<button class="submit-btn" :disabled="submitting" @click="submit">{{submitting ? '提交中...' : '提交申请'}}</button>
		</view>
	</view>
</template>

<script>
	import { createReturnApply } from '@/api/order.js';
	import { aiReturnSuggest } from '@/api/ai.js';
	export default {
		data() {
			return {
				orderId: '',
				orderSn: '',
				productId: '',
				productName: '',
				productPic: '',
				productBrand: '',
				productAttr: '',
				productCount: 1,
				productPrice: 0,
				productRealPrice: 0,
				reasonList: ['质量问题', '商品与描述不符', '不想要了', '商品损坏', '其他'],
				reason: '',
				reasonIndex: -1,
				description: '',
				proofPicsList: [],
				submitting: false,
				showAiSuggest: false,
				aiIssue: '',
				aiSuggesting: false,
				highlightDescription: false
			}
		},
		computed: {
			refundAmount() {
				return (this.productRealPrice * this.productCount).toFixed(2);
			}
		},
		onLoad(option) {
			this.orderId = option.orderId || '';
			this.orderSn = option.orderSn || '';
			this.productId = option.productId || '';
			this.productName = decodeURIComponent(option.productName || '');
			this.productPic = decodeURIComponent(option.productPic || '');
			this.productBrand = decodeURIComponent(option.productBrand || '');
			this.productAttr = decodeURIComponent(option.productAttr || '');
			this.productCount = parseInt(option.productCount || 1);
			this.productPrice = parseFloat(option.productPrice || 0);
			this.productRealPrice = parseFloat(option.productRealPrice || option.productPrice || 0);
		},
		methods: {
				stopPrevent() {},
			formatAttr(attr) {
				if (!attr) return '';
				try {
					const arr = JSON.parse(attr);
					return arr.map(a => a.key + ':' + a.value).join('; ');
				} catch(e) { return attr; }
			},
			onReasonChange(e) {
				this.reasonIndex = e.detail.value;
				this.reason = this.reasonList[this.reasonIndex];
			},
			chooseImage() {
				uni.chooseImage({
					count: 3 - this.proofPicsList.length,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						const tempFiles = res.tempFilePaths;
						uni.showLoading({ title: '上传中...', mask: true });
						// 使用临时路径作为凭证（实际生产环境应上传到OSS）
						for (let i = 0; i < tempFiles.length; i++) {
							if (this.proofPicsList.length < 3) {
								this.proofPicsList.push(tempFiles[i]);
							}
						}
						uni.hideLoading();
					}
				});
			},
			removePic(index) {
				this.proofPicsList.splice(index, 1);
			},
			submit() {
				if (!this.reason) {
					uni.showToast({ title: '请选择退货原因', icon: 'none' });
					return;
				}
				if (!this.description.trim()) {
					uni.showToast({ title: '请描述退货问题', icon: 'none' });
					return;
				}
				this.submitting = true;
				const data = {
					orderId: parseInt(this.orderId),
					orderSn: this.orderSn,
					productId: parseInt(this.productId),
					productName: this.productName,
					productPic: this.productPic,
					productBrand: this.productBrand,
					productAttr: this.productAttr,
					productCount: this.productCount,
					productPrice: this.productPrice,
					productRealPrice: this.productRealPrice,
					reason: this.reason,
					description: this.description,
					proofPics: this.proofPicsList.join(',')
				};
				createReturnApply(data).then(() => {
					this.submitting = false;
					uni.showToast({ title: '申请提交成功', icon: 'success' });
					setTimeout(() => {
						uni.redirectTo({ url: '/pages/order/order?state=5' });
					}, 1000);
				}).catch(() => {
					this.submitting = false;
				});
			},
			openAiSuggest() {
				this.aiIssue = "";
				this.showAiSuggest = true;
			},
			closeAiSuggest() {
				this.showAiSuggest = false;
			},
			getAiSuggest() {
				if (!this.aiIssue.trim()) return;
				this.aiSuggesting = true;
				aiReturnSuggest({
					issue: this.aiIssue,
					productName: this.productName,
					productAttr: this.productAttr,
					orderSn: this.orderSn
				}).then(res => {
					const data = res.data;
					const idx = this.reasonList.indexOf(data.suggestedReason);
					if (idx !== -1) {
						this.reason = data.suggestedReason;
						this.reasonIndex = idx;
					}
					this.description = data.suggestedDescription;
					this.closeAiSuggest();
					uni.showToast({ title: "已自动填写建议内容", icon: "success" });
					
					// 高亮显示被修改的字段
					this.highlightDescription = true;
					setTimeout(() => {
						this.highlightDescription = false;
					}, 2000);
				}).catch(() => {
					uni.showToast({ title: "获取建议失败", icon: "none" });
				}).finally(() => {
					this.aiSuggesting = false;
				});
			}
		}
	}
</script>

<style lang="scss">
	page {
		background: color-bg-secondary;
		padding-bottom: 120upx;
	}

	.section {
		background: #fff;
		margin-top: 16upx;
		padding: 0 30upx;

		.section-header {
			display: flex;
			align-items: center;
			font-size: 30upx;
			color: font-color-dark;
			font-weight: 600;
			padding: 30upx 0 20upx;
			border-bottom: 1px solid color-border;
		}
	}

	.info-row {
		display: flex;
		align-items: center;
		min-height: 88upx;
		padding: 10upx 0;
		border-bottom: 1px solid color-border;

		.label {
			font-size: 28upx;
			color: font-color-light;
			width: 160upx;
			flex-shrink: 0;
		}

		.value {
			font-size: 28upx;
			color: font-color-dark;
			flex: 1;

			&.price {
				color: base-color;
				font-size: 34upx;
				font-weight: 600;
			}
		}

		.picker-value {
			font-size: 28upx;
			color: font-color-dark;
			flex: 1;
		}

		.icon-you {
			font-size: 28upx;
			color: font-color-light;
		}

		&:last-child {
			border-bottom: none;
		}
	}

	.product-info {
		display: flex;
		padding: 24upx 0;
		border-bottom: 1px solid color-border;

		image {
			width: 160upx;
			height: 160upx;
			border-radius: radius-base;
			flex-shrink: 0;
		}

		.product-right {
			flex: 1;
			padding-left: 20upx;
			display: flex;
			flex-direction: column;
			justify-content: center;
		}

		.product-name {
			font-size: 28upx;
			color: font-color-dark;
			font-weight: 500;
		}

		.product-attr {
			font-size: 24upx;
			color: font-color-light;
			margin: 8upx 0;
		}

		.product-meta {
			display: flex;
			align-items: center;
		}

		.product-price {
			font-size: 28upx;
			color: base-color;
			font-weight: 600;
		}

		.product-count {
			font-size: 24upx;
			color: font-color-light;
			margin-left: 16upx;
		}
	}

	.desc-input {
		width: 100%;
		height: 200upx;
		font-size: 28upx;
		color: font-color-dark;
		padding: 20upx 0;
		line-height: 1.6;
		transition: background 0.3s;
		
		&.highlight-change {
			animation: highlightPulse 2s ease;
		}
	}
	
	@keyframes highlightPulse {
		0%, 100% { background: #fff; }
		50% { background: #fff9c4; }
	}

	.word-count {
		display: block;
		text-align: right;
		font-size: 24upx;
		color: font-color-light;
		padding-bottom: 20upx;
	}

	.upload-list {
		display: flex;
		flex-wrap: wrap;
		padding: 20upx 0;
	}

	.upload-item {
		width: 180upx;
		height: 180upx;
		border-radius: radius-base;
		overflow: hidden;
		position: relative;
		margin-right: 16upx;
		margin-bottom: 16upx;

		image {
			width: 100%;
			height: 100%;
		}

		.del-btn {
			position: absolute;
			top: 0;
			right: 0;
			width: 36upx;
			height: 36upx;
			background: rgba(0, 0, 0, 0.5);
			color: #fff;
			text-align: center;
			line-height: 36upx;
			font-size: 28upx;
			border-radius: 0 radius-base 0radius-base;
		}
	}

	.upload-btn {
		width: 180upx;
		height: 180upx;
		border: 2upx dashed color-border;
		border-radius: radius-base;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		margin-bottom: 16upx;

		.plus {
			font-size: 48upx;
			color: font-color-light;
		}

		.upload-tip {
			font-size: 22upx;
			color: font-color-light;
			margin-top: 8upx;
		}
	}

	.upload-hint {
		display: block;
		font-size: 22upx;
		color: font-color-light;
		padding-bottom: 20upx;
	}

	.footer {
		position: fixed;
		left: 0;
		bottom: 0;
		width: 100%;
		padding: 20upx 30upx;
		background: #fff;
		box-shadow: 0 -2upx 10upx rgba(0, 0, 0, 0.05);
		box-sizing: border-box;
	}

	.submit-btn {
		width: 100%;
		height: 88upx;
		line-height: 88upx;
		background: base-color;
		color: #fff;
		font-size: 32upx;
		border-radius: radius-lg;
		text-align: center;

		&[disabled] {
			opacity: 0.6;
		}

		&:active {
			opacity: 0.8;
		}
	}
	/* AI 建议提示按钮 */
	.ai-suggest-tip {
		display: flex;
		align-items: center;
		padding: 20upx 24upx;
		margin: 0 0 20upx 0;
		background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
		border: 1px solid #667eea30;
		border-radius: 12upx;
		cursor: pointer;
		transition: all 0.2s;
		
		&:active {
			background: linear-gradient(135deg, #667eea25 0%, #764ba225 100%);
			transform: scale(0.98);
		}
		
		.ai-tip-icon {
			font-size: 32upx;
			margin-right: 12upx;
		}
		
		.ai-tip-text {
			flex: 1;
			font-size: 26upx;
			color: #667eea;
			font-weight: 500;
		}
		
		.icon-you {
			font-size: 24upx;
			color: #667eea;
		}
	}
	
	/* AI 建议弹窗 */
	.ai-suggest-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30upx;
		border-bottom: 1px solid #f0f0f0;
		
		.ai-suggest-title {
			font-size: 32upx;
			font-weight: 600;
			color: #333;
		}
		
		.close-icon {
			font-size: 32upx;
			color: #999;
			padding: 10upx;
			min-width: 88upx;
			min-height: 88upx;
			display: flex;
			align-items: center;
			justify-content: center;
		}
	}
	
	.ai-issue-input {
		width: 85%;
		height: 200upx;
		margin: 30upx auto 0;
		padding: 20upx;
		border: 1px solid #e0e0e0;
		border-radius: 8upx;
		font-size: 28upx;
		color: #333;
		display: block;
	}
	
	.ai-actions {
		display: flex;
		gap: 20upx;
		padding: 30upx;
	}
	
	.ai-cancel-btn {
		flex: 1;
		height: 80upx;
		line-height: 80upx;
		background: #f5f5f5;
		color: #666;
		font-size: 30upx;
		border-radius: 12upx;
		text-align: center;
		padding: 0;
	}
	
	.ai-submit-btn {
		flex: 1;
		height: 80upx;
		line-height: 80upx;
		background: #171717;
		color: #fff;
		font-size: 30upx;
		border-radius: 12upx;
		text-align: center;
		padding: 0;
	}
	
	.ai-submit-btn[disabled] {
		opacity: 0.6;
	}
	
	.popup {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		z-index: 999;
	}
	.popup.show {
		display: block;
	}
	.popup.none {
		display: none;
	}
	.popup .mask {
		position: fixed;
		top: 0;
		width: 100%;
		height: 100%;
		z-index: 1;
		background-color: rgba(0, 0, 0, 0.4);
	}
	.popup .layer {
		position: fixed;
		z-index: 999;
		bottom: 0;
		width: 100%;
		min-height: 40vh;
		border-radius: 24upx 24upx 0 0;
		background: #fff;
		padding: 20upx 0;
	}
</style>
