<template>
	<view>
		<view class="subject-list">
			<view v-for="(item, index) in subjectList" :key="index" class="subject-item" @click="navToDetail(item)">
				<view class="art-block">
					<text class="art-icon">✦</text>
					<text class="art-label">{{item.title ? item.title.substring(0, 2) : 'ZT'}}</text>
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

		.art-block {
			width: 160upx;
			height: 160upx;
			border-radius: 12upx;
			flex-shrink: 0;
			margin-right: 20upx;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			background: linear-gradient(135deg, #a18cd1, #fbc2eb, #a6c1ee);
			background-size: 300% 300%;
			animation: artFlow 4s ease infinite;

			.art-icon {
				font-size: 42upx;
				color: rgba(255,255,255,0.85);
			}

			.art-label {
				font-size: 20upx;
				color: rgba(255,255,255,0.75);
				margin-top: 6upx;
				font-weight: 500;
			}
		}

		@keyframes artFlow {
			0% { background-position: 0% 50%; }
			50% { background-position: 100% 50%; }
			100% { background-position: 0% 50%; }
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
