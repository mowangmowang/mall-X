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
	page,
	.content {
		height: 100%;
		background: $color-bg;
	}

	.content {
		display: flex;
	}

	.left-aside {
		flex-shrink: 0;
		width: 200upx;
		height: 100%;
		background: $color-bg;
		border-right: 1px solid $color-border;
	}

	.f-item {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 100%;
		height: 100upx;
		font-size: $font-base;
		font-weight: 500;
		color: $font-color-base;
		position: relative;
		cursor: pointer;

		text {
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
			max-width: 90%;
		}

		&:hover {
			background: $color-bg-secondary;
		}

		&.active {
			color: $color-primary;
			background: $color-bg-secondary;
			font-weight: 600;

			&:before {
				content: '';
				position: absolute;
				left: 0;
				top: 50%;
				transform: translateY(-50%);
				height: 48upx;
				width: 6upx;
				background-color: $color-primary;
				border-radius: 0 $radius-sm $radius-sm 0;
			}
		}
	}

	.right-aside {
		flex: 1;
		overflow: hidden;
		padding: $spacing-base;
		margin: $spacing-base $spacing-base $spacing-base 0;
	}

	.s-list {
		margin-top: 0;
		display: flex;
		flex-wrap: wrap;
		width: 100%;
		background: $color-bg;
		border: 1px solid $color-border;
		border-radius: $radius-lg;
		padding: $spacing-lg;

		&:after {
			content: '';
			flex: 99;
			height: 0;
		}
	}

	.s-item {
		display: flex;
		justify-content: center;
		align-items: center;
		flex-direction: column;
		width: calc(50% - 16upx);
		font-size: $font-sm;
		font-weight: 500;
		color: $font-color-dark;
		padding: $spacing-base;
		margin: 8upx 8upx 0 0;
		background: $color-bg-secondary;
		border: 1px solid $color-border;
		border-radius: $radius-lg;
		cursor: pointer;
		box-sizing: border-box;

		&:hover {
			opacity: 0.8;
		}

		image {
			width: 120upx;
			height: 120upx;
			border-radius: $radius-base;
			margin-bottom: $spacing-sm;
		}

		text {
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
			max-width: 100%;
			text-align: center;
		}
	}
</style>
