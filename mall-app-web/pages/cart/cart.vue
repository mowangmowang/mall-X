<template>
	<view class="container">
		<!-- 空白页 -->
		<view v-if="!hasLogin || empty===true" class="empty">
			<image src="/static/emptyCart.jpg" mode="aspectFit"></image>
			<view v-if="hasLogin" class="empty-tips">
				空空如也
				<navigator class="navigator" v-if="hasLogin" url="../index/index" open-type="switchTab">随便逛逛></navigator>
			</view>
			<view v-else class="empty-tips">
				空空如也
				<view class="navigator" @click="navToLogin">去登陆></view>
			</view>
		</view>
		<view v-else>
			<!-- 列表 -->
			<view class="cart-list">
				<block v-for="(item, index) in cartList" :key="item.id">
					<view class="cart-item" :class="{'b-b': index!==cartList.length-1}">
						<view class="image-wrapper">
							<image :src="item.productPic" :class="[item.loaded]" mode="aspectFill" lazy-load @load="onImageLoad('cartList', index)"
							 @error="onImageError('cartList', index)"></image>
							<view class="yticon icon-xuanzhong2 checkbox" :class="{checked: item.checked}" @click="check('item', index)"></view>
						</view>
						<view class="item-right">
							<text class="clamp title">{{item.productName}}</text>
							<text class="attr">{{item.spDataStr}}</text>
							<text class="price">¥{{item.price}}</text>
							<uni-number-box class="step" :min="1" :max="100" :value="item.quantity" :index="index" @eventChange="numberChange"></uni-number-box>
						</view>
						<text class="del-btn yticon icon-fork" @click="handleDeleteCartItem(index)"></text>
					</view>
				</block>
			</view>
			<!-- 底部菜单栏 -->
			<view class="action-section">
				<view class="checkbox">
					<image :src="allChecked?'/static/selected.png':'/static/select.png'" mode="aspectFit" @click="check('all')"></image>
					<view class="clear-btn" :class="{show: allChecked}" @click="clearCart">
						清空
					</view>
				</view>
				<view class="total-box">
					<text class="price">¥{{total}}元</text>
				</view>
				<button type="primary" class="no-border confirm-btn" @click="createOrder">去结算</button>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		mapState
	} from 'vuex';
	import uniNumberBox from '@/components/uni-number-box.vue';
	import {
		fetchCartList,
		deletCartItem,
		updateQuantity,
		clearCartList
	} from '@/api/cart.js';
	export default {
		components: {
			uniNumberBox
		},
		data() {
			return {
				total: 0, //总价格
				allChecked: false, //全选状态  true|false
				empty: false, //空白页现实  true|false
				cartList: [],
			};
		},
		onLoad() {
			// this.loadData();
		},
		onShow(){
			//页面显示时重新加载购物车
			this.loadData();
		},
		watch: {
			//显示空白页
			cartList(e) {
				let empty = e.length === 0 ? true : false;
				if (this.empty !== empty) {
					this.empty = empty;
				}
			}
		},
		computed: {
			...mapState(['hasLogin'])
		},
		methods: {
			//请求数据
			async loadData() {
				if(!this.hasLogin){
					return;
				}
				fetchCartList().then(response => {
					let list = response.data;
					let cartList = list.map(item => {
						item.checked = true;
						item.loaded = "loaded";
						let spDataArr = JSON.parse(item.productAttr);
						let spDataStr = '';
						for (let attr of spDataArr) {
							spDataStr += attr.key;
							spDataStr += ":";
							spDataStr += attr.value;
							spDataStr += ";";
						}
						item.spDataStr = spDataStr;
						return item;
					});
					this.cartList = cartList;
					this.calcTotal(); //计算总价
				});
			},
			//监听image加载完成
			onImageLoad(key, index) {
				this.$set(this[key][index], 'loaded', 'loaded');
			},
			//监听image加载失败
			onImageError(key, index) {
				this[key][index].productPic = '/static/errorImage.jpg';
			},
			navToLogin() {
				uni.navigateTo({
					url: '/pages/public/login'
				})
			},
			//选中状态处理
			check(type, index) {
				if (type === 'item') {
					this.cartList[index].checked = !this.cartList[index].checked;
				} else {
					const checked = !this.allChecked
					const list = this.cartList;
					list.forEach(item => {
						item.checked = checked;
					})
					this.allChecked = checked;
				}
				this.calcTotal(type);
			},
			//数量
			numberChange(data) {
				let cartItem = this.cartList[data.index];
				updateQuantity({id:cartItem.id,quantity:data.number}).then(response=>{
					cartItem.quantity = data.number;
					this.calcTotal();
				});
			},
			//删除
			handleDeleteCartItem(index) {
				let list = this.cartList;
				let row = list[index];
				let id = row.id;
				deletCartItem({ids:id}).then(response=>{
					this.cartList.splice(index, 1);
					this.calcTotal();
					uni.hideLoading();
				});
			},
			//清空
			clearCart() {
				clearCartList().then(response=>{
					uni.showModal({
						content: '清空购物车？',
						success: (e) => {
							if (e.confirm) {
								this.cartList = [];
							}
						}
					})
				});
			},
			//计算总价
			calcTotal() {
				let list = this.cartList;
				if (list.length === 0) {
					this.empty = true;
					return;
				}
				let total = 0;
				let checked = true;
				list.forEach(item => {
					if (item.checked === true) {
						total += item.price * item.quantity;
					} else if (checked === true) {
						checked = false;
					}
				})
				this.allChecked = checked;
				this.total = Number(total.toFixed(2));
			},
			//创建订单
			createOrder() {
				let list = this.cartList;
				let cartIds = [];
				list.forEach(item => {
					if (item.checked) {
						cartIds.push(item.id);
					}
				})
				if(cartIds.length==0){
					uni.showToast({
						title:'您还未选择要下单的商品！',
						duration:1000
					})
					return;
				}
				uni.navigateTo({
					url: `/pages/order/createOrder?cartIds=${JSON.stringify(cartIds)}`
				})
			}
		}
	}
</script>

<style lang='scss'>
	@import '../../uni.scss';

	.container {
		padding-bottom: 134upx;

		/* 空白页 */
		.empty {
			position: fixed;
			left: 0;
			top: 0;
			width: 100%;
			height: 100vh;
			padding-bottom: 100upx;
			display: flex;
			justify-content: center;
			flex-direction: column;
			align-items: center;
			background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);

			image {
				width: 240upx;
				height: 160upx;
				margin-bottom: $glass-spacing-lg;
				border-radius: $glass-radius-base;
				box-shadow: $glass-shadow-md;
			}

			.empty-tips {
				display: flex;
				font-size: $glass-font-base;
				color: $font-color-light;
				font-family: $glass-font-body;
				padding: $glass-spacing-base;
				@include glass-effect(0.85, 15px);
				border-radius: $glass-radius-lg;

				.navigator {
					color: $glass-primary;
					margin-left: $glass-spacing-base;
					font-weight: 600;
					transition: color $glass-transition-base;

					&:active {
						color: $glass-primary-dark;
					}
				}
			}
		}
	}

	/* 购物车列表项 */
	.cart-item {
		display: flex;
		position: relative;
		padding: $glass-spacing-lg $page-row-spacing;
		margin: $glass-spacing-base $page-row-spacing;
		@include glass-card;
		border-radius: $glass-radius-lg;
		transition: all $glass-transition-base;
		font-family: $glass-font-body;

		&:active {
			transform: translateY(4px);
			box-shadow: $glass-shadow-sm;
		}

		.image-wrapper {
			width: 230upx;
			height: 230upx;
			flex-shrink: 0;
			position: relative;

			image {
				border-radius: $glass-radius-base;
				box-shadow: $glass-shadow-sm;
				transition: transform $glass-transition-base;
			}

			&:active image {
				transform: scale(1.05);
			}
		}

		.checkbox {
			position: absolute;
			left: -16upx;
			top: -16upx;
			z-index: 8;
			font-size: 44upx;
			line-height: 1;
			padding: 4upx;
			color: $font-color-light;
			background: rgba(255, 255, 255, 0.9);
			border-radius: $glass-radius-full;
			box-shadow: $glass-shadow-sm;
			transition: all $glass-transition-base;

			&.checked {
				color: $glass-primary;
				box-shadow: 0 0 15upx rgba($glass-primary, 0.5);
			}
		}

		.item-right {
			display: flex;
			flex-direction: column;
			flex: 1;
			overflow: hidden;
			position: relative;
			padding-left: $glass-spacing-lg;

			.title,
			.price {
				font-size: $glass-font-base;
				color: $font-color-dark;
				height: 40upx;
				line-height: 40upx;
				font-weight: 600;
			}

			.attr {
				font-size: $glass-font-sm;
				color: $font-color-light;
				height: 50upx;
				line-height: 50upx;
				margin: $glass-spacing-xs 0;
				display: -webkit-box;
				-webkit-line-clamp: 2;
				-webkit-box-orient: vertical;
				overflow: hidden;
			}

			.price {
				height: 50upx;
				line-height: 50upx;
				color: $glass-primary;
				font-weight: 700;
				font-size: $glass-font-lg;
				margin-top: auto;
			}
		}

		.del-btn {
			padding: 4upx 10upx;
			font-size: 34upx;
			height: 50upx;
			color: $font-color-light;
			transition: all $glass-transition-base;

			&:active {
				color: $glass-error;
				transform: scale(1.2);
			}
		}
	}

	/* 底部栏 */
	.action-section {
		/* #ifdef H5 */
		margin-bottom: 100upx;
		/* #endif */
		position: fixed;
		left: 30upx;
		bottom: 30upx;
		z-index: 95;
		display: flex;
		align-items: center;
		width: 690upx;
		height: 100upx;
		padding: 0 30upx;
		@include glass-effect(0.9, 20px);
		border-radius: $glass-radius-lg;

		.checkbox {
			height: 52upx;
			position: relative;

			image {
				width: 52upx;
				height: 100%;
				position: relative;
				z-index: 5;
			}
		}

		.clear-btn {
			position: absolute;
			left: 26upx;
			top: 0;
			z-index: 4;
			width: 0;
			height: 52upx;
			line-height: 52upx;
			padding-left: 38upx;
			font-size: $font-base;
			color: #fff;
			background: $font-color-disabled;
			border-radius: 0 50px 50px 0;
			opacity: 0;
			transition: .2s;

			&.show {
				opacity: 1;
				width: 120upx;
			}
		}

		.total-box {
			flex: 1;
			display: flex;
			flex-direction: column;
			text-align: right;
			padding-right: 40upx;

			.price {
				font-size: $font-lg;
				color: $font-color-dark;
			}

			.coupon {
				font-size: $font-sm;
				color: $font-color-light;

				text {
					color: $font-color-dark;
				}
			}
		}

		.confirm-btn {
			padding: 0 38upx;
			margin: 0;
			border-radius: 100px;
			height: 76upx;
			line-height: 76upx;
			font-size: $glass-font-base;
			background: $glass-primary;
			box-shadow: $glass-shadow-md;
			transition: all $glass-transition-base;
			font-weight: 600;

			&:active {
				background: $glass-primary-dark;
				transform: translateY(2px);
				box-shadow: $glass-shadow-sm;
			}
		}
	}

	/* 复选框选中状态 */
	.action-section .checkbox.checked,
	.cart-item .checkbox.checked {
		color: $glass-primary;
	}
</style>
