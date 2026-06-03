package com.macro.mall.portal.repository;

import com.macro.mall.portal.domain.MemberReadHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 会员商品浏览历史数据访问接口 (Member Read History Repository)
 * <p>
 * 基于Spring Data MongoDB实现，提供会员浏览记录的持久化操作。
 * 继承MongoRepository获得基础的CRUD能力，自定义方法用于特定查询场景。
 */
public interface MemberReadHistoryRepository extends MongoRepository<MemberReadHistory, String> {
    /**
     * 根据会员ID分页查询浏览记录（按创建时间倒序）
     * <p>
     * 用于展示用户的浏览历史，最新的浏览记录排在前面。
     * Spring Data会根据方法名自动生成MongoDB查询：
     * - findByMemberId: 根据memberId字段筛选
     * - OrderByCreateTimeDesc: 按createTime字段降序排列
     *
     * @param memberId 会员唯一标识符 (Member ID)
     * @param pageable 分页参数，包含页码和每页大小
     * @return 分页后的浏览记录列表，按时间从新到旧排序
     */
    Page<MemberReadHistory> findByMemberIdOrderByCreateTimeDesc(Long memberId, Pageable pageable);

    /**
     * 删除指定会员的所有浏览记录
     * <p>
     * 用于清空用户的浏览历史功能。
     * Spring Data会根据方法名自动生成删除操作：
     * - deleteAllByMemberId: 删除所有memberId匹配的记录
     *
     * @param memberId 会员唯一标识符 (Member ID)
     */
    void deleteAllByMemberId(Long memberId);
}
