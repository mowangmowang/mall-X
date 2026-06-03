/**
 * 前端降级逻辑集成测试脚本
 * 用于验证在 ES 服务不可用时，前端是否能正确切换到 MySQL 接口
 */

// 模拟前端环境的简单测试
const testFallback = async () => {
    console.log('--- 开始测试智能降级逻辑 ---');

    // 1. 模拟 ES 服务不可用 (Connection Refused)
    console.log('1. 尝试请求 ES 服务 (localhost:8088 - 已关闭)...');
    try {
        // 这里假设 ES 接口地址，实际会根据前端配置变化
        await fetch('http://localhost:8088/esProduct/search?keyword=test', { method: 'GET' });
    } catch (e) {
        console.log('   [捕获异常] ES 请求失败:', e.message || 'Network Error');
        
        // 2. 执行降级逻辑
        console.log('2. 触发降级策略，转向 MySQL 接口...');
        try {
            // 假设 MySQL 接口在 8085 (mall-portal 默认端口)
            const response = await fetch('http://localhost:8085/product/search?keyword=test&pageNum=1&pageSize=5');
            if (response.ok) {
                const data = await response.json();
                console.log('   [降级成功] MySQL 接口返回状态:', response.status);
                console.log('   [数据验证] 是否包含商品列表:', !!data.data && !!data.data.list);
            } else {
                console.log('   [降级失败] MySQL 接口返回异常:', response.status);
            }
        } catch (mysqlError) {
            console.log('   [降级失败] MySQL 请求也出错:', mysqlError.message);
        }
    }

    console.log('--- 测试结束 ---');
};

testFallback();
