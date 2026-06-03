/**
 * 错误分类与处理工具 (Error Handler Utility)
 * 用于区分网络错误、服务器错误和业务错误，辅助前端实现精准的降级策略
 */

/**
 * 判断是否为网络层错误或服务器不可用错误
 * @param {Object} error - 请求捕获的异常对象
 * @returns {boolean} - 若为网络/服务器故障返回 true，否则返回 false
 */
export function shouldFallback(error) {
	// 1. 检查是否是网络连接问题（如超时、断网）
	if (error.errMsg && (error.errMsg.includes('timeout') || error.errMsg.includes('fail'))) {
		return true;
	}

	// 2. 检查 HTTP 状态码：5xx 系列通常代表服务器内部错误或网关错误，适合降级
	if (error.statusCode && error.statusCode >= 500) {
		return true;
	}

	// 3. 针对 luch-request 或类似库的错误结构检查
	if (error.message && (error.message.includes('Network Error') || error.message.includes('timeout'))) {
		return true;
	}

	// 4. 其他情况（如 400, 404, 业务 code != 200）视为业务错误，不触发降级
	return false;
}

/**
 * 获取友好的错误提示信息
 * @param {Object} error - 请求捕获的异常对象
 * @returns {string} - 错误提示文案
 */
export function getErrorMessage(error) {
	if (error.data && error.data.message) {
		return error.data.message;
	}
	if (error.errMsg) {
		return error.errMsg;
	}
	return '搜索服务暂时不可用，请稍后重试';
}
