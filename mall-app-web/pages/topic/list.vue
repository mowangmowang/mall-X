<template>
	<view>
		<view class="topic-list">
			<view v-for="(item, index) in topicList" :key="index" class="topic-item" @click="navToDetail(item)">
				<view class="txt">
					<text class="title clamp">{{item.name}}</text>
					<text class="desc clamp">{{item.content ? item.content.substring(0, 80) : ''}}</text>
					<view class="stats">
						<text>{{item.attendCount || 0}}人参与</text>
						<text>{{item.readCount || 0}}次阅读</text>
						<text>{{item.attentionCount || 0}}人关注</text>
					</view>
				</view>
			</view>
		</view>
		<uni-load-more :status="loadingType"></uni-load-more>
	</view>
</template>

<script>
	import { fetchTopicList } from '@/api/home.js';
	import uniLoadMore from '@/components/uni-load-more/uni-load-more.vue';
	export default {
		components: { uniLoadMore },
		data() {
			return {
				topicList: [],
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
				fetchTopicList({ pageNum: this.pageNum, pageSize: this.pageSize }).then(response => {
					const list = response.data || [];
					this.topicList = append ? this.topicList.concat(list) : list;
					this.loadingType = list.length < this.pageSize ? 'nomore' : 'more';
				}).catch(() => {
					this.loadingType = 'nomore';
				});
			},
			navToDetail(item) {
				uni.navigateTo({ url: `/pages/topic/topic?id=${item.id}` });
			}
		}
	}
</script>

<style lang="scss">
	page { background: $color-bg-secondary; }

	.topic-list {
		padding: 20upx;
	}

	.topic-item {
		background: #fff;
		border-radius: 12upx;
		overflow: hidden;
		margin-bottom: 20upx;
		padding: 30upx;

		&:active { opacity: 0.8; }

		.txt {
			display: flex;
			flex-direction: column;
		}

		.title {
			font-size: $font-lg+2upx;
			color: $font-color-dark;
			font-weight: 600;
			line-height: 1.4;
		}

		.desc {
			font-size: $font-sm;
			color: $font-color-light;
			line-height: 1.6;
			margin: 12upx 0;
		}

		.stats {
			display: flex;
			font-size: 22upx;
			color: $color-secondary;
			text { margin-right: 20upx; }
		}
	}
</style>
