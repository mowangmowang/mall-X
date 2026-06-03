-- ===================================
-- 修复 MinIO 端口配置问题
-- ===================================
-- 问题：本地 MinIO 运行在 9001 端口
--      但数据库中图片 URL 使用错误端口
-- 解决：批量更新数据库中的图片 URL 为 9001
use mall;

-- 1. 更新商品主图（处理所有可能的错误端口）
UPDATE pms_product 
SET pic = REPLACE(pic, 'localhost:9000', 'localhost:9001')
WHERE pic LIKE '%localhost:9000%';

UPDATE pms_product 
SET pic = REPLACE(pic, 'localhost:9091', 'localhost:9001')
WHERE pic LIKE '%localhost:9091%';

-- 2. 更新商品相册图（album_pics 字段存储逗号分隔的多张图片）
UPDATE pms_product 
SET album_pics = REPLACE(album_pics, 'localhost:9000', 'localhost:9001')
WHERE album_pics LIKE '%localhost:9000%';

UPDATE pms_product 
SET album_pics = REPLACE(album_pics, 'localhost:9091', 'localhost:9001')
WHERE album_pics LIKE '%localhost:9091%';

-- 3. 更新专题封面图
UPDATE cms_subject 
SET pic = REPLACE(pic, 'localhost:9000', 'localhost:9001')
WHERE pic LIKE '%localhost:9000%';

UPDATE cms_subject 
SET pic = REPLACE(pic, 'localhost:9091', 'localhost:9001')
WHERE pic LIKE '%localhost:9091%';

-- 4. 验证更新结果
SELECT '商品主图' AS type, COUNT(*) AS count FROM pms_product WHERE pic LIKE '%localhost:9001%'
UNION ALL
SELECT '商品相册图', COUNT(*) FROM pms_product WHERE album_pics LIKE '%localhost:9001%'
UNION ALL
SELECT '专题封面图', COUNT(*) FROM cms_subject WHERE pic LIKE '%localhost:9001%';

