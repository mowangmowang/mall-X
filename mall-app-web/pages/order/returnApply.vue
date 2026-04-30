<template>
	<view class="content">
		<!-- è®¢å•ä¿¡æ¯ -->
		<view class="section">
			<view class="section-header">è®¢å•ä¿¡æ¯</view>
			<view class="info-row">
				<text class="label">è®¢å•ç¼–å·</text>
				<text class="value">{{orderSn}}</text>
			</view>
		</view>

		<!-- å•†å“ä¿¡æ¯ -->
		<view class="section">
			<view class="section-header">å•†å“ä¿¡æ¯</view>
			<view class="product-info">
				<image :src="productPic" mode="aspectFill"></image>
				<view class="product-right">
					<text class="product-name clamp">{{productName}}</text>
					<text class="product-attr">{{formatAttr(productAttr)}}</text>
					<view class="product-meta">
						<text class="product-price">ï¿¥{{productPrice}}</text>
						<text class="product-count">x {{productCount}}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- é€€è´§åŽŸå›  -->
		<view class="section">
			<view class="info-row">
				<text class="label">é€€è´§åŽŸå› </text>
				<picker mode="selector" :range="reasonList" @change="onReasonChange">
					<view class="picker-value">{{reason || 'è¯·é€‰æ‹©é€€è´§åŽŸå› '}}</view>
				</picker>
				<text class="yticon icon-you"></text>
			</view>
		</view>

		<!-- é€€æ¬¾é‡‘é¢ -->
		<view class="section">
			<view class="info-row">
				<text class="label">é€€æ¬¾é‡‘é¢</text>
				<text class="value price">ï¿¥{{refundAmount}}</text>
			</view>
		</view>

		<!-- é—®é¢˜æè¿° -->
		<view class="section">
			<view class="section-header">
				<text>é—®é¢˜æè¿°</text>
				<button class="ai-suggest-btn" @click="openAiSuggest">AI建议</button>
			</view>
			<textarea class="desc-input" v-model="description" placeholder="è¯·æè¿°é€€è´§åŽŸå› ï¼Œæœ€å¤š500å­—" maxlength="500" />
			<text class="word-count">{{description.length}}/500</text>
		</view>

		<!-- å‡­è¯å›¾ç‰‡ -->
		<view class="section">
			<view class="section-header">å‡­è¯å›¾ç‰‡</view>
			<view class="upload-list">
				<view class="upload-item" v-for="(img, index) in proofPicsList" :key="index">
					<image :src="img" mode="aspectFill"></image>
					<text class="del-btn" @click="removePic(index)">Ã—</text>
				</view>
				<view class="upload-btn" v-if="proofPicsList.length < 3" @click="chooseImage">
					<text class="plus">+</text>
					<text class="upload-tip">ä¸Šä¼ å‡­è¯</text>
				</view>
			</view>
			<text class="upload-hint">æœ€å¤šä¸Šä¼ 3å¼ å›¾ç‰‡ï¼Œä»…æ”¯æŒjpg/pngæ ¼å¼</text>
		</view>

		
		<!-- AI建议弹窗 -->
		<view class="popup" :class="showAiSuggest ? 'show' : 'none'">
			<view class="mask" @click="closeAiSuggest"></view>
			<view class="layer">
				<view class="ai-suggest-title">AI售后建议</view>
				<textarea class="ai-issue-input" v-model="aiIssue" placeholder="请描述您遇到的问题..." maxlength="500" />
				<button class="ai-submit-btn" :disabled="aiSuggesting" @click="getAiSuggest">{{aiSuggesting ? '建议生成中...' : '获取建议'}}</button>
			</view>
		</view>

<!-- æäº¤æŒ‰é’® -->
		<view class="footer">
			<button class="submit-btn" :disabled="submitting" @click="submit">{{submitting ? 'æäº¤ä¸­...' : 'æäº¤ç”³è¯·'}}</button>
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
				reasonList: ['è´¨é‡é—®é¢˜', 'å•†å“ä¸Žæè¿°ä¸ç¬¦', 'ä¸æƒ³è¦äº†', 'å•†å“æŸå', 'å…¶ä»–'],
				reason: '',
				reasonIndex: -1,
				description: '',
				proofPicsList: [],
				submitting: false,
				showAiSuggest: false,
				aiIssue: '',
				aiSuggesting: false
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
						uni.showLoading({ title: 'ä¸Šä¼ ä¸­...', mask: true });
						// ä½¿ç”¨ä¸´æ—¶è·¯å¾„ä½œä¸ºå‡­è¯ï¼ˆå®žé™…ç”Ÿäº§çŽ¯å¢ƒåº”ä¸Šä¼ åˆ°OSSï¼‰
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
					uni.showToast({ title: 'è¯·é€‰æ‹©é€€è´§åŽŸå› ', icon: 'none' });
					return;
				}
				if (!this.description.trim()) {
					uni.showToast({ title: 'è¯·æè¿°é€€è´§é—®é¢˜', icon: 'none' });
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
					uni.showToast({ title: 'ç”³è¯·æäº¤æˆåŠŸ', icon: 'success' });
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
	/* AI建议弹窗 */
	.ai-suggest-btn {
		background: #171717;
		color: #fff;
		font-size: 24upx;
		padding: 8upx 24upx;
		border-radius: 8upx;
		border: none;
		margin-left: auto;
		line-height: 2;
	}
	.ai-suggest-title {
		font-size: 32upx;
		font-weight: 600;
		padding: 30upx;
		text-align: center;
		color: #333;
	}
	.ai-issue-input {
		width: 85%;
		height: 200upx;
		margin: 0 auto;
		padding: 20upx;
		border: 1px solid #e0e0e0;
		border-radius: 8upx;
		font-size: 28upx;
		color: #333;
		display: block;
	}
	.ai-submit-btn {
		display: block;
		width: 85%;
		height: 80upx;
		line-height: 80upx;
		background: #171717;
		color: #fff;
		font-size: 30upx;
		border-radius: 12upx;
		margin: 30upx auto;
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



















