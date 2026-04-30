<template>
	<view class="container">
		<!-- 小程序头部兼容 -->
		<!-- #ifdef MP -->
		<view class="mp-search-box">
			<input class="ser-input" type="text" value="输入关键字搜索" disabled />
		</view>
		<!-- #endif -->

		<!-- 头部轮播 -->
		<view class="carousel-section">
			<!-- 标题栏和状态栏占位符 -->
			<view class="titleNview-placing"></view>
			<!-- 背景色区域 -->
			<view class="titleNview-background" :style="{backgroundColor:titleNViewBackground}"></view>
			<swiper class="carousel" circular @change="swiperChange">
				<swiper-item v-for="(item, index) in advertiseList" :key="index" class="carousel-item" @click="navToAdvertisePage(item)">
					<image :src="item.pic" />
				</swiper-item>
			</swiper>
			<!-- 自定义swiper指示器 -->
			<view class="swiper-dots">
				<text class="num">{{swiperCurrent+1}}</text>
				<text class="sign">/</text>
				<text class="num">{{swiperLength}}</text>
			</view>
		</view>
		<!-- 头部功能区 -->
		<view class="cate-section">
			<view class="cate-item">
				<image src="/static/temp/c3.png"></image>
				<text>专题</text>
			</view>
			<view class="cate-item">
				<image src="/static/temp/c5.png"></image>
				<text>话题</text>
			</view>
			<view class="cate-item">
				<image src="/static/temp/c6.png"></image>
				<text>优选</text>
			</view>
			<view class="cate-item">
				<image src="/static/temp/c7.png"></image>
				<text>特惠</text>
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

		<!-- 秒杀专区 -->
		<view class="f-header m-t" v-if="homeFlashPromotion">
			<image src="/static/icon_flash_promotion.png"></image>
			<view class="tit-box">
				<text class="tit">秒杀专区</text>
				<text class="tit2">下一场 {{homeFlashPromotion.nextStartTime | formatTime}} 开始</text>
			</view>
			<view class="tit-box">
				<text class="tit2" style="text-align: right;">本场结束剩余：</text>
				<view style="text-align: right;">
					<text class="hour timer">{{cutDownTime.endHour}}</text>
					<text>:</text>
					<text class="minute timer">{{cutDownTime.endMinute}}</text>
					<text>:</text>
					<text class="second timer">{{cutDownTime.endSecond}}</text>
				</view>
			</view>
			<text class="yticon icon-you" v-show="false"></text>
		</view>

		<view class="guess-section">
			<view v-for="(item, index) in homeFlashPromotion.productList" :key="index" class="guess-item" @click="navToDetailPage(item)">
				<view class="image-wrapper">
					<image :src="item.pic" mode="aspectFill"></image>
				</view>
				<text class="title clamp">{{item.name}}</text>
				<text class="title2 clamp">{{item.subTitle}}</text>
				<text class="price">￥{{item.price}}</text>
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

		<!-- 人气推荐楼层 -->
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
	import {
		formatDate
	} from '@/utils/date';
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
				homeFlashPromotion: null,
				newProductList: [],
				hotProductList: [],
				recommendProductList: [],
				recommendParams: {
					pageNum: 1,
					pageSize: 4
				},
				loadingType:'more'
			};
		},
		onLoad() {
			this.loadData();
		},
		//下拉刷新
		onPullDownRefresh(){
			this.recommendParams.pageNum=1;
			this.loadData();
		},
		//加载更多
		onReachBottom(){
			this.recommendParams.pageNum++;
			this.loadingType = 'loading';
			fetchRecommendProductList(this.recommendParams).then(response => {
				let addProductList = response.data;
				if(response.data.length===0){
					//没有更多了
					this.recommendParams.pageNum--;
					this.loadingType = 'nomore';
				}else{
					this.recommendProductList = this.recommendProductList.concat(addProductList);
					this.loadingType = 'more';
				}
			})
		},
		computed: {
			cutDownTime() {
					if (!this.homeFlashPromotion) return { endHour: "00", endMinute: "00", endSecond: "00" };
				let endTime = new Date(this.homeFlashPromotion.endTime);
				let endDateTime = new Date();
				let startDateTime = new Date();
				endDateTime.setHours(endTime.getHours());
				endDateTime.setMinutes(endTime.getMinutes());
				endDateTime.setSeconds(endTime.getSeconds());
				let offsetTime = (endDateTime.getTime() - startDateTime.getTime());
				let endHour = Math.floor(offsetTime / (60 * 60 * 1000));
				let offsetMinute = offsetTime % (60 * 60 * 1000);
				let endMinute = Math.floor(offsetMinute / (60 * 1000));
				let offsetSecond = offsetTime % (60 * 1000);
				let endSecond = Math.floor(offsetSecond / 1000);
				return {
					endHour: endHour,
					endMinute: endMinute,
					endSecond: endSecond
				}
			}
		},
		filters: {
			formatTime(time) {
				if (time == null || time === '') {
					return 'N/A';
				}
				let date = new Date(time);
				return formatDate(date, 'hh:mm:ss')
			},
		},
		methods: {
			/**
			 * 加载数据
			 */
			async loadData() {
				fetchContent().then(response => {
					console.log("onLoad", response.data);
					this.advertiseList = response.data.advertiseList;
					this.swiperLength = this.advertiseList.length;
					this.titleNViewBackground = this.titleNViewBackgroundList[0];
					this.brandList = response.data.brandList;
					this.homeFlashPromotion = response.data.homeFlashPromotion || null;
					this.newProductList = response.data.newProductList;
					this.hotProductList = response.data.hotProductList;
					fetchRecommendProductList(this.recommendParams).then(response => {
						this.recommendProductList = response.data;
						uni.stopPullDownRefresh();
					})
				});
			},
			//轮播图切换修改背景色
			swiperChange(e) {
				const index = e.detail.current;
				this.swiperCurrent = index;
				let changeIndex = index % this.titleNViewBackgroundList.length;
				this.titleNViewBackground = this.titleNViewBackgroundList[changeIndex];
			},
			//商品详情页
			navToDetailPage(item) {
				let id = item.id;
				uni.navigateTo({
					url: `/pages/product/product?id=${id}`
				})
			},
			//广告详情页
			navToAdvertisePage(item) {
				let id = item.id;
				console.log("navToAdvertisePage",item)
			},
			//品牌详情页
			navToBrandDetailPage(item) {
				let id = item.id;
				uni.navigateTo({
					url: `/pages/brand/brandDetail?id=${id}`
				})
			},
			//推荐品牌列表页
			navToRecommendBrandPage() {
				uni.navigateTo({
					url: `/pages/brand/list`
				})
			},
			//新鲜好物列表页
			navToNewProudctListPage() {
				uni.navigateTo({
					url: `/pages/product/newProductList`
				})
			},
			//人气推荐列表页
			navToHotProudctListPage() {
				uni.navigateTo({
					url: `/pages/product/hotProductList`
				})
			},
		},
		// #ifndef MP
		// 标题栏input搜索框点击
		onNavigationBarSearchInputClicked: async function(e) {
			this.$api.msg('点击了搜索框');
		},
		//点击导航栏 buttons 时触发
		onNavigationBarButtonTap(e) {
			const index = e.index;
			if (index === 0) {
				this.$api.msg('点击了扫描');
			} else if (index === 1) {
				// #ifdef APP-PLUS
				const pages = getCurrentPages();
				const page = pages[pages.length - 1];
				const currentWebview = page.$getAppWebview();
				currentWebview.hideTitleNViewButtonRedDot({
					index
				});
				// #endif
				uni.navigateTo({
					url: '/pages/notice/notice'
				})
			}
		}
		// #endif
	}
</script>

<style lang="scss">
	@import '../../uni.scss';

	/* #ifdef MP */
	.mp-search-box {
		position: absolute;
		left: 0;
		top: 30upx;
		z-index: 9999;
		width: 100%;
		padding: 0 80upx;

		.ser-input {
			flex: 1;
			height: 56upx;
			line-height: 56upx;
			text-align: center;
			font-size: 28upx;
			color: $font-color-base;
			border-radius: 20px;
			background: rgba(255, 255, 255, .6);
		}
	}

	page {
		.cate-section {
			position: relative;
			z-index: 5;
			border-radius: $glass-radius-xl $glass-radius-xl 0 0;
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
		background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
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
			height: var(--status-bar-height);
			padding-top: 44px;
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
		@include glass-effect(0.8, 10px);
		border-radius: $glass-radius-full;
		border: 1px solid rgba(255, 255, 255, 0.5);
		box-shadow: $glass-shadow-md;

		.num {
			width: 36upx;
			height: 36upx;
			border-radius: 50px;
			font-size: $glass-font-sm;
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
			font-size: $glass-font-xs;
			color: #fff;
			transform: translateX(-50%);
			font-weight: 400;
		}
	}

	/* 分类 */
	.cate-section {
		display: flex;
		justify-content: space-around;
		align-items: center;
		flex-wrap: wrap;
		padding: $glass-spacing-xl $glass-spacing-base;
		@include glass-card;
		margin: $glass-spacing-base $page-row-spacing;
		border-radius: $glass-radius-xl;
		font-family: $glass-font-body;

		.cate-item {
			display: flex;
			flex-direction: column;
			align-items: center;
			font-size: $glass-font-sm;
			color: $font-color-dark;
			font-weight: 600;
			padding: $glass-spacing-sm;
			transition: all $glass-transition-base;

			&:active {
				transform: scale(0.95);
			}
		}

		/* 原图标颜色太深,不想改图了,所以加了透明度 */
		image {
			width: 100upx;
			height: 100upx;
			margin-bottom: $glass-spacing-sm;
			border-radius: 50%;
			opacity: .9;
			box-shadow: 0 8upx 20upx rgba($glass-primary, 0.3);
			transition: all $glass-transition-base;

			&:active {
				transform: scale(0.95);
				box-shadow: 0 4upx 10upx rgba($glass-primary, 0.4);
			}
		}
	}

	.ad-1 {
		width: 100%;
		height: 210upx;
		padding: $glass-spacing-base 0;
		margin: $glass-spacing-base $page-row-spacing;
		@include glass-card;
		border-radius: $glass-radius-lg;
		overflow: hidden;

		image {
			width: 100%;
			height: 100%;
			border-radius: $glass-radius-base;
		}
	}

	/* 秒杀专区 */
	.seckill-section {
		padding: $glass-spacing-base $page-row-spacing;
		margin: $glass-spacing-base $page-row-spacing;
		@include glass-card;
		border-radius: $glass-radius-xl;

		.s-header {
			display: flex;
			align-items: center;
			height: 92upx;
			line-height: 1;
			margin-bottom: $glass-spacing-base;
			font-family: $glass-font-heading;

			.s-img {
				width: 140upx;
				height: 30upx;
			}

			.tip {
				font-size: $glass-font-base;
				color: $font-color-light;
				margin: 0 20upx 0 40upx;
			}

			.timer {
				display: inline-block;
				width: 40upx;
				height: 36upx;
				text-align: center;
				line-height: 36upx;
				margin-right: 14upx;
				font-size: $glass-font-sm;
				color: #fff;
				border-radius: $glass-radius-sm;
				background: rgba($glass-primary, 0.9);
				font-weight: 600;
			}

			.icon-you {
				font-size: $glass-font-lg;
				color: $glass-accent;
				flex: 1;
				text-align: right;
				font-weight: 600;
			}
		}

		.floor-list {
			white-space: nowrap;
		}

		.scoll-wrapper {
			display: flex;
			align-items: flex-start;
			padding: $glass-spacing-xs 0;
		}

		.floor-item {
			width: 300upx;
			margin-right: $glass-spacing-lg;
			padding: $glass-spacing-base;
			@include glass-effect(0.85, 10px);
			border-radius: $glass-radius-lg;
			font-size: $glass-font-sm;
			color: $font-color-dark;
			line-height: 1.8;
			transition: all $glass-transition-base;
			font-family: $glass-font-body;

			&:active {
				transform: translateY(4px);
				box-shadow: $glass-shadow-sm;
			}

			image {
				width: 300upx;
				height: 300upx;
				border-radius: $glass-radius-base;
				margin-bottom: $glass-spacing-base;
				transition: transform $glass-transition-base;
			}

			&:active image {
				transform: scale(1.05);
			}

			.price {
				color: $glass-primary;
				font-weight: 700;
				font-size: $glass-font-lg;
				margin-top: $glass-spacing-sm;
			}
		}

		.title2 {
			font-size: $glass-font-sm;
			color: $font-color-light;
			line-height: 1.4;
			margin-bottom: $glass-spacing-xs;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}
	}

	.f-header {
		display: flex;
		align-items: center;
		height: 140upx;
		padding: 6upx 30upx 8upx;
		@include glass-effect(0.85, 15px);
		border-radius: $glass-radius-lg;
		margin: $glass-spacing-base $page-row-spacing;
		font-family: $glass-font-heading;
		transition: all $glass-transition-base;

		&:active {
			transform: translateY(2px);
			box-shadow: $glass-shadow-sm;
		}

		image {
			flex-shrink: 0;
			width: 80upx;
			height: 80upx;
			margin-right: 20upx;
			border-radius: $glass-radius-base;
			box-shadow: $glass-shadow-sm;
		}

		.tit-box {
			flex: 1;
			display: flex;
			flex-direction: column;
		}

		.tit {
			font-size: $glass-font-lg;
			color: $font-color-dark;
			line-height: 1.3;
			font-weight: 600;
		}

		.tit2 {
			font-size: $glass-font-sm;
			color: $font-color-light;
			font-weight: 400;
		}

		.icon-you {
			font-size: $glass-font-lg;
			color: $glass-accent;
			font-weight: 600;
		}

		.timer {
			display: inline-block;
			width: 40upx;
			height: 36upx;
			text-align: center;
			line-height: 36upx;
			margin-right: 14upx;
			font-size: $glass-font-sm;
			color: #fff;
			border-radius: $glass-radius-sm;
			background: rgba($glass-primary, 0.9);
			font-weight: 600;
		}
	}

	/* 分类推荐楼层 */
	.hot-floor {
		width: 100%;
		overflow: hidden;
		margin-bottom: $glass-spacing-lg;

		.floor-img-box {
			width: 100%;
			height: 320upx;
			position: relative;
			border-radius: $glass-radius-lg;
			overflow: hidden;
			@include glass-effect(0.7, 5px);
			margin: $glass-spacing-base $page-row-spacing;

			&:after {
				content: '';
				position: absolute;
				left: 0;
				top: 0;
				width: 100%;
				height: 100%;
				background: linear-gradient(rgba($glass-primary, 0.1) 30%, transparent);
			}
		}

		.floor-img {
			width: 100%;
			height: 100%;
		}

		.floor-list {
			white-space: nowrap;
			padding: $glass-spacing-base;
			padding-right: 50upx;
			border-radius: $glass-radius-lg;
			margin-top: -140upx;
			margin-left: $page-row-spacing;
			@include glass-card;
			position: relative;
			z-index: 1;
		}

		.scoll-wrapper {
			display: flex;
			align-items: flex-start;
		}

		.floor-item {
			width: 180upx;
			margin-right: $glass-spacing-lg;
			padding: $glass-spacing-base;
			@include glass-effect(0.85, 10px);
			border-radius: $glass-radius-base;
			font-size: $glass-font-sm;
			color: $font-color-dark;
			line-height: 1.8;
			transition: all $glass-transition-base;
			font-family: $glass-font-body;

			&:active {
				transform: translateY(4px);
				box-shadow: $glass-shadow-sm;
			}

			image {
				width: 180upx;
				height: 180upx;
				border-radius: $glass-radius-base;
				margin-bottom: $glass-spacing-sm;
				transition: transform $glass-transition-base;
			}

			&:active image {
				transform: scale(1.05);
			}

			.price {
				color: $glass-primary;
				font-weight: 700;
				font-size: $glass-font-base;
				margin-top: $glass-spacing-xs;
			}
		}

		.more {
			display: flex;
			align-items: center;
			justify-content: center;
			flex-direction: column;
			flex-shrink: 0;
			width: 180upx;
			height: 180upx;
			border-radius: $glass-radius-base;
			@include glass-effect(0.9, 10px);
			font-size: $glass-font-base;
			color: $font-color-light;
			transition: all $glass-transition-base;

			&:active {
				transform: scale(0.95);
			}

			text:first-child {
				margin-bottom: $glass-spacing-xs;
			}
		}
	}

	/* 猜你喜欢 */
	.guess-section {
		display: flex;
		flex-wrap: wrap;
		padding: $glass-spacing-base $page-row-spacing;

		.guess-item {
			display: flex;
			flex-direction: column;
			width: 48%;
			padding: $glass-spacing-lg;
			margin-bottom: $glass-spacing-lg;
			@include glass-card;
			transition: all $glass-transition-base;
			font-family: $glass-font-body;

			&:nth-child(2n+1) {
				margin-right: 4%;
			}

			&:active {
				transform: translateY(4px);
				box-shadow: $glass-shadow-sm;
			}
		}

		.image-wrapper {
			width: 100%;
			height: 330upx;
			border-radius: $glass-radius-base;
			overflow: hidden;
			margin-bottom: $glass-spacing-base;

			image {
				width: 100%;
				height: 100%;
				opacity: 1;
				transition: transform $glass-transition-base;
			}

			&:active image {
				transform: scale(1.05);
			}
		}

		.image-wrapper-brand {
			width: 100%;
			height: 150upx;
			border-radius: $glass-radius-base;
			overflow: hidden;
			margin-bottom: $glass-spacing-base;

			image {
				width: 100%;
				height: 100%;
				opacity: 1;
				transition: transform $glass-transition-base;
			}

			&:active image {
				transform: scale(1.05);
			}
		}

		.title {
			font-size: $glass-font-base;
			color: $font-color-dark;
			line-height: 1.4;
			font-weight: 600;
			margin-bottom: $glass-spacing-xs;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.title2 {
			font-size: $glass-font-sm;
			color: $font-color-light;
			line-height: 1.4;
			margin-bottom: $glass-spacing-base;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.price {
			font-size: $glass-font-lg;
			color: $glass-primary;
			line-height: 1;
			font-weight: 700;
			margin-top: auto;
		}
	}

	.hot-section {
		display: flex;
		flex-wrap: wrap;
		padding: $glass-spacing-base $page-row-spacing;

		.guess-item {
			display: flex;
			flex-direction: row;
			width: 100%;
			padding: $glass-spacing-lg;
			margin-bottom: $glass-spacing-lg;
			@include glass-card;
			border-radius: $glass-radius-lg;
			transition: all $glass-transition-base;
			font-family: $glass-font-body;

			&:active {
				transform: translateY(4px);
				box-shadow: $glass-shadow-sm;
			}
		}

		.image-wrapper {
			width: 30%;
			height: 250upx;
			border-radius: $glass-radius-base;
			overflow: hidden;
			flex-shrink: 0;

			image {
				width: 100%;
				height: 100%;
				opacity: 1;
				transition: transform $glass-transition-base;
			}

			&:active image {
				transform: scale(1.05);
			}
		}

		.title {
			font-size: $glass-font-base;
			color: $font-color-dark;
			line-height: 1.4;
			font-weight: 600;
			margin-bottom: $glass-spacing-xs;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.title2 {
			font-size: $glass-font-sm;
			color: $font-color-light;
			line-height: 1.4;
			margin-bottom: $glass-spacing-base;
			display: -webkit-box;
			-webkit-line-clamp: 3;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		.price {
			font-size: $glass-font-lg;
			color: $glass-primary;
			line-height: 1;
			font-weight: 700;
			margin-top: auto;
		}

		.txt {
			width: 70%;
			display: flex;
			flex-direction: column;
			padding-left: $glass-spacing-lg;
			justify-content: space-between;
		}
	}
</style>
