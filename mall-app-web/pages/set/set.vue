<template>
	<view class="container">
		<view class="list-cell b-b" @click="navTo('/pages/address/address')" hover-class="cell-hover" :hover-stay-time="50">
			<text class="cell-tit">收货地址</text>
			<text class="cell-more yticon icon-you"></text>
		</view>

		<view class="list-cell m-t">
			<text class="cell-tit">消息推送</text>
			<switch checked color="#07c160" @change="switchChange" />
		</view>
		<view class="list-cell m-t b-b" @click="navTo('清除缓存')" hover-class="cell-hover" :hover-stay-time="50">
			<text class="cell-tit">清除缓存</text>
			<text class="cell-more yticon icon-you"></text>
		</view>
		<view class="list-cell b-b" @click="navToOuter('https://github.com/macrozheng/mall')" hover-class="cell-hover" :hover-stay-time="50">
			<text class="cell-tit">关于mall-app-web</text>
			<text class="cell-more yticon icon-you"></text>
		</view>
		<view class="list-cell">
			<text class="cell-tit">检查更新</text>
			<text class="cell-tip">当前版本 1.0.0</text>
			<text class="cell-more yticon icon-you"></text>
		</view>
		<view class="list-cell log-out-btn" @click="toLogout">
			<text class="cell-tit">退出登录</text>
		</view>
	</view>
</template>

<script>
	import {
	    mapMutations
	} from 'vuex';
	export default {
		data() {
			return {

			};
		},
		methods:{
			...mapMutations(['logout']),

			navTo(url){
				if(url.indexOf("pages")!=-1){
					uni.navigateTo({
						url:url
					});
				}
				this.$api.msg(`跳转到${url}`);
			},
			navToOuter(url){
				window.location.href = url;
			},
			//退出登录
			toLogout(){
				uni.showModal({
				    content: '确定要退出登录么',
				    success: (e)=>{
				    	if(e.confirm){
				    		this.logout();
				    		setTimeout(()=>{
				    			uni.navigateBack();
				    		}, 200)
				    	}
				    }
				});
			},
			//switch
			switchChange(e){
				let statusTip = e.detail.value ? '打开': '关闭';
				this.$api.msg(`${statusTip}消息推送`);
			},

		}
	}
</script>

<style lang='scss'>
	page{
		background: $color-bg-secondary;
	}
	.list-cell{
		display:flex;
		align-items:baseline;
		padding: 20upx $page-row-spacing;
		line-height:60upx;
		position:relative;
		background: $color-bg;
		justify-content: center;
		&.log-out-btn{
			margin-top: 40upx;
			.cell-tit{
				color: $color-primary;
				text-align: center;
				margin-right: 0;
			}
		}
		&.cell-hover{
			background:$color-bg-secondary;
		}
		&.b-b:after{
			left: 30upx;
		}
		&.m-t{
			margin-top: 16upx;
		}
		.cell-more{
			align-self: baseline;
			font-size:$font-lg;
			color:$font-color-light;
			margin-left:10upx;
		}
		.cell-tit{
			flex: 1;
			font-size: $font-base + 2upx;
			color: $font-color-dark;
			margin-right:10upx;
		}
		.cell-tip{
			font-size: $font-base;
			color: $font-color-light;
		}
		switch{
			transform: translateX(16upx) scale(.84);
		}
	}
</style>
