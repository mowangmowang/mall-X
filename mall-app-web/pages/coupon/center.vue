<template>
	<view class="content">
		<view class="header-banner">
			<text class="banner-title">领券中心</text>
			<text class="banner-subtitle">海量优惠券等你来领</text>
		</view>
		<view class="coupon-item" v-for="(item,index) in couponList" :key="index">
			<view class="con">
				<view class="left">
					<text class="title">{{item.name}}</text>
					<text class="time">有效期至{{item.endTime | formatDateTime}}</text>
				</view>
				<view class="right">
					<text class="price">{{item.amount}}</text>
					<text>满{{item.minPoint}}可用</text>
				</view>
				<view class="circle l"></view>
				<view class="circle r"></view>
			</view>
			<view class="bottom-bar">
				<text class="tips">{{item.useType | formatCouponUseType}}</text>
				<text class="claim-btn" :class="{claimed: item.claimed}" @click="claimCoupon(item)">{{item.claimed ? '已领取' : '立即领取'}}</text>
			</view>
		</view>
		<view class="empty-state" v-if="couponList.length === 0 && !loading">
			<text>暂无可用优惠券</text>
		</view>
		<uni-load-more :status="loadingType"></uni-load-more>
	</view>
</template>

<script>
	import {
		fetchAvailableCouponList,
		addMemberCoupon
	} from '@/api/coupon.js';
	import {
		formatDate
	} from '@/utils/date';
	import uniLoadMore from '@/components/uni-load-more/uni-load-more.vue';
	export default {
		components: { uniLoadMore },
		data() {
			return {
				couponList: [],
				loadingType: 'loading',
				loading: true
			};
		},
		onLoad() {
			this.loadData();
		},
		filters: {
			formatDateTime(time) {
				if (time == null || time === '') {
					return 'N/A';
				}
				let date = new Date(time);
				return formatDate(date, 'yyyy-MM-dd hh:mm:ss')
			},
			formatCouponUseType(useType) {
				if (useType == 0) {
					return "全场通用";
				} else if (useType == 1) {
					return "指定分类商品可用";
				} else if (useType == 2) {
					return "指定商品可用";
				}
				return null;
			}
		},
		methods: {
			loadData() {
				this.loading = true;
				this.loadingType = 'loading';
				fetchAvailableCouponList().then(response => {
					this.couponList = response.data || [];
					this.loading = false;
					this.loadingType = this.couponList.length > 0 ? 'more' : 'nomore';
				}).catch(() => {
					this.loading = false;
					this.loadingType = 'nomore';
				});
			},
			claimCoupon(item) {
				if (item.claimed) return;
				addMemberCoupon(item.id).then(response => {
					if (response.code === 200) {
						uni.showToast({ title: '领取成功', icon: 'success' });
						item.claimed = true;
					} else {
						uni.showToast({ title: response.message || '领取失败', icon: 'none' });
					}
				}).catch(() => {
					uni.showToast({ title: '领取失败，请重试', icon: 'none' });
				});
			}
		}
	}
</script>

<style lang='scss'>
	page {
		background: $color-bg-secondary;
		padding-bottom: 100upx;
	}

	.header-banner {
		padding: 50upx 30upx;
		background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
		text-align: center;

		.banner-title {
			font-size: 44upx;
			color: #fff;
			font-weight: 700;
		}

		.banner-subtitle {
			display: block;
			font-size: 26upx;
			color: rgba(255,255,255,0.85);
			margin-top: 12upx;
		}
	}

	.coupon-item {
		display: flex;
		flex-direction: column;
		margin: 20upx 24upx;
		background: #fff;
		border-radius: 12upx;
		overflow: hidden;

		.con {
			display: flex;
			align-items: center;
			position: relative;
			height: 120upx;
			padding: 0 30upx;

			&:after {
				position: absolute;
				left: 0;
				bottom: 0;
				content: '';
				width: 100%;
				height: 0;
				border-bottom: 1px dashed $color-border;
				transform: scaleY(50%);
			}
		}

		.left {
			display: flex;
			flex-direction: column;
			justify-content: center;
			flex: 1;
			overflow: hidden;
			height: 100upx;
		}

		.title {
			font-size: 32upx;
			color: $font-color-dark;
			margin-bottom: 10upx;
		}

		.time {
			font-size: 24upx;
			color: $font-color-light;
		}

		.right {
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-items: center;
			font-size: 26upx;
			color: $font-color-base;
			height: 100upx;
		}

		.price {
			font-size: 44upx;
			color: $base-color;

			&:before {
				content: '￥';
				font-size: 34upx;
			}
		}

		.bottom-bar {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 0 30upx;
			height: 60upx;
		}

		.tips {
			font-size: 24upx;
			color: $font-color-light;
		}

		.claim-btn {
			font-size: 24upx;
			color: #fff;
			background: $base-color;
			padding: 6upx 24upx;
			border-radius: 30upx;
			line-height: 1.8;

			&:active {
				opacity: 0.8;
			}

			&.claimed {
				background: $color-secondary;
			}
		}

		.circle {
			position: absolute;
			left: -6upx;
			bottom: -10upx;
			z-index: 10;
			width: 20upx;
			height: 20upx;
			background: $color-bg-secondary;
			border-radius: 100px;

			&.r {
				left: auto;
				right: -6upx;
			}
		}
	}

	.empty-state {
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 100upx 0;
		font-size: 28upx;
		color: $font-color-light;
	}
</style>
