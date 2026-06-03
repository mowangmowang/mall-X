<template>
	<view class="content">

		<view class="mix-list-cell" :class="border" @click="eventClick" hover-class="cell-hover"  :hover-stay-time="50">
			<text
				v-if="icon"
				class="cell-icon yticon"
				:style="[{
					color: iconColor,
				}]"
				:class="icon"
			></text>
			<text class="cell-tit clamp">{{title}}</text>
			<text v-if="tips" class="cell-tip">{{tips}}</text>
			<text class="cell-more yticon"
				:class="typeList[navigateType]"
			></text>
		</view>

	</view>
</template>

<script>
	/**
	 *  简单封装了下， 应用范围比较狭窄，可以在此基础上进行扩展使用
	 *  比如加入image， iconSize可控等
	 */
	export default {
		data() {
			return {
				typeList: {
					left: 'icon-zuo',
					right: 'icon-you',
					up: 'icon-shang',
					down: 'icon-xia'
				},
			}
		},
		props: {
			icon: {
				type: String,
				default: ''
			},
			title: {
				type: String,
				default: '标题'
			},
			tips: {
				type: String,
				default: ''
			},
			navigateType: {
				type: String,
				default: 'right'
			},
			border: {
				type: String,
				default: 'b-b'
			},
			hoverClass: {
				type: String,
				default: 'cell-hover'
			},
			iconColor: {
				type: String,
				default: '#333'
			}
		},
		methods: {
			eventClick(){
				this.$emit('eventClick');
			}
		},
	}
</script>

<style lang='scss'>
	.icon .mix-list-cell.b-b:after{
		left: 90upx;
	}
	.mix-list-cell{
		display:flex;
		align-items:baseline;
		padding: $spacing-lg $page-row-spacing;
		line-height:60upx;
		position:relative;
		background: $color-bg;
		border-radius: $radius-base;
		margin: $spacing-sm $page-row-spacing;

		&.cell-hover{
			background: $color-bg-secondary;
		}
		&.b-b:after{
			display: none;
		}

		.cell-icon{
			align-self:center;
			width:56upx;
			max-height:60upx;
			font-size:38upx;
			color: $color-primary;
			margin-right: $spacing-base;
		}
		.cell-more{
			align-self: center;
			font-size:30upx;
			color: $color-secondary;
			margin-left:$spacing-sm;
		}
		.cell-tit{
			flex: 1;
			font-size: $font-base;
			font-weight: 600;
			color: $font-color-dark;
			margin-right:10upx;
		}
		.cell-tip{
			font-size: $font-sm;
			color: $font-color-light;
		}
	}
</style>
