<template>
	<view class="content">
		<view class="navbar">
			<view v-for="(item, index) in navList" :key="index" class="nav-item" :class="{current: tabCurrentIndex === index}"
			 @click="tabClick(index)">
				{{item.text}}
			</view>
		</view>

		<swiper :current="tabCurrentIndex" class="swiper-box" duration="300" @change="changeTab">
			<swiper-item class="tab-content" v-for="(tabItem,tabIndex) in navList" :key="tabIndex">
				<scroll-view class="list-scroll-content" scroll-y @scrolltolower="loadData('add')">

					<!-- 售后列表 -->
					<template v-if="navList[tabCurrentIndex].state === 99">
						<!-- 空状态 -->
						<view v-if="returnList.length === 0" class="empty-tip">
							<text class="empty-icon">📦</text>
							<text class="empty-text">暂无售后记录</text>
						</view>
						<!-- 列表 -->
						<view v-for="(item, index) in returnList" :key="item.id" class="order-item">
							<view class="i-top b-b">
								<text class="time">订单号：{{item.orderSn}}</text>
								<text class="state" :style="{color: statusColor(item.status)}">{{item.status | formatReturnStatus}}</text>
							</view>
							<view class="goods-box-single">
								<image class="goods-img" :src="item.productPic" mode="aspectFill"></image>
								<view class="right">
									<text class="title clamp">{{item.productName}}</text>
									<text class="attr-box">{{item.productAttr | formatProductAttr}} x {{item.productCount}}</text>
									<text class="price">￥{{item.productPrice}}</text>
								</view>
							</view>
							<view class="info-row">
								<text class="label">申请时间</text>
								<text class="value">{{item.createTime | formatDateTime}}</text>
							</view>
							<view class="info-row">
								<text class="label">退货原因</text>
								<text class="value">{{item.reason}}</text>
							</view>
							<view class="info-row" v-if="item.description">
								<text class="label">问题描述</text>
								<text class="value">{{item.description}}</text>
							</view>
							<view class="info-row" v-if="item.handleNote">
								<text class="label">处理备注</text>
								<text class="value">{{item.handleNote}}</text>
							</view>
							<view class="info-row" v-if="item.returnAmount">
								<text class="label">退款金额</text>
								<text class="value price">￥{{item.returnAmount}}</text>
							</view>
							<view class="action-box b-t" v-if="item.status === 0">
								<button class="action-btn" @click="cancelReturnApply(item.id)">取消申请</button>
							</view>
						</view>
						<uni-load-more :status="returnList.length === 0 ? 'nomore' : 'more'"></uni-load-more>
					</template>

					<!-- 订单列表 -->
					<template v-else>
						<!-- 空状态 -->
						<view v-if="orderList==null||orderList.length === 0" class="empty-tip">
							<text class="empty-icon">📋</text>
							<text class="empty-text">暂无订单记录</text>
						</view>
						<!-- 列表 -->
						<view v-for="(item,index) in orderList" :key="index" class="order-item">
							<view class="i-top b-b">
								<text class="time" @click="showOrderDetail(item.id)">{{item.createTime | formatDateTime}}</text>
								<text class="state" :style="{color: '#171717'}">{{item.status | formatStatus}}</text>
								<text v-if="item.status===3||item.status===4" class="del-btn yticon icon-iconfontshanchu1" @click="deleteOrder(item.id)"></text>
							</view>
							<view class="goods-box-single" v-for="(orderItem, itemIndex) in item.orderItemList"
							 :key="itemIndex">
								<image class="goods-img" :src="orderItem.productPic" mode="aspectFill"></image>
								<view class="right">
									<text class="title clamp">{{orderItem.productName}}</text>
									<text class="attr-box">{{orderItem.productAttr | formatProductAttr}} x {{orderItem.productQuantity}}</text>
									<text class="price">{{orderItem.productPrice}}</text>
								</view>
							</view>

							<view class="price-box">
								共
								<text class="num">{{calcTotalQuantity(item)}}</text>
								件商品 实付款
								<text class="price">{{item.payAmount}}</text>
							</view>
							<view class="action-box b-t" v-if="item.status == 0">
								<button class="action-btn" @click="cancelOrder(item.id)">取消订单</button>
								<button class="action-btn recom" @click="payOrder(item.id)">立即付款</button>
							</view>
							<view class="action-box b-t" v-if="item.status == 2">
								<button class="action-btn recom" @click="receiveOrder(item.id)">确认收货</button>
							</view>
							<view class="action-box b-t" v-if="item.status == 3">
								<button class="action-btn" @click="navToReturnApply(item)">申请售后</button>
							</view>
						</view>
						<uni-load-more :status="loadingType"></uni-load-more>
					</template>

				</scroll-view>
			</swiper-item>
		</swiper>
	</view>
</template>

<script>
	import uniLoadMore from '@/components/uni-load-more/uni-load-more.vue';
	import empty from "@/components/empty";
	import { formatDate } from '@/utils/date';
	import {
		fetchOrderList, cancelUserOrder, confirmReceiveOrder, deleteUserOrder,
		fetchReturnApplyList, cancelReturnApply
	} from '@/api/order.js';
	export default {
		components: { uniLoadMore, empty },
		data() {
			return {
				tabCurrentIndex: 0,
				orderParam: { status: -1, pageNum: 1, pageSize: 5 },
				orderList: [],
				returnList: [],
				loadingType: 'more',
				navList: [
					{ state: -1, text: '全部' },
					{ state: 0, text: '待付款' },
					{ state: 2, text: '待收货' },
					{ state: 3, text: '已完成' },
					{ state: 4, text: '已取消' },
					{ state: 99, text: '售后/退款' }
				],
			};
		},
		onLoad(options) {
			console.log('onLoad 触发, options:', options);
			this.tabCurrentIndex = +options.state;
			console.log('tabCurrentIndex 设置为:', this.tabCurrentIndex);
			console.log('navList[this.tabCurrentIndex]:', this.navList[this.tabCurrentIndex]);
			this.loadData();
		},
		filters: {
			formatStatus(status) {
				const map = { 0: '等待付款', 1: '等待发货', 2: '等待收货', 3: '交易完成', 4: '交易关闭' };
				return map[+status] || '';
			},
			formatReturnStatus(status) {
				const map = { 0: '待处理', 1: '退货中', 2: '已完成', 3: '已拒绝' };
				return map[+status] || '未知';
			},
			formatProductAttr(jsonAttr) {
				if (!jsonAttr) return '';
				try {
					let attrArr = JSON.parse(jsonAttr);
					return attrArr.map(a => a.key + ':' + a.value).join('; ');
				} catch(e) { return jsonAttr; }
			},
			formatDateTime(time) {
				if (time == null || time === '') return 'N/A';
				return formatDate(new Date(time), 'yyyy-MM-dd hh:mm:ss')
			},
		},
		methods: {
			loadData(type='refresh') {
				console.log('loadData 触发, tabCurrentIndex:', this.tabCurrentIndex);
				const state = this.navList[this.tabCurrentIndex].state;
				console.log('当前 state:', state);
				if (state === 99) {
					console.log('进入售后列表加载逻辑');
					this.loadReturnList();
					return;
				}
				if (type=='refresh') this.orderParam.pageNum=1;
				else this.orderParam.pageNum++;
				if (this.loadingType === 'loading') return;
				this.orderParam.status = state;
				this.loadingType = 'loading';
				fetchOrderList(this.orderParam).then(response => {
					let list = response.data.list;
					if(type=='refresh'){
						this.orderList = list;
						this.loadingType = 'more';
					}else{
						if(list&&list.length>0){
							this.orderList = this.orderList.concat(list);
							this.loadingType = 'more';
						}else{
							this.orderParam.pageNum--;
							this.loadingType = 'noMore';
						}
					}
				});
			},
			loadReturnList() {
				console.log('loadReturnList 被调用');
				uni.showLoading({ title: '加载中...', mask: true });
				fetchReturnApplyList().then(response => {
					console.log('售后列表请求成功, response:', response);
					uni.hideLoading();
					this.returnList = response.data || [];
					console.log('returnList 设置为:', this.returnList);
					this.loadingType = 'nomore';
				}).catch(error => {
					uni.hideLoading();
					console.error('获取售后列表失败:', error);
					this.returnList = [];
					this.loadingType = 'nomore';
					uni.showToast({ title: '加载失败，请重试', icon: 'none', duration: 2000 });
				});
			},
			changeTab(e) {
				this.tabCurrentIndex = e.target.current;
				this.loadData();
			},
			tabClick(index) {
				console.log('tabClick 触发, index:', index);
				this.tabCurrentIndex = index;
				this.loadData();
			},
			statusColor(status) {
				const map = { 0: '#e65100', 1: '#1565c0', 2: '#2e7d32', 3: '#c62828' };
				return map[+status] || '#171717';
			},
			cancelReturnApply(id) {
				uni.showModal({
					title: '提示',
					content: '确定要取消该售后申请吗？',
					success: (e) => {
						if (e.confirm) {
							uni.showLoading({ title: '取消中...', mask: true });
							cancelReturnApply(id).then(() => {
								uni.hideLoading();
								uni.showToast({ title: '已取消', icon: 'success' });
								this.loadReturnList();
							}).catch(() => uni.hideLoading());
						}
					}
				});
			},
			deleteOrder(orderId) {
				uni.showModal({
				    title: '提示',
				    content: '是否要删除该订单？',
				    success: (res) => {
				        if (res.confirm) {
				            uni.showLoading({ title: '请稍后' })
				            deleteUserOrder({orderId}).then(() => {
				            	uni.hideLoading();
				            	this.loadData();
				            });
				        }
				    }
				});
			},
			cancelOrder(orderId) {
				uni.showModal({
				    title: '提示',
				    content: '是否要取消该订单？',
				    success: (res) => {
				        if (res.confirm) {
				            uni.showLoading({ title: '请稍后' })
				            cancelUserOrder({orderId}).then(() => {
				            	uni.hideLoading();
				            	this.loadData();
				            });
				        }
				    }
				});
			},
			payOrder(orderId){
				uni.redirectTo({ url: `/pages/money/pay?orderId=${orderId}` });
			},
			receiveOrder(orderId){
				uni.showModal({
				    title: '提示',
				    content: '是否要确认收货？',
				    success: (res) => {
				        if (res.confirm) {
				            uni.showLoading({ title: '请稍后' })
				            confirmReceiveOrder({orderId}).then(() => {
				            	uni.hideLoading();
				            	this.loadData();
				            });
				        }
				    }
				});
			},
			navToReturnApply(order) {
				const item = order.orderItemList[0];
				if (!item) {
					uni.showToast({ title: '商品信息不存在', icon: 'none' });
					return;
				}
				uni.navigateTo({
					url: `/pages/order/returnApply?orderId=${order.id}&orderSn=${order.orderSn}&productId=${item.productId}&productName=${encodeURIComponent(item.productName)}&productPic=${encodeURIComponent(item.productPic)}&productBrand=${encodeURIComponent(item.productBrand || '')}&productAttr=${encodeURIComponent(item.productAttr || '')}&productCount=${item.productQuantity}&productPrice=${item.productPrice}&productRealPrice=${item.productRealPrice || item.productPrice}`
				});
			},
			showOrderDetail(orderId){
				uni.navigateTo({ url: `/pages/order/orderDetail?orderId=${orderId}` })
			},
			calcTotalQuantity(order){
				let total = 0;
				if(order.orderItemList){
					for(let item of order.orderItemList) total += item.productQuantity;
				}
				return total;
			},
		},
	}
</script>

<style lang="scss">
	page, .content { background: $color-bg-secondary; height: 100%; }
	.swiper-box { height: calc(100% - 40px); }
	.list-scroll-content { height: 100%; }

	.navbar {
		display: flex;
		height: 40px;
		padding: 0 5px;
		background: #fff;
		box-shadow: 0 1px 5px rgba(0, 0, 0, .06);
		position: relative;
		z-index: 10;

		.nav-item {
			flex: 1;
			display: flex;
			justify-content: center;
			align-items: center;
			height: 100%;
			font-size: 15px;
			color: $font-color-dark;
			position: relative;

			&.current {
				color: $base-color;
				&:after {
					content: '';
					position: absolute;
					left: 50%;
					bottom: 0;
					transform: translateX(-50%);
					width: 44px;
					height: 0;
					border-bottom: 2px solid $base-color;
				}
			}
		}
	}

	.order-item {
		display: flex;
		flex-direction: column;
		padding-left: 30upx;
		background: #fff;
		margin-top: 16upx;

		.i-top {
			display: flex;
			align-items: center;
			height: 80upx;
			padding-right: 30upx;
			font-size: $font-base;
			color: $font-color-dark;
			position: relative;

			.time { flex: 1; }
			.state { color: $base-color; }

			.del-btn {
				padding: 10upx 0 10upx 36upx;
				font-size: $font-lg;
				color: $font-color-light;
				position: relative;
				&:after {
					content: '';
					width: 0;
					height: 30upx;
					border-left: 1px solid $border-color-dark;
					position: absolute;
					left: 20upx;
					top: 50%;
					transform: translateY(-50%);
				}
			}
		}

		.goods-box-single {
			display: flex;
			padding: 20upx 0;

			.goods-img {
				display: block;
				width: 120upx;
				height: 120upx;
				flex-shrink: 0;
			}

			.right {
				flex: 1;
				display: flex;
				flex-direction: column;
				padding: 0 30upx 0 24upx;
				overflow: hidden;

				.title {
					font-size: $font-base + 2upx;
					color: $font-color-dark;
					line-height: 1.4;
				}
				.attr-box {
					font-size: $font-sm + 2upx;
					color: $font-color-light;
					padding: 10upx 12upx;
				}
				.price {
					font-size: $font-base + 2upx;
					color: $font-color-dark;
					&:before { content: '￥'; font-size: $font-sm; margin: 0 2upx 0 8upx; }
				}
			}
		}

		.price-box {
			display: flex;
			justify-content: flex-end;
			align-items: baseline;
			padding: 20upx 30upx;
			font-size: $font-sm + 2upx;
			color: $font-color-light;

			.num { margin: 0 8upx; color: $font-color-dark; }
			.price {
				font-size: $font-lg;
				color: $font-color-dark;
				&:before { content: '￥'; font-size: $font-sm; margin: 0 2upx 0 8upx; }
			}
		}

		.action-box {
			display: flex;
			justify-content: flex-end;
			align-items: center;
			height: 100upx;
			position: relative;
			padding-right: 30upx;
		}

		.action-btn {
			width: 160upx;
			height: 60upx;
			margin: 0;
			margin-left: 24upx;
			padding: 0;
			text-align: center;
			line-height: 60upx;
			font-size: $font-sm + 2upx;
			color: $font-color-dark;
			background: #fff;
			border-radius: 100px;

			&:after { border-radius: 100px; }
			&.recom { background: $color-bg-secondary; color: $base-color; }
		}

		/* 售后列表信息行 */
		.info-row {
			display: flex;
			padding: 12upx 30upx 12upx 0;
			border-top: 1px solid $color-border;

			.label {
				font-size: 24upx;
				color: $font-color-light;
				width: 120upx;
				flex-shrink: 0;
			}
			value {
				font-size: 24upx;
				color: $font-color-dark;
				flex: 1;
				&.price { color: $base-color; font-weight: 600; }
			}
		}

		/* 空状态提示 */
		.empty-tip {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			padding: 120upx 0;

			.empty-icon {
				font-size: 120upx;
				margin-bottom: 24upx;
			}
			.empty-text {
				font-size: 28upx;
				color: $font-color-light;
			}
		}
	}
</style>
