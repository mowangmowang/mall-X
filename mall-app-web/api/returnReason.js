import request from '@/utils/requestUtil';

/**
 * 获取启用的退货原因列表
 * @returns {Promise} 退货原因列表
 */
export function getEnabledReturnReasons() {
	return request({
		url: '/returnReason/list',
		method: 'GET',
		params: {
			pageNum: 1,
			pageSize: 100,
			status: 1  // 只获取启用的原因
		}
	});
}
