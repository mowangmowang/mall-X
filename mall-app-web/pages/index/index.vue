<template>
	<view class="container">

		<view class="mp-search-box" :style="searchBarStyle">
			<text class="yticon icon-sousuo" :style="searchIconStyle"></text>
			<input class="ser-input" type="text" v-model="keyword" placeholder="输入关键字搜索" @confirm="search($event.detail.value)" />
		</view>

		<!-- 头部轮播 -->
		<view class="carousel-section">
			<view class="titleNview-placing"></view>
			<view class="titleNview-background" :style="{backgroundColor:titleNViewBackground}"></view>
			<swiper class="carousel" circular @change="swiperChange">
				<swiper-item v-for="(item, index) in advertiseList" :key="index" class="carousel-item" @click="navToAdvertisePage(item)">
					<image :src="item.pic" />
				</swiper-item>
			</swiper>
			<view class="swiper-dots">
				<text class="num">{{swiperCurrent+1}}</text>
				<text class="sign">/</text>
				<text class="num">{{swiperLength}}</text>
			</view>
		</view>

		<!-- 头部功能区：导航到独立页面 -->
		<view class="cate-section">
			<view class="cate-item" @click="navToSubjectList()">
				<image src="/static/temp/c3.png"></image>
				<text>专题</text>
			</view>
			<view class="cate-item" @click="navToTopicList()">
				<image src="/static/temp/c5.png"></image>
				<text>话题</text>
			</view>
			<view class="cate-item" @click="navToPrefrencePage()">
				<image src="/static/temp/c6.png"></image>
				<text>优选</text>
			</view>
			<view class="cate-item" @click="navToCouponCenter()">
				<image src="/static/temp/c7.png"></image>
				<text>领券</text>
			</view>
		</view>

		<!-- 品牌制造商直供 -->
		<view class="f-header m-t" @click="navToRecommendBrandPage()">
			<image src="/static/icon_home_brand.png"></image>
			<view class="tit-box">
				<text class="tit">品牌制造商直供</text>
				<text class="tit2">工厂直达消费者，剔除品牌溢价</text>
			</view>
			<text class="yticon icon-you"></text>
		</view>

		<view class="guess-section">
			<view v-for="(item, index) in brandList" :key="index" class="guess-item" @click="navToBrandDetailPage(item)">
				<view class="image-wrapper-brand">
					<image :src="item.logo" mode="aspectFit"></image>
				</view>
				<text class="title clamp">{{item.name}}</text>
				<text class="title2">商品数量：{{item.productCount}}</text>
			</view>
		</view>

		<!-- 新鲜好物 -->
		<view class="f-header m-t" @click="navToNewProudctListPage()">
			<image src="/static/icon_new_product.png"></image>
			<view class="tit-box">
				<text class="tit">新鲜好物</text>
				<text class="tit2">为你寻觅世间好物</text>
			</view>
			<text class="yticon icon-you"></text>
		</view>
		<view class="seckill-section">
			<scroll-view class="floor-list" scroll-x>
				<view class="scoll-wrapper">
					<view v-for="(item, index) in newProductList" :key="index" class="floor-item" @click="navToDetailPage(item)">
						<image :src="item.pic" mode="aspectFill"></image>
						<text class="title clamp">{{item.name}}</text>
						<text class="title2 clamp">{{item.subTitle}}</text>
						<text class="price">￥{{item.price}}</text>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 人气推荐 -->
		<view class="f-header m-t" @click="navToHotProudctListPage()">
			<image src="/static/icon_hot_product.png"></image>
			<view class="tit-box">
				<text class="tit">人气推荐</text>
				<text class="tit2">大家都赞不绝口的</text>
			</view>
			<text class="yticon icon-you"></text>
		</view>

		<view class="hot-section">
			<view v-for="(item, index) in hotProductList" :key="index" class="guess-item" @click="navToDetailPage(item)">
				<view class="image-wrapper">
					<image :src="item.pic" mode="aspectFill"></image>
				</view>
				<view class="txt">
					<text class="title clamp">{{item.name}}</text>
					<text class="title2">{{item.subTitle}}</text>
					<text class="price">￥{{item.price}}</text>
				</view>
			</view>
		</view>

		<!-- 猜你喜欢-->
		<view class="f-header m-t">
			<image src="/static/icon_recommend_product.png"></image>
			<view class="tit-box">
				<text class="tit">猜你喜欢</text>
				<text class="tit2">你喜欢的都在这里了</text>
			</view>
			<text class="yticon icon-you" v-show="false"></text>
		</view>

		<view class="guess-section">
			<view v-for="(item, index) in recommendProductList" :key="index" class="guess-item" @click="navToDetailPage(item)">
				<view class="image-wrapper">
					<image :src="item.pic" mode="aspectFill"></image>
				</view>
				<text class="title clamp">{{item.name}}</text>
				<text class="title2 clamp">{{item.subTitle}}</text>
				<text class="price">￥{{item.price}}</text>
			</view>
		</view>
		<uni-load-more :status="loadingType"></uni-load-more>
	</view>
</template>

<script>
	import {
		fetchContent,
		fetchRecommendProductList
	} from '@/api/home.js';
	import uniLoadMore from '@/components/uni-load-more/uni-load-more.vue';
	export default {
		components: {
			uniLoadMore
		},
		data() {
			return {
				titleNViewBackground: '',
				titleNViewBackgroundList: ['rgb(203, 87, 60)', 'rgb(205, 215, 218)'],
				swiperCurrent: 0,
				swiperLength: 0,
				carouselList: [],
				goodsList: [],
				advertiseList: [],
				brandList: [],
				newProductList: [],
				hotProductList: [],
				recommendProductList: [],
				recommendParams: {
					pageNum: 1,
					pageSize: 4
				},
				loadingType:'more',
				keyword: '',
				searchBarOpacity: 0
			};
		},
		computed: {
			searchBarStyle() {
				const o = this.searchBarOpacity;
				return {
					background: `rgba(255, 255, 255, ${o})`,
					boxShadow: o > 0.5 ? '0 2upx 12upx rgba(0,0,0,0.06)' : 'none'
				};
			},
			searchIconStyle() {
				const o = this.searchBarOpacity;
				const c = Math.round(255 - 191 * o);
				return { color: `rgb(${c}, ${c}, ${c})` };
			}
		},
		onPageScroll(e) {
			const scrollY = e.scrollTop || 0;
			this.searchBarOpacity = Math.min(1, Math.max(0, scrollY / 250));
		},
		onLoad() {
			this.loadData();
		},
		onPullDownRefresh(){
			this.recommendParams.pageNum=1;
			this.loadData();
		},
		onReachBottom(){
			this.recommendParams.pageNum++;
			this.loadingType = 'loading';
			fetchRecommendProductList(this.recommendParams).then(response => {
				let addProductList = response.data;
				if(response.data.length===0){
					this.recommendParams.pageNum--;
					this.loadingType = 'nomore';
				}else{
					this.recommendProductList = this.recommendProductList.concat(addProductList);
					this.loadingType = 'more';
				}
			})
		},
		onNavigationBarSearchInputConfirmed(e) {
			this.search(e.keyword);
		},
		methods: {
			loadData() {
				fetchContent().then(response => {
					this.advertiseList = response.data.advertiseList;
					this.swiperLength = this.advertiseList.length;
					this.titleNViewBackground = this.titleNViewBackgroundList[0];
					this.brandList = response.data.brandList;
					this.newProductList = response.data.newProductList;
					this.hotProductList = response.data.hotProductList;
					fetchRecommendProductList(this.recommendParams).then(response => {
						this.recommendProductList = response.data;
						uni.stopPullDownRefresh();
					})
				});
			},
			navToSubjectList() {
				uni.navigateTo({ url: '/pages/subject/list' });
			},
			navToTopicList() {
				uni.navigateTo({ url: '/pages/topic/list' });
			},
			navToPrefrencePage() {
				uni.navigateTo({ url: '/pages/prefrence/prefrence' });
			},
			navToCouponCenter() {
				uni.navigateTo({ url: '/pages/coupon/center' });
			},
			swiperChange(e) {
				const index = e.detail.current;
				this.swiperCurrent = index;
				let changeIndex = index % this.titleNViewBackgroundList.length;
				this.titleNViewBackground = this.titleNViewBackgroundList[changeIndex];
			},
			search(keyword) {
				let kw = keyword || this.keyword;
				if (!kw || !kw.trim()) return;
				uni.navigateTo({
					url: `/pages/product/list?keyword=${encodeURIComponent(kw.trim())}`
				})
			},
			navToDetailPage(item) {
				let id = item.id;
				uni.navigateTo({
					url: `/pages/product/product?id=${id}`
				})
			},
			navToAdvertisePage(item) {
				let id = item.id;
			},
			navToBrandDetailPage(item) {
				let id = item.id;
				uni.navigateTo({
					url: `/pages/brand/brandDetail?id=${id}`
				})
			},
			navToRecommendBrandPage() {
				uni.navigateTo({
					url: `/pages/brand/list`
				})
			},
			navToNewProudctListPage() {
				uni.navigateTo({
					url: `/pages/product/newProductList`
				})
			},
			navToHotProudctListPage() {
				uni.navigateTo({
					url: `/pages/product/hotProductList`
				})
			},
		},
	}
</script>

<style lang="scss">
	// 移除全局 focus-visible 黑框
	:focus-visible {
		outline: none !important;
	}
	
	.mp-search-box {
			position: fixed;
			top: 0;
			left: 0;
			right: 0;
			z-index: $z-dropdown;
			padding: calc(var(--status-bar-height) + 16upx) 80upx 16upx;
			display: flex;
			align-items: center;
			cursor: pointer;

			.yticon {
				font-size: 32upx;
				color: $color-secondary;
				margin-right: 12upx;
				flex-shrink: 0;
			}

			.ser-input {
				flex: 1;
				height: 64upx;
				line-height: 64upx;
				padding: 0 24upx;
				font-size: 28upx;
				color: $font-color-base;
				border-radius: 20px;
				background: $color-bg-secondary;
				outline: none;
				border: none;
			}
			
			.ser-input:focus,
			.ser-input:focus-visible {
				outline: none !important;
				border: none !important;
				-webkit-appearance: none;
				box-shadow: none !important;
			}
		}



		/* #ifdef MP */
		page {
		.cate-section {
			position: relative;
			z-index: 5;
			border-radius: $radius-xl $radius-xl 0 0;
			margin-top: -20upx;
		}

		.carousel-section {
			padding: 0;

			.titleNview-placing {
				padding-top: 0;
				height: 0;
			}

			.carousel {
				.carousel-item {
					padding: 0;
			}
			}

			.swiper-dots {
				left: 45upx;
				bottom: 50upx;
			}
		}
	}
	/* #endif */

	page {
		background: $color-bg;
		min-height: 100vh;
	}

	.m-t {
		margin-top: 16upx;
	}

	/* 头部 轮播图 */
	.carousel-section {
		position: relative;
		padding-top: 10px;

		.titleNview-placing {
			height: calc(var(--status-bar-height) + 110upx);
			box-sizing: content-box;
		}

		.titleNview-background {
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 426upx;
			transition: .4s;
		}
	}

	.carousel {
		width: 100%;
		height: 350upx;

		.carousel-item {
			width: 100%;
			height: 100%;
			padding: 0 28upx;
			overflow: hidden;
		}

		image {
			width: 100%;
			height: 100%;
			border-radius: 10upx;
		}
	}

	.swiper-dots {
		display: flex;
		position: absolute;
		left: 60upx;
		bottom: 30upx;
		width: 72upx;
		height: 36upx;
		background: rgba(0, 0, 0, 0.5);
		border-radius: 18px;

		.num {
			width: 36upx;
			height: 36upx;
			border-radius: 50px;
			font-size: 22upx;
			color: #fff;
			text-align: center;
			line-height: 36upx;
			font-weight: 600;
		}

		.sign {
			position: absolute;
			top: 0;
			left: 50%;
			line-height: 36upx;
			font-size: 20upx;
			color: #fff;
			transform: translateX(-50%);
		}
	}

	/* 分类 */
	.cate-section {
		display: flex;
		justify-content: space-around;
		align-items: center;
		flex-wrap: wrap;
		padding: $spacing-xl $spacing-base;
		background: $color-bg;
		border: 1px solid $color-border;
		border-radius: $radius-xl;
		margin: $spacing-base $page-row-spacing;

		.cate-item {
			display: flex;
			flex-direction: column;
			align-items: center;
			font-size: $font-sm;
			color: $font-color-dark;
			font-weight: 600;
			padding: $spacing-sm;

			&:active {
				opacity: 0.7;
			}
		}

		image {
			width: 100upx;
			height: 100upx;
			margin-bottom: $spacing-sm;
			border-radius: 50%;
			opacity: .9;

			&:active {
				opacity: 0.7;
			}
		}
	}

	.seckill-section {
		.floor-list {
			white-space: nowrap;
		}

		.scoll-wrapper {
			display: flex;
			align-items: flex-start;
			padding: $spacing-xs 0;
		}

		.floor-item {
			width: 300upx;
			margin-right: $spacing-lg;
			padding: $spacing-base;
			background: $color-bg;
			border: 1px solid $color-border;
			border-radius: $radius-lg;
			font-size: $font-sm;
			color: $font-color-dark;
			line-height: 1.8;

			&:active {
				opacity: 0.8;
			}

			image {
				width: 300upx;
				height: 300upx;
				border-radius: $radius-base;
				margin-bottom: $spacing-base;
			}

			.price {
				color: $color-primary;
				font-weight: 700;
				font-size: $font-lg;
				margin-top: $spacing-sm;
			}
		}
	}

	.f-header {
		display: flex;
		align-items: center;
		height: 140upx;
		padding: 6upx 30upx 8upx;
		background: $color-bg;
		border: 1px solid $color-border;
		border-radius: $radius-lg;
		margin: $spacing-base $page-row-spacing;

		&:active {
			opacity: 0.7;
		}

		image {
			flex-shrink: 0;
			width: 80upx;
			height: 80upx;
			margin-right: 20upx;
			border-radius: $radius-base;
		}

		.tit-box {
			flex: 1;
			display: flex;
			flex-direction: column;
		}

		.tit {
			font-size: $font-lg;
			color: $font-color-dark;
			line-height: 1.3;
			font-weight: 600;
		}

		.tit2 {
			font-size: $font-sm;
			color: $font-color-light;
		}

		.icon-you {
			font-size: $font-lg;
			color: $color-secondary;
			font-weight: 600;
		}
	}

	/* 猜你喜欢 & 品牌 */
	.guess-section {
		display: flex;
		flex-wrap: wrap;
		padding: $spacing-base $page-row-spacing;

		.guess-item {
			display: flex;
			flex-direction: column;
			width: 48%;
			padding: $spacing-lg;
			margin-bottom: $spacing-lg;
			background: $color-bg;
			border: 1px solid $color-border;
			border-radius: $radius-lg;

			&:nth-child(2n+1) {
				margin-right: 4%;
			}

			&:active {
				opacity: 0.8;
			}
		}

		.image-wrapper {
			width: 100%;
			height: 330upx;
			border-radius: $radius-base;
			overflow: hidden;
			margin-bottom: $spacing-base;

			image {
				width: 100%;
				height: 100%;
				opacity: 1;
			}
		}

		.image-wrapper-brand {
			width: 100%;
			height: 150upx;
			border-radius: $radius-base;
			overflow: hidden;
			margin-bottom: $spacing-base;

			image {
				width: 100%;
				height: 100%;
				opacity: 1;
			}
		}

		.title {
			font-size: $font-base;
			color: $font-color-dark;
			line-height: 1.4;
			font-weight: 600;
			margin-bottom: $spacing-xs;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.title2 {
			font-size: $font-sm;
			color: $font-color-light;
			line-height: 1.4;
			margin-bottom: $spacing-base;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.price {
			font-size: $font-lg;
			color: $color-primary;
			line-height: 1.4;
			font-weight: 700;
			margin-top: auto;
		}
	}

	.hot-section {
		display: flex;
		flex-wrap: wrap;
		padding: $spacing-base $page-row-spacing;

		.guess-item {
			display: flex;
			flex-direction: row;
			width: 100%;
			padding: $spacing-lg;
			margin-bottom: $spacing-lg;
			background: $color-bg;
			border: 1px solid $color-border;
			border-radius: $radius-lg;

			&:active {
				opacity: 0.8;
			}
		}

		.image-wrapper {
			width: 30%;
			height: 250upx;
			border-radius: $radius-base;
			overflow: hidden;
			flex-shrink: 0;

			image {
				width: 100%;
				height: 100%;
				opacity: 1;
			}
		}

		.title {
			font-size: $font-base;
			color: $font-color-dark;
			line-height: 1.4;
			font-weight: 600;
			margin-bottom: $spacing-xs;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.title2 {
			font-size: $font-sm;
			color: $font-color-light;
			line-height: 1.4;
			margin-bottom: $spacing-base;
			display: -webkit-box;
			-webkit-line-clamp: 3;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.price {
			font-size: $font-lg;
			color: $color-primary;
			line-height: 1.4;
			font-weight: 700;
			margin-top: auto;
		}

		.txt {
			width: 70%;
			display: flex;
			flex-direction: column;
			padding-left: $spacing-lg;
			justify-content: space-between;
		}
	}
</style>
