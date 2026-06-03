<template>
	<view>
		<!-- 顶部艺术色块 -->
		<view class="top-art">
			<text class="art-icon">✦</text>
			<text class="art-title">{{subject.title || '专题详情'}}</text>
		</view>
		<!-- 专题信息 -->
		<view class="info">
			<view class="title">
				<text class="name">{{subject.title}}</text>
				<text class="desc">{{subject.description}}</text>
			</view>
			<view class="stats">
				<text>{{subject.readCount || 0}}人阅读</text>
				<text>{{subject.collectCount || 0}}人收藏</text>
			</view>
		</view>
		<!-- 专题内容 -->
		<view class="content-section" v-if="subject.content">
			<view class="section-tit">专题详情</view>
			<rich-text class="rich-text" :nodes="subject.content"></rich-text>
		</view>
		<!-- 相关商品 -->
		<view class="section-tit">相关商品</view>
		<view class="goods-list">
			<view v-for="(item, index) in productList" :key="index" class="goods-item" @click="navToDetailPage(item)">
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
		<uni-load-more :status="loadingType"></uni-load-more>
	</view>
</template>

<script>
	import { fetchSubjectDetail } from '@/api/home.js';
	import uniLoadMore from '@/components/uni-load-more/uni-load-more.vue';
	export default {
		components: { uniLoadMore },
		data() {
			return {
				subject: {},
				productList: [],
				loadingType: 'more'
			};
		},
		onLoad(options) {
			let id = options.id;
			fetchSubjectDetail(id).then(response => {
				this.subject = response.data.subject;
				this.productList = response.data.productList || [];
				this.loadingType = this.productList.length > 0 ? 'more' : 'nomore';
			});
		},
		methods: {
			navToDetailPage(item) {
				let id = item.id;
				uni.navigateTo({
					url: `/pages/product/product?id=${id}`
				})
			}
		}
	}
</script>

<style lang="scss">
	page { background: $color-bg-secondary; }

	.top-art {
		height: 280upx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #a18cd1, #fbc2eb, #a6c1ee, #84fab0);
		background-size: 400% 400%;
		animation: artFlow 5s ease infinite;

		.art-icon {
			font-size: 64upx;
			color: rgba(255,255,255,0.85);
			margin-bottom: 16upx;
		}

		.art-title {
			font-size: 36upx;
			color: #fff;
			font-weight: 600;
			text-shadow: 0 2upx 8upx rgba(0,0,0,0.1);
		}
	}

	@keyframes artFlow {
		0% { background-position: 0% 50%; }
		50% { background-position: 100% 50%; }
		100% { background-position: 0% 50%; }
	}

	.info {
		padding: 30upx 30upx 20upx;
		background: #fff;
		margin-top: 16upx;
		.title {
			.name { font-size: $font-lg+6upx; color: $font-color-dark; font-weight: 600; }
			.desc { display: block; font-size: $font-sm; color: $font-color-light; margin-top: 12upx; }
		}
		.stats {
			display: flex; margin-top: 20upx;
			text { font-size: $font-sm; color: $color-secondary; margin-right: 30upx; }
		}
	}

	.content-section {
		background: #fff; margin-top: 16upx; padding: 0 30upx 30upx;
		.rich-text { font-size: $font-sm; color: $font-color-dark; line-height: 1.8; }
	}

	.section-tit {
		font-size: $font-base+2upx; color: $font-color-dark; background: #fff;
		margin-top: 16upx; text-align: center; padding: 20upx;
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
