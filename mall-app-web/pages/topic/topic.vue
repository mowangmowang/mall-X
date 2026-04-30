<template>
	<view>
		<!-- 话题头部 -->
		<view class="header">
			<text class="name">{{topic.name}}</text>
			<view class="stats">
				<text>{{topic.attendCount || 0}}人参与</text>
				<text>{{topic.readCount || 0}}次阅读</text>
				<text>{{topic.attentionCount || 0}}人关注</text>
			</view>
		</view>
		<!-- 话题内容 -->
		<view class="content-section" v-if="topic.content">
			<rich-text class="rich-text" :nodes="topic.content"></rich-text>
		</view>
	</view>
</template>

<script>
	import { fetchTopicDetail } from '@/api/home.js';
	export default {
		data() {
			return { topic: {} };
		},
		onLoad(options) {
			let id = options.id;
			fetchTopicDetail(id).then(response => {
				this.topic = response.data;
				uni.setNavigationBarTitle({ title: this.topic.name || '话题详情' });
			});
		}
	}
</script>

<style lang="scss">
	page { background: $color-bg-secondary; }

	.header {
		padding: 40upx 30upx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		.name { font-size: $font-lg+8upx; color: #fff; font-weight: 600; }
		.stats {
			display: flex; margin-top: 24upx;
			text { font-size: $font-sm; color: rgba(255,255,255,0.8); margin-right: 30upx; }
		}
	}

	.content-section {
		background: #fff; margin-top: 16upx; padding: 30upx;
		.rich-text { font-size: $font-base; color: $font-color-dark; line-height: 1.8; }
	}
</style>
