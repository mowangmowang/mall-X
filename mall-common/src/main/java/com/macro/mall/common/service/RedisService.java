package com.macro.mall.common.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 操作服务接口 (Redis Service Interface)
 * 封装常用的 Redis 数据结构操作，包括 String、Hash、Set、List
 */
public interface RedisService {

    /**
     * 保存属性（带过期时间）
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间（秒）
     */
    void set(String key, Object value, long time);

    /**
     * 保存属性（永久有效）
     *
     * @param key   键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 获取属性
     *
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * 删除单个属性
     *
     * @param key 键
     * @return 是否删除成功
     */
    Boolean del(String key);

    /**
     * 批量删除属性
     *
     * @param keys 键列表
     * @return 删除的个数
     */
    Long del(List<String> keys);

    /**
     * 设置过期时间
     *
     * @param key  键
     * @param time 过期时间（秒）
     * @return 是否设置成功
     */
    Boolean expire(String key, long time);

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 剩余过期时间（秒），-1 表示永久有效，-2 表示键不存在
     */
    Long getExpire(String key);

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 按 delta 递增（原子操作）
     *
     * @param key   键
     * @param delta 增量
     * @return 递增后的值
     */
    Long incr(String key, long delta);

    /**
     * 按 delta 递减（原子操作）
     *
     * @param key   键
     * @param delta 减量
     * @return 递减后的值
     */
    Long decr(String key, long delta);

    /**
     * 获取 Hash 结构中的属性
     *
     * @param key     键
     * @param hashKey 哈希键
     * @return 值
     */
    Object hGet(String key, String hashKey);

    /**
     * 向 Hash 结构中放入一个属性（带过期时间）
     *
     * @param key     键
     * @param hashKey 哈希键
     * @param value   值
     * @param time    过期时间（秒）
     * @return 是否设置成功
     */
    Boolean hSet(String key, String hashKey, Object value, long time);

    /**
     * 向 Hash 结构中放入一个属性（永久有效）
     *
     * @param key     键
     * @param hashKey 哈希键
     * @param value   值
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * 直接获取整个 Hash 结构
     *
     * @param key 键
     * @return Map<Object, Object>
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * 直接设置整个 Hash 结构（带过期时间）
     *
     * @param key  键
     * @param map  哈希表
     * @param time 过期时间（秒）
     * @return 是否设置成功
     */
    Boolean hSetAll(String key, Map<String, Object> map, long time);

    /**
     * 直接设置整个 Hash 结构（永久有效）
     *
     * @param key 键
     * @param map 哈希表
     */
    void hSetAll(String key, Map<String, ?> map);

    /**
     * 删除 Hash 结构中的属性
     *
     * @param key      键
     * @param hashKeys 哈希键列表
     */
    void hDel(String key, Object... hashKey);

    /**
     * 判断 Hash 结构中是否有该属性
     *
     * @param key     键
     * @param hashKey 哈希键
     * @return 是否存在
     */
    Boolean hHasKey(String key, String hashKey);

    /**
     * Hash 结构中属性递增
     *
     * @param key     键
     * @param hashKey 哈希键
     * @param delta   增量
     * @return 递增后的值
     */
    Long hIncr(String key, String hashKey, Long delta);

    /**
     * Hash 结构中属性递减
     *
     * @param key     键
     * @param hashKey 哈希键
     * @param delta   减量
     * @return 递减后的值
     */
    Long hDecr(String key, String hashKey, Long delta);

    /**
     * 获取 Set 结构中的所有成员
     *
     * @param key 键
     * @return 集合成员
     */
    Set<Object> sMembers(String key);

    /**
     * 向 Set 结构中添加属性（永久有效）
     *
     * @param key    键
     * @param values 值列表
     * @return 添加成功的个数
     */
    Long sAdd(String key, Object... values);

    /**
     * 向 Set 结构中添加属性（带过期时间）
     *
     * @param key    键
     * @param time   过期时间（秒）
     * @param values 值列表
     * @return 添加成功的个数
     */
    Long sAdd(String key, long time, Object... values);

    /**
     * 判断是否为 Set 中的属性
     *
     * @param key   键
     * @param value 值
     * @return 是否存在
     */
    Boolean sIsMember(String key, Object value);

    /**
     * 获取 Set 结构的长度
     *
     * @param key 键
     * @return 集合大小
     */
    Long sSize(String key);

    /**
     * 删除 Set 结构中的属性
     *
     * @param key    键
     * @param values 值列表
     * @return 删除的个数
     */
    Long sRemove(String key, Object... values);

    /**
     * 获取 List 结构中的属性（范围查询）
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     * @return 列表片段
     */
    List<Object> lRange(String key, long start, long end);

    /**
     * 获取 List 结构的长度
     *
     * @param key 键
     * @return 列表大小
     */
    Long lSize(String key);

    /**
     * 根据索引获取 List 中的属性
     *
     * @param key   键
     * @param index 索引
     * @return 指定位置的值
     */
    Object lIndex(String key, long index);

    /**
     * 向 List 结构右侧添加属性（永久有效）
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    Long lPush(String key, Object value);

    /**
     * 向 List 结构右侧添加属性（带过期时间）
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间（秒）
     * @return 列表长度
     */
    Long lPush(String key, Object value, long time);

    /**
     * 向 List 结构中批量添加属性（永久有效）
     *
     * @param key    键
     * @param values 值列表
     * @return 列表长度
     */
    Long lPushAll(String key, Object... values);

    /**
     * 向 List 结构中批量添加属性（带过期时间）
     *
     * @param key    键
     * @param time   过期时间（秒）
     * @param values 值列表
     * @return 列表长度
     */
    Long lPushAll(String key, Long time, Object... values);

    /**
     * 从 List 结构中移除属性
     *
     * @param key   键
     * @param count 移除个数（0: 所有, >0: 从头到尾, <0: 从尾到头）
     * @param value 值
     * @return 移除的个数
     */
    Long lRemove(String key, long count, Object value);
}