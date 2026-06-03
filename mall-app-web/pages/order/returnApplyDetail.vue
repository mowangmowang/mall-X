<template>
	<view class="content">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-container">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 详情内容 -->
		<view v-else-if="returnApply" class="detail-container">
			<!-- 售后状态 -->
			<view class="status-section">
				<view class="status-icon" :style="{background: statusColor(returnApply.status)}">
					<text class="yticon" :class="statusIcon(returnApply.status)"></text>
				</view>
				<view class="status-info">
					<text class="status-text">{{returnApply.status | formatReturnStatus}}</text>
					<text class="status-tip">{{statusTip(returnApply.status)}}</text>
				</view>
			</view>

			<!-- 订单信息 -->
			<view class="section">
				<view class="section-header">订单信息</view>
				<view class="info-row">
					<text class="label">订单编号</text>
					<text class="value">{{returnApply.orderSn}}</text>
				</view>
				<view class="info-row">
					<text class="label">申请时间</text>
					<text class="value">{{returnApply.createTime | formatDateTime}}</text>
				</view>
			</view>

			<!-- 商品信息 -->
			<view class="section">
				<view class="section-header">商品信息</view>
				<view class="product-info">
					<image :src="returnApply.productPic" mode="aspectFill"></image>
					<view class="product-right">
						<text class="product-name clamp">{{returnApply.productName}}</text>
						<text class="product-attr">{{formatAttr(returnApply.productAttr)}}</text>
						<view class="product-meta">
							<text class="product-price">¥{{returnApply.productPrice}}</text>
							<text class="product-count">x {{returnApply.productCount}}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 退款信息 -->
			<view class="section">
				<view class="section-header">退款信息</view>
				<view class="info-row">
					<text class="label">退货原因</text>
					<text class="value">{{returnApply.reason}}</text>
				</view>
				<view class="info-row">
					<text class="label">退款金额</text>
					<text class="value price">¥{{returnApply.returnAmount || (returnApply.productRealPrice * returnApply.productCount).toFixed(2)}}</text>
				</view>
			</view>

			<!-- 问题描述 -->
			<view class="section" v-if="returnApply.description">
				<view class="section-header">问题描述</view>
				<view class="desc-content">{{returnApply.description}}</view>
			</view>

			<!-- 凭证图片 -->
			<view class="section" v-if="returnApply.proofPics">
				<view class="section-header">凭证图片</view>
				<view class="proof-pics">
					<image 
						v-for="(pic, index) in returnApply.proofPics.split(',')" 
						:key="index" 
						:src="pic" 
						mode="aspectFill"
						@click="previewImage(pic)"
					></image>
				</view>
			</view>

			<!-- 处理信息 -->
			<view class="section" v-if="returnApply.handleNote || returnApply.status > 0">
				<view class="section-header">处理信息</view>
				<view class="info-row" v-if="returnApply.handleMan">
					<text class="label">处理人员</text>
					<text class="value">{{returnApply.handleMan}}</text>
				</view>
				<view class="info-row" v-if="returnApply.handleTime">
					<text class="label">处理时间</text>
					<text class="value">{{returnApply.handleTime | formatDateTime}}</text>
				</view>
				<view class="info-row" v-if="returnApply.handleNote">
					<text class="label">处理备注</text>
					<text class="value">{{returnApply.handleNote}}</text>
				</view>
				<view class="info-row" v-if="returnApply.returnAmount && returnApply.status === 1">
					<text class="label">确认退款</text>
					<text class="value price">¥{{returnApply.returnAmount}}</text>
				</view>
			</view>

			<!-- 收货信息 -->
			<view class="section" v-if="returnApply.receiveMan">
				<view class="section-header">收货信息</view>
				<view class="info-row" v-if="returnApply.receiveMan">
					<text class="label">收货人</text>
					<text class="value">{{returnApply.receiveMan}}</text>
				</view>
				<view class="info-row" v-if="returnApply.receiveTime">
					<text class="label">收货时间</text>
					<text class="value">{{returnApply.receiveTime | formatDateTime}}</text>
				</view>
				<view class="info-row" v-if="returnApply.receiveNote">
					<text class="label">收货备注</text>
					<text class="value">{{returnApply.receiveNote}}</text>
				</view>
			</view>
		</view>

		<!-- 底部操作按钮 -->
		<view class="footer" v-if="returnApply && returnApply.status === 0">
			<button class="cancel-btn" @click="cancelApply">取消申请</button>
		</view>
	</view>
</template>

<script>
	import { fetchReturnApplyDetail, cancelReturnApply } from '@/api/order.js';
	import { formatDate } from '@/utils/date.js';

	export default {
		data() {
			return {
				applyId: '',
				returnApply: null,
				loading: true
			}
		},
		filters: {
			formatReturnStatus(status) {
				const map = { 0: '待处理', 1: '退货中', 2: '已完成', 3: '已拒绝' };
				return map[+status] || '未知';
			},
			formatDateTime(time) {
				if (!time) return 'N/A';
				return formatDate(new Date(time), 'yyyy-MM-dd hh:mm:ss')
			},
		},
		onLoad(options) {
			this.applyId = options.id;
			this.loadDetail();
		},
		methods: {
			async loadDetail() {
				this.loading = true;
				try {
					const response = await fetchReturnApplyDetail(this.applyId);
					if (response.code === 200 && response.data) {
						this.returnApply = response.data;
						console.log('售后详情:', this.returnApply);
					} else {
						uni.showToast({ title: response.message || '加载失败', icon: 'none' });
					}
				} catch (error) {
					console.error('加载售后详情失败:', error);
					uni.showToast({ title: '加载失败，请重试', icon: 'none' });
				} finally {
					this.loading = false;
				}
			},
			formatAttr(attr) {
				if (!attr) return '';
				try {
					const arr = JSON.parse(attr);
					return arr.map(a => a.key + ':' + a.value).join('; ');
				} catch(e) { 
					return attr; 
				}
			},
			previewImage(url) {
				const pics = this.returnApply.proofPics.split(',');
				uni.previewImage({
					urls: pics,
					current: url
				});
			},
			statusColor(status) {
				const map = { 
					0: 'linear-gradient(135deg, #ff9800, #f57c00)', 
					1: 'linear-gradient(135deg, #2196f3, #1976d2)', 
					2: 'linear-gradient(135deg, #4caf50, #388e3c)', 
					3: 'linear-gradient(135deg, #f44336, #d32f2f)' 
				};
				return map[+status] || '#999';
			},
			statusIcon(status) {
				const map = { 0: 'icon-iconfontshijian', 1: 'icon-yunshu', 2: 'icon-wancheng', 3: 'icon-cuowu' };
				return map[+status] || 'icon-iconfontshijian';
			},
			statusTip(status) {
				const map = { 
					0: '商家将在24小时内处理您的申请', 
					1: '请按商家提供的地址寄回商品', 
					2: '退款已完成，请注意查收', 
					3: '商家拒绝了您的申请，请联系客服' 
				};
				return map[+status] || '';
			},
			cancelApply() {
				uni.showModal({
					title: '提示',
					content: '确定要取消该售后申请吗？',
					success: (res) => {
						if (res.confirm) {
							uni.showLoading({ title: '取消中...', mask: true });
							cancelReturnApply(this.applyId).then(() => {
								uni.hideLoading();
								uni.showToast({ title: '已取消', icon: 'success' });
								setTimeout(() => {
									uni.navigateBack();
								}, 1000);
							}).catch(() => {
								uni.hideLoading();
							});
						}
					}
				});
			}
		}
	}
</script>

<style lang="scss">
	page {
		background: $color-bg-secondary;
		padding-bottom: 120upx;
	}

	.loading-container {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 60vh;
		
		.loading-text {
			font-size: 28upx;
			color: $font-color-light;
		}
	}

	.detail-container {
		/* 状态区域 */
		.status-section {
			display: flex;
			align-items: center;
			padding: 40upx 30upx;
			background: #fff;
			margin-bottom: 16upx;

			.status-icon {
				width: 100upx;
				height: 100upx;
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
				margin-right: 24upx;
				flex-shrink: 0;

				.yticon {
					font-size: 48upx;
					color: #fff;
				}
			}

			.status-info {
				flex: 1;
				display: flex;
				flex-direction: column;

				.status-text {
					font-size: 32upx;
					font-weight: 600;
					color: $font-color-dark;
					margin-bottom: 8upx;
				}

				.status-tip {
					font-size: 24upx;
					color: $font-color-light;
				}
			}
		}

		/* 通用section */
		.section {
			background: #fff;
			margin-bottom: 16upx;
			padding: 0 30upx;

			.section-header {
				font-size: 30upx;
				color: $font-color-dark;
				font-weight: 600;
				padding: 30upx 0 20upx;
				border-bottom: 1px solid $color-border;
			}

			.info-row {
				display: flex;
				align-items: center;
				min-height: 88upx;
				padding: 10upx 0;
				border-bottom: 1px solid $color-border;

				.label {
					font-size: 28upx;
					color: $font-color-light;
					width: 160upx;
					flex-shrink: 0;
				}

				.value {
					font-size: 28upx;
					color: $font-color-dark;
					flex: 1;

					&.price {
						color: $base-color;
						font-size: 34upx;
						font-weight: 600;
					}
				}

				&:last-child {
					border-bottom: none;
				}
			}

			.desc-content {
				padding: 20upx 0;
				font-size: 28upx;
				color: $font-color-dark;
				line-height: 1.6;
			}

			.proof-pics {
				display: flex;
				flex-wrap: wrap;
				padding: 20upx 0;
				gap: 16upx;

				image {
					width: 200upx;
					height: 200upx;
					border-radius: $radius-base;
				}
			}
		}

		/* 商品信息 */
		.product-info {
			display: flex;
			padding: 24upx 0;

			image {
				width: 160upx;
				height: 160upx;
				border-radius: $radius-base;
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
				color: $font-color-dark;
				font-weight: 500;
			}

			.product-attr {
				font-size: 24upx;
				color: $font-color-light;
				margin: 8upx 0;
			}

			.product-meta {
				display: flex;
				align-items: center;
			}

			.product-price {
				font-size: 28upx;
				color: $base-color;
				font-weight: 600;
			}

			.product-count {
				font-size: 24upx;
				color: $font-color-light;
				margin-left: 16upx;
			}
		}
	}

	/* 底部按钮 */
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

	.cancel-btn {
		width: 100%;
		height: 88upx;
		line-height: 88upx;
		background: #fff;
		color: $font-color-dark;
		font-size: 32upx;
		border-radius: $radius-lg;
		text-align: center;
		border: 1px solid $color-border;

		&:active {
			opacity: 0.8;
		}
	}
</style>
