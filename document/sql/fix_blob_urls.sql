-- ===================================
-- 修复错误的 blob URL 图片地址
-- ===================================
-- 问题：前端上传组件BUG导致blob URL被保存到数据库
-- 解决：清空错误的blob URL，需要重新上传图片
use mall;

-- 1. 查找所有使用 blob URL 的商品
SELECT id, name, pic, '需要重新上传图片' AS action 
FROM pms_product 
WHERE pic LIKE 'blob:%';

-- 2. 清空错误的 blob URL（设置为NULL或空字符串）
UPDATE pms_product 
SET pic = NULL
WHERE pic LIKE 'blob:%';

-- 3. 清空错误的 blob URL 的相册图片
UPDATE pms_product 
SET album_pics = NULL
WHERE album_pics LIKE 'blob:%';

-- 4. 验证修复结果
SELECT '修复完成' AS status, 
       (SELECT COUNT(*) FROM pms_product WHERE pic LIKE 'blob:%') AS remaining_blob_urls;
