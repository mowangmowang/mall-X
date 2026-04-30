<template>
	<view class="container">
		<view class="left-bottom-sign"></view>
		<view class="back-btn yticon icon-zuojiantou-up" @click="navBack"></view>
		<view class="right-top-sign"></view>
		<!-- 设置白色背景防止软键盘把下部绝对定位元素顶上来盖住输入框等 -->
		<view class="wrapper">
			<view class="left-top-sign">REGISTER</view>
			<view class="welcome">
				欢迎注册！
			</view>
			<view class="input-content">
				<view class="input-item">
					<text class="tit">用户名</text>
					<input type="text" v-model="username" placeholder="请输入用户名" maxlength="20"/>
				</view>
				<view class="input-item">
					<text class="tit">密码</text>
					<input type="text" v-model="password" placeholder="8-18位不含特殊字符的数字、字母组合" placeholder-class="input-empty" maxlength="20"
					 password />
				</view>
				<view class="input-item">
					<text class="tit">确认密码</text>
					<input type="text" v-model="confirmPassword" placeholder="请再次输入密码" placeholder-class="input-empty" maxlength="20"
					 password />
				</view>
				<view class="input-item">
					<text class="tit">手机号</text>
					<input type="number" v-model="telephone" placeholder="请输入手机号" maxlength="11"/>
				</view>
				<view class="input-item">
					<text class="tit">验证码</text>
					<input type="text" v-model="authCode" placeholder="请输入验证码" maxlength="6" style="flex:1"/>
					<button class="auth-code-btn" @click="getAuthCode" :disabled="authCodeSending">{{authCodeText}}</button>
				</view>
			</view>
			<button class="confirm-btn" @click="toRegister" :disabled="registering">注册</button>
			<view class="login-section">
				已有账号?
				<text @click="toLogin">马上登录</text>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		memberRegister,
		fetchAuthCode
	} from '@/api/member.js';
	export default {
		data() {
			return {
				username: '',
				password: '',
				confirmPassword: '',
				telephone: '',
				authCode: '',
				authCodeSending: false,
				authCodeText: '获取验证码',
				registering: false
			}
		},
		onLoad() {
		},
		methods: {
			navBack() {
				uni.navigateBack();
			},
			toLogin() {
				uni.navigateTo({url:'/pages/public/login'});
			},
			async toRegister() {
				// 验证输入
				if (!this.username || !this.password || !this.confirmPassword || !this.telephone || !this.authCode) {
					uni.showToast({
						title: '请填写完整信息',
						icon: 'none'
					});
					return;
				}
				if (this.password !== this.confirmPassword) {
					uni.showToast({
						title: '两次密码不一致',
						icon: 'none'
					});
					return;
				}
				if (this.password.length < 8 || this.password.length > 18) {
					uni.showToast({
						title: '密码长度需8-18位',
						icon: 'none'
					});
					return;
				}
				if (!/^1[3-9]\d{9}$/.test(this.telephone)) {
					uni.showToast({
						title: '手机号格式不正确',
						icon: 'none'
					});
					return;
				}
				this.registering = true;
				uni.showLoading({ title: '注册中...' });
				try {
					await memberRegister({
						username: this.username,
						password: this.password,
						telephone: this.telephone,
						authCode: this.authCode
					});
					uni.hideLoading();
					uni.showToast({
						title: '注册成功，请登录',
						icon: 'success'
					});
					// 注册成功后跳转到登录页面
					setTimeout(() => {
						uni.navigateTo({url:'/pages/public/login'});
					}, 1500);
				} catch (error) {
					uni.hideLoading();
					console.error('注册失败:', error);
				} finally {
					this.registering = false;
				}
			},
			//获取验证码
			getAuthCode() {
				if (!/^1[3-9]\d{9}$/.test(this.telephone)) {
					uni.showToast({
						title: '手机号格式不正确',
						icon: 'none'
					});
					return;
				}
				this.authCodeSending = true;
				fetchAuthCode(this.telephone).then(response => {
					this.authCode = response.data;
					uni.showToast({
						title: '验证码已发：' + response.data,
						icon: 'none'
					});
					// 60秒倒计时
					let countdown = 60;
					this.authCodeText = countdown + 's';
					let timer = setInterval(() => {
						countdown--;
						this.authCodeText = countdown + 's';
						if (countdown <= 0) {
							clearInterval(timer);
							this.authCodeText = '重新获取';
							this.authCodeSending = false;
						}
					}, 1000);
				}).catch(() => {
					this.authCodeSending = false;
					this.authCodeText = '获取验证码';
				});
			}
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

	.login-section {
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

	.auth-code-btn {
		position: absolute;
		right: 0;
		top: 50%;
		transform: translateY(-50%);
		height: 56upx;
		line-height: 56upx;
		padding: 0 20upx;
		font-size: 24upx;
		color: #fff;
		background: $color-primary;
		border-radius: 28upx;
		border: none;
		min-width: 160upx;
		text-align: center;

		&[disabled] {
			opacity: 0.6;
		}
	}

	.input-item {
		position: relative;
	}
</style>
