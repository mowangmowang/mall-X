import request from '@/utils/requestUtil'
import { ES_SEARCH_BASE_URL } from '@/utils/appConfig.js'

// 强制打印日志确认文件已加载
console.log('🚀 product.js 模块已加载 (版本: 2024-05-01-Fallback-Fix)');

/**
 * 搜索商品列表 — 智能路由
 * 优先请求 mall-search (Elasticsearch)，失败时自动降级到 mall-portal (MySQL)
 */
export async function searchProductList(params) {
	console.log('🔍 searchProductList 被调用，参数:', params);
	
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
		console.log('👉 尝试请求 ES:', ES_SEARCH_BASE_URL + '/esProduct/search', esParams);
		return await request({
			method: 'GET',
			url: ES_SEARCH_BASE_URL + '/esProduct/search',
			params: esParams
		});
	} catch (e) {
		// 捕获到异常，打印详细信息
		console.error('❌ ES 请求失败，捕获到异常:', e);
		
		// 由于 luch-request + Babel 转译的问题，e 可能是一个 Promise 对象
		// 这里我们直接触发降级，不再依赖 e 的属性判断
		// 因为在这个场景下，只要 ES 请求失败，就应该降级到 MySQL
		console.warn('🔄 触发降级策略 -> 请求 MySQL 接口');
		try {
			return await request({
				method: 'GET',
				url: '/product/search',
				params: cleanParams
			});
		} catch (mysqlError) {
			console.error('💀 降级也失败了 (MySQL Error):', mysqlError);
			throw mysqlError; // 抛出给页面处理
		}
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
