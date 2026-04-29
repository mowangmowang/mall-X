-- ============================================================
-- 清理秒杀功能菜单（适用已有数据库）
-- 执行前请备份数据库
-- ============================================================

-- 删除秒杀菜单（父菜单为"营销" id=12）
DELETE FROM ums_menu WHERE name = '秒杀活动列表';

-- 删除秒杀资源
DELETE FROM ums_resource WHERE name LIKE '%限时购%';

-- 验证：确认已清理
SELECT * FROM ums_menu WHERE name LIKE '%秒杀%';
SELECT * FROM ums_resource WHERE name LIKE '%限时购%';
