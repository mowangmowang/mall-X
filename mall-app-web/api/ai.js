import Request from '@/js_sdk/luch-request/request.js'
import { AI_BASE_URL } from '@/utils/appConfig.js';

const aiRequest = new Request()

aiRequest.setConfig((config) => {
	config.baseUrl = AI_BASE_URL
	config.header = {
		...config.header
	}
	return config
})

aiRequest.validateStatus = (statusCode) => {
	return statusCode === 200
}

aiRequest.interceptor.response((response) => {
	const res = response.data;
	if (res.code !== 200) {
		return Promise.reject(response);
	} else {
		return response.data;
	}
}, (response) => {
	return Promise.reject(response);
})

/**
 * AI商品问答
 */
export function aiProductQa(data) {
	return aiRequest.request({
		method: 'POST',
		url: '/ai/product/qa',
		data: data
	});
}

/**
 * AI退货建议
 */
export function aiReturnSuggest(data) {
	return aiRequest.request({
		method: 'POST',
		url: '/ai/return/suggest',
		data: data
	});
}
