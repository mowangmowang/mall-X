<template>
	<view>
		<view class="topic-header">
			<text class="header-icon">💬</text>
			<text class="header-title">热门话题</text>
			<text class="header-sub">一起来参与讨论</text>
		</view>
		<view class="topic-list">
			<view v-for="(item, index) in topicList" :key="index" class="topic-item" @click="navToDetail(item)">
				<view class="art-block" :style="artStyle(index)">
					<text class="art-char">{{item.name ? item.name.substring(0, 2) : '话'}}</text>
					<view class="art-dot" v-for="i in 3" :key="i" :style="dotStyle(i)"></view>
				</view>
				<view class="txt">
					<view class="title-row">
						<text class="title clamp">{{item.name}}</text>
						<text class="hot-badge" v-if="item.attendCount > 50">🔥 热门</text>
					</view>
					<text class="desc clamp">{{item.content ? stripHtml(item.content).substring(0, 80) : (item.awardName ? '奖品：' + item.awardName : (item.attendType ? '参与方式：' + item.attendType : ''))}}</text>
					<view class="meta-row">
						<text class="tag tag-attend" v-if="item.attendType">{{item.attendType}}</text>
						<text class="tag tag-award" v-if="item.awardName">🎁 {{item.awardName}}</text>
					</view>
					<view class="stats">
						<text class="stat">👤 {{item.attendCount || 0}}</text>
						<text class="stat">👁 {{item.readCount || 0}}</text>
						<text class="stat">⭐ {{item.attentionCount || 0}}</text>
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
				loadingType: 'loading',
				colors: [
					['#ff9a9e', '#fad0c4'],
					['#a18cd1', '#fbc2eb'],
					['#fccb90', '#d57eeb'],
					['#96fbc4', '#f9f586'],
					['#a6c1ee', '#fbc2eb'],
					['#84fab0', '#8fd3f4'],
					['#cfd9df', '#e2ebf0'],
					['#fa709a', '#fee140']
				]
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
			},
			stripHtml(html) {
				if (!html) return '';
				return html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').trim();
			},
			artStyle(index) {
				const c = this.colors[index % this.colors.length];
				return { background: `linear-gradient(135deg, ${c[0]}, ${c[1]})` };
			},
			dotStyle(i) {
				const sizes = [12, 8, 16];
				const positions = ['20% 80%', '70% 20%', '85% 75%'];
				return {
					width: sizes[i-1] + 'upx',
					height: sizes[i-1] + 'upx',
					left: positions[i-1].split(' ')[0],
					top: positions[i-1].split(' ')[1]
				};
			}
		}
	}
</script>

<style lang="scss">
	page { background: $color-bg-secondary; }

	.topic-header {
		padding: 40upx 30upx 20upx;
		display: flex;
		flex-direction: column;
		align-items: center;

		.header-icon { font-size: 48upx; }
		.header-title { font-size: 36upx; color: $font-color-dark; font-weight: 700; margin-top: 8upx; }
		.header-sub { font-size: 24upx; color: $font-color-light; margin-top: 4upx; }
	}

	.topic-list {
		padding: 10upx 20upx;
	}

	.topic-item {
		display: flex;
		background: rgba(255,255,255,0.85);
		backdrop-filter: blur(10px);
		border-radius: 16upx;
		overflow: hidden;
		margin-bottom: 20upx;
		padding: 24upx;
		box-shadow: 0 4upx 20upx rgba(0,0,0,0.04);

		&:active { opacity: 0.8; }

		.art-block {
			width: 120upx;
			height: 120upx;
			border-radius: 16upx;
			flex-shrink: 0;
			margin-right: 20upx;
			position: relative;
			display: flex;
			align-items: center;
			justify-content: center;
			overflow: hidden;

			.art-char {
				font-size: 40upx;
				color: #fff;
				font-weight: 700;
				text-shadow: 0 2upx 8upx rgba(0,0,0,0.15);
				z-index: 1;
			}

			.art-dot {
				position: absolute;
				border-radius: 50%;
				background: rgba(255,255,255,0.25);
			}
		}

		.txt {
			flex: 1;
			display: flex;
			flex-direction: column;
			min-width: 0;
		}

		.title-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
		}

		.title {
			font-size: 32upx;
			color: $font-color-dark;
			font-weight: 600;
			line-height: 1.4;
			flex: 1;
		}

		.hot-badge {
			font-size: 20upx;
			color: #fff;
			background: linear-gradient(135deg, #ff6b6b, #ee5a24);
			padding: 2upx 16upx;
			border-radius: 20upx;
			margin-left: 12upx;
			flex-shrink: 0;
		}

		.desc {
			font-size: 24upx;
			color: $font-color-light;
			line-height: 1.5;
			margin: 8upx 0;
		}

		.meta-row {
			display: flex;
			flex-wrap: wrap;
			gap: 8upx;
			margin-bottom: 8upx;
		}

		.tag {
			font-size: 20upx;
			padding: 2upx 14upx;
			border-radius: 12upx;
			line-height: 1.6;
		}

		.tag-attend {
			background: #e8f5e9;
			color: #2e7d32;
		}

		.tag-award {
			background: #fff3e0;
			color: #e65100;
		}

		.stats {
			display: flex;
			font-size: 22upx;
			color: $color-secondary;

			.stat { margin-right: 20upx; }
		}
	}
</style>
