import request from '@/utils/requestUtil'
import { ES_SEARCH_BASE_URL } from '@/utils/appConfig.js'

/**
 * 搜索商品列表 — 智能路由
 * 优先请求 mall-search (Elasticsearch)，失败时自动降级到 mall-portal (MySQL)
 */
export async function searchProductList(params) {
	// 过滤掉 null 和 undefined 的参数，避免后端 NumberFormatException
	const cleanParams = {};
	Object.keys(params).forEach(key => {
		if (params[key] !== null && params[key] !== undefined) {
			cleanParams[key] = params[key];
		}
	});
	try {
		// 优先走 ES 搜索（ES 分页从 0 开始，前端从 1 开始，需转换）
		const esParams = { ...cleanParams, pageNum: cleanParams.pageNum - 1 };
		return await request({
			method: 'GET',
			url: ES_SEARCH_BASE_URL + '/esProduct/search',
			params: esParams
		});
	} catch (e) {
		// ES 不可用，降级到 MySQL 搜索（MySQL 分页从 1 开始，直接用原参数）
		console.warn('ES search failed, fallback to MySQL:', e);
		return await request({
			method: 'GET',
			url: '/product/search',
			params: cleanParams
		});
	}
}

export function fetchCategoryTreeList() {
	return request({
		method: 'GET',
		url: '/product/categoryTreeList'
	})
}

export function fetchProductDetail(id) {
	return request({
		method: 'GET',
		url: '/product/detail/'+id
	})
}
