<template>
	<view>
		<view v-for="(area, idx) in prefrenceAreaList" :key="idx">
			<view class="area-header">
				<view class="area-pic" v-if="area.area.pic">
					<image :src="area.area.pic" mode="aspectFill"></image>
				</view>
				<view class="area-info">
					<text class="name">{{area.area.name}}</text>
					<text class="subtitle">{{area.area.subTitle}}</text>
				</view>
			</view>
			<view class="goods-list" v-if="area.productList.length > 0">
				<view v-for="(item, index) in area.productList" :key="index" class="goods-item" @click="navToDetailPage(item)">
					<view class="image-wrapper">
						<image :src="item.pic" mode="aspectFill"></image>
					</view>
					<text class="title clamp">{{item.name}}</text>
					<text class="title2">{{item.subTitle}}</text>
					<view class="price-box">
						<text class="price">{{item.price}}</text>
						<text>已售 {{item.sale}}</text>
					</view>
				</view>
			</view>
		</view>
		<uni-load-more :status="loadingType"></uni-load-more>
	</view>
</template>

<script>
	import { fetchPrefrenceAreaList } from '@/api/home.js';
	import uniLoadMore from '@/components/uni-load-more/uni-load-more.vue';
	export default {
		components: { uniLoadMore },
		data() {
			return {
				prefrenceAreaList: [],
				loadingType: 'loading'
			};
		},
		onLoad() {
			fetchPrefrenceAreaList().then(response => {
				this.prefrenceAreaList = response.data || [];
				this.loadingType = this.prefrenceAreaList.length > 0 ? 'more' : 'nomore';
			});
		},
		methods: {
			navToDetailPage(item) {
				let id = item.id;
				uni.navigateTo({ url: `/pages/product/product?id=${id}` })
			}
		}
	}
</script>

<style lang="scss">
	page { background: $color-bg-secondary; }

	.area-header {
		display: flex; align-items: center; padding: 30upx; background: #fff; margin-top: 16upx;
		.area-pic {
			width: 120upx; height: 120upx; border-radius: $radius-base; overflow: hidden; margin-right: 20upx;
			image { width: 100%; height: 100%; }
		}
		.area-info {
			flex: 1;
			.name { font-size: $font-lg+2upx; color: $font-color-dark; font-weight: 600; }
			.subtitle { display: block; font-size: $font-sm; color: $font-color-light; margin-top: 8upx; }
		}
	}

	.goods-list {
		display: flex; flex-wrap: wrap; padding: 0 30upx; background: #fff;
		.goods-item {
			display: flex; flex-direction: column; width: 48%; padding-bottom: 40upx;
			&:nth-child(2n+1) { margin-right: 4%; }
		}
		.image-wrapper {
			width: 100%; height: 330upx; border-radius: 3px; overflow: hidden;
			image { width: 100%; height: 100%; opacity: 1; }
		}
		.title { font-size: $font-lg; color: $font-color-dark; line-height: 80upx; }
		.title2 { font-size: $font-sm; color: $font-color-light; line-height: 40upx; height: 80upx; overflow: hidden; text-overflow: ellipsis; display: block; }
		.price-box { display: flex; align-items: center; justify-content: space-between; padding-right: 10upx; font-size: 24upx; color: $font-color-light; }
		.price { font-size: $font-lg; color: $uni-color-primary; line-height: 1.4;
			&:before { content: '￥'; font-size: 26upx; }
		}
	}
</style>
