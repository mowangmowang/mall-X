<template>
	<view class="content">
		<scroll-view scroll-y class="left-aside">
			<view v-for="item in flist" :key="item.id" class="f-item b-b" :class="{active: item.id === currentId}" @click="tabtap(item)">
				{{item.name}}
			</view>
		</scroll-view>
		<scroll-view scroll-with-animation scroll-y class="right-aside">
			<view class="s-list">
				<view @click="navToList(item.id)" class="s-item" v-for="item in slist" :key="item.id">
					<image :src="item.icon||'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20190519/default.png'"></image>
					<text>{{item.name}}</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import {
		fetchProductCateList
	} from '@/api/home.js';
	export default {
		data() {
			return {
				currentId: 0,
				flist: [],
				slist: []
			}
		},
		onLoad() {
			this.loadData();
		},
		methods: {
			async loadData() {
				fetchProductCateList(0).then(response => {
					this.flist = response.data;
					if (this.flist.length > 0) {
						this.currentId = this.flist[0].id;
						fetchProductCateList(this.currentId).then(response => {
							this.slist = response.data;
						});
					}
				})
			},
			//一级分类点击
			tabtap(item) {
				this.currentId = item.id;
				fetchProductCateList(this.currentId).then(response => {
					this.slist = response.data;
				});
			},
			navToList(sid) {
				uni.navigateTo({
					url: `/pages/product/list?fid=${this.currentId}&sid=${sid}`
				})
			}
		}
	}
</script>

<style lang='scss'>
	@import '../../uni.scss';

	page,
	.content {
		height: 100%;
		background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
	}

	.content {
		display: flex;
	}

	.left-aside {
		flex-shrink: 0;
		width: 200upx;
		height: 100%;
		@include glass-effect(0.85, 15px);
		border-radius: 0 $glass-radius-lg $glass-radius-lg 0;
		margin: $glass-spacing-base;
		box-shadow: $glass-shadow-md;
	}

	.f-item {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 100%;
		height: 100upx;
		font-size: $glass-font-base;
		font-family: $glass-font-body;
		font-weight: 500;
		color: $font-color-base;
		position: relative;
		transition: all $glass-transition-base;
		cursor: pointer;

		&:hover {
			background: rgba($glass-primary, 0.08);
		}

		&:active {
			transform: translateX(2px);
		}

		&.active {
			color: $glass-primary;
			background: rgba($glass-primary, 0.12);
			font-weight: 600;

			&:before {
				content: '';
				position: absolute;
				left: 0;
				top: 50%;
				transform: translateY(-50%);
				height: 48upx;
				width: 6upx;
				background-color: $glass-primary;
				border-radius: 0 $glass-radius-sm $glass-radius-sm 0;
				box-shadow: 0 0 10upx rgba($glass-primary, 0.5);
				opacity: 1;
			}
		}
	}

	.right-aside {
		flex: 1;
		overflow: hidden;
		padding: $glass-spacing-base;
		margin: $glass-spacing-base $glass-spacing-base $glass-spacing-base 0;
	}

	.s-list {
		margin-top: 0;
		display: flex;
		flex-wrap: wrap;
		width: 100%;
		@include glass-effect(0.8, 20px);
		border-radius: $glass-radius-lg;
		padding: $glass-spacing-lg;
		box-shadow: $glass-shadow-md;

		&:after {
			content: '';
			flex: 99;
			height: 0;
		}
	}

	.s-item {
		flex-shrink: 0;
		display: flex;
		justify-content: center;
		align-items: center;
		flex-direction: column;
		width: 200upx;
		font-size: $glass-font-sm;
		font-family: $glass-font-body;
		font-weight: 500;
		color: $font-color-dark;
		padding: $glass-spacing-base;
		margin: $glass-spacing-xs;
		@include glass-card;
		transition: all $glass-transition-base;
		cursor: pointer;

		&:hover {
			transform: translateY(-4px);
			box-shadow: $glass-shadow-lg;
			background: rgba(255, 255, 255, 0.9);
		}

		&:active {
			transform: translateY(-2px);
		}

		image {
			width: 140upx;
			height: 140upx;
			border-radius: $glass-radius-base;
			margin-bottom: $glass-spacing-sm;
			box-shadow: $glass-shadow-sm;
			transition: transform $glass-transition-base;
		}

		&:hover image {
			transform: scale(1.05);
		}
	}
</style>
