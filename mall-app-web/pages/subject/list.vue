<template>
	<view>
		<view class="subject-list">
			<view v-for="(item, index) in subjectList" :key="index" class="subject-item" @click="navToDetail(item)">
				<view class="image-wrapper">
					<image :src="item.pic" mode="aspectFill" v-if="item.pic"></image>
					<view class="img-placeholder" v-else></view>
				</view>
				<view class="info">
					<text class="title clamp">{{item.title}}</text>
					<text class="desc clamp">{{item.description}}</text>
					<view class="stats">
						<text>{{item.readCount || 0}}人阅读</text>
						<text>{{item.collectCount || 0}}人收藏</text>
						<text>{{item.productCount || 0}}件商品</text>
					</view>
				</view>
			</view>
		</view>
		<uni-load-more :status="loadingType"></uni-load-more>
	</view>
</template>

<script>
	import { fetchSubjectList } from '@/api/home.js';
	import uniLoadMore from '@/components/uni-load-more/uni-load-more.vue';
	export default {
		components: { uniLoadMore },
		data() {
			return {
				subjectList: [],
				pageNum: 1,
				pageSize: 10,
				loadingType: 'loading'
			};
		},
		onLoad() {
			this.loadData();
		},
		onReachBottom() {
			this.pageNum++;
			this.loadData(true);
		},
		methods: {
			loadData(append = false) {
				this.loadingType = 'loading';
				fetchSubjectList({ pageNum: this.pageNum, pageSize: this.pageSize }).then(response => {
					const list = response.data?.list || response.data || [];
					this.subjectList = append ? this.subjectList.concat(list) : list;
					this.loadingType = list.length < this.pageSize ? 'nomore' : 'more';
				}).catch(() => {
					this.loadingType = 'nomore';
				});
			},
			navToDetail(item) {
				uni.navigateTo({ url: `/pages/subject/subject?id=${item.id}` });
			}
		}
	}
</script>

<style lang="scss">
	page { background: $color-bg-secondary; }

	.subject-list {
		padding: 20upx;
	}

	.subject-item {
		display: flex;
		background: #fff;
		border-radius: 12upx;
		overflow: hidden;
		margin-bottom: 20upx;
		padding: 20upx;

		&:active { opacity: 0.8; }

		.image-wrapper {
			width: 200upx;
			height: 200upx;
			border-radius: 8upx;
			overflow: hidden;
			flex-shrink: 0;
			margin-right: 20upx;
			image { width: 100%; height: 100%; }
		}

		.img-placeholder {
			width: 100%;
			height: 100%;
			background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
			background-size: 400% 400%;
			animation: drift 8s ease infinite;
		}

		@keyframes drift {
			0% { background-position: 0% 0%; }
			25% { background-position: 100% 0%; }
			50% { background-position: 100% 100%; }
			75% { background-position: 0% 100%; }
			100% { background-position: 0% 0%; }
		}

		.info {
			flex: 1;
			display: flex;
			flex-direction: column;
			justify-content: space-between;
		}

		.title {
			font-size: $font-lg;
			color: $font-color-dark;
			font-weight: 600;
			line-height: 1.4;
		}

		.desc {
			font-size: $font-sm;
			color: $font-color-light;
			line-height: 1.4;
			margin: 8upx 0;
		}

		.stats {
			display: flex;
			font-size: 22upx;
			color: $color-secondary;
			text { margin-right: 20upx; }
		}
	}
</style>
