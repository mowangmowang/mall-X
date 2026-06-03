<template>
	<view class="container">
		<view class="left-bottom-sign"></view>
		<view class="back-btn yticon icon-zuojiantou-up" @click="navBack"></view>
		<view class="right-top-sign"></view>
		<!-- 设置白色背景防止软键盘把下部绝对定位元素顶上来盖住输入框等 -->
		<view class="wrapper">
			<view class="left-top-sign">LOGIN</view>
			<view class="welcome">
				欢迎回来！
			</view>
			<view class="input-content">
				<view class="input-item">
					<text class="tit">用户名</text>
					<input type="text" v-model="username" placeholder="请输入用户名" maxlength="11"/>
				</view>
				<view class="input-item">
					<text class="tit">密码</text>
					<input type="text" v-model="password" placeholder="8-18位不含特殊字符的数字、字母组合" placeholder-class="input-empty" maxlength="20"
					 password @confirm="toLogin" />
				</view>
			</view>
			<button class="confirm-btn" @click="toLogin" :disabled="logining">登录</button>
			<view class="forget-section" @click="toRegist">
				忘记密码?
			</view>
		</view>
		<view class="register-section">
			还没有账号?
			<text @click="toRegist">马上注册</text>
		</view>
	</view>
</template>

<script>
	import {
		mapMutations
	} from 'vuex';
	import {
		memberLogin,memberInfo
	} from '@/api/member.js';
	export default {
		data() {
			return {
				username: '',
				password: '',
				logining: false
			}
		},
		onLoad() {
			this.username = uni.getStorageSync('username') || '';
			this.password = uni.getStorageSync('password') || '';
		},
		methods: {
			...mapMutations(['login']),
			navBack() {
				uni.navigateBack();
			},
			toRegist() {
				uni.navigateTo({url:'/pages/public/register'});
			},
			async toLogin() {
				this.logining = true;
				uni.showLoading({ title: '登录中...' });
				memberLogin({
					username: this.username,
					password: this.password
				}).then(response => {
					let token = response.data.tokenHead+response.data.token;
					uni.setStorageSync('token',token);
					uni.setStorageSync('username',this.username);
					uni.setStorageSync('password',this.password);
					memberInfo().then(response=>{
						this.login(response.data);
						uni.hideLoading();
						uni.navigateBack();
					});
				}).catch(() => {
					this.logining = false;
					uni.hideLoading();
				});
			},
		},

	}
</script>

<style lang='scss'>
	page {
		background: $color-bg;
	}

	.container {
		padding-top: 115px;
		position: relative;
		width: 100vw;
		height: 100vh;
		overflow: hidden;
		background: $color-bg;
	}

	.wrapper {
		position: relative;
		z-index: $z-sticky;
		background: $color-bg;
		padding-bottom: 40upx;
	}

	.back-btn {
		position: absolute;
		left: 24upx;
		z-index: $z-modal;
		padding: 20upx 24upx;
		padding-top: calc(var(--status-bar-height) + 20upx);
		top: 20upx;
		font-size: 40upx;
		color: $font-color-dark;
	}

	.left-top-sign {
		font-size: 120upx;
		color: $color-bg-secondary;
		position: relative;
		left: -16upx;
	}

	.right-top-sign {
		position: absolute;
		top: 80upx;
		right: -30upx;
		z-index: $z-sticky;

		&:before,
		&:after {
			display: block;
			content: "";
			width: 400upx;
			height: 80upx;
			background: $color-bg-secondary;
		}

		&:before {
			transform: rotate(50deg);
			border-radius: 0 50px 0 0;
		}

		&:after {
			position: absolute;
			right: -198upx;
			top: 0;
			transform: rotate(-50deg);
			border-radius: 50px 0 0 0;
		}
	}

	.left-bottom-sign {
		position: absolute;
		left: -270upx;
		bottom: -320upx;
		border: 100upx solid $color-bg-secondary;
		border-radius: 50%;
		padding: 180upx;
	}

	.welcome {
		position: relative;
		left: 50upx;
		top: -90upx;
		font-size: 46upx;
		color: $font-color-dark;
	}

	.input-content {
		padding: 0 60upx;
	}

	.input-item {
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		justify-content: center;
		padding: 0 30upx;
		background: $color-bg-secondary;
		height: 120upx;
		border-radius: 4px;
		margin-bottom: 50upx;

		&:last-child {
			margin-bottom: 0;
		}

		.tit {
			height: 50upx;
			line-height: 56upx;
			font-size: $font-sm+2upx;
			color: $font-color-base;
		}

		input {
			height: 60upx;
			font-size: $font-base + 2upx;
			color: $font-color-dark;
			width: 100%;
		}
	}

	.confirm-btn {
		width: 630upx;
		height: 76upx;
		line-height: 76upx;
		border-radius: 50px;
		margin-top: 70upx;
		background: $color-primary;
		color: #fff;
		font-size: $font-lg;

		&:after {
			border-radius: 100px;
		}

		&[disabled] {
			opacity: 0.5;
		}
	}

	.forget-section {
		font-size: $font-sm+2upx;
		color: $font-color-base;
		text-align: center;
		margin-top: 40upx;
	}

	.register-section {
		position: absolute;
		left: 0;
		bottom: 50upx;
		width: 100%;
		font-size: $font-sm+2upx;
		color: $font-color-base;
		text-align: center;

		text {
			color: $font-color-dark;
			margin-left: 10upx;
			font-weight: 600;
		}
	}
</style>
