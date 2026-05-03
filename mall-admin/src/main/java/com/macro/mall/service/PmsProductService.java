package com.macro.mall.service;

import com.macro.mall.dto.PmsProductParam;
import com.macro.mall.dto.PmsProductQueryParam;
import com.macro.mall.dto.PmsProductResult;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductVerifyRecord;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品管理 Service 接口
 * 定义商品业务逻辑的核心方法，包括商品的增删改查、状态管理及审核流程。
 */
public interface PmsProductService {
    /**
     * 创建新商品
     * 处理商品基本信息、SKU库存、属性值及关联关系的持久化。
     *
     * @param productParam 包含完整商品信息的参数对象
     * @return 新创建的商品 ID
     */
    @Transactional(isolation = Isolation.DEFAULT,propagation = Propagation.REQUIRED)
    Long create(PmsProductParam productParam);

    /**
     * 根据商品 ID 获取用于编辑的详细信息
     * 组装商品的基本信息、SKU列表、属性值等，供前端编辑页面使用。
     *
     * @param id 商品唯一标识符
     * @return 商品编辑结果对象
     */
    PmsProductResult getUpdateInfo(Long id);

    /**
     * 更新现有商品信息
     * 支持对商品各维度信息的修改，并同步更新关联数据。
     *
     * @param id 待更新的商品 ID
     * @param productParam 更新后的商品参数
     * @return 受影响的记录数
     */
    @Transactional
    int update(Long id, PmsProductParam productParam);

    /**
     * 分页查询商品列表
     * 支持根据多种筛选条件（如名称、分类、上架状态等）进行动态查询。
     *
     * @param productQueryParam 查询条件封装对象
     * @param pageSize 每页显示条数
     * @param pageNum 当前页码
     * @return 商品列表
     */
    List<PmsProduct> list(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum);

    /**
     * 批量修改商品审核状态
     * 同时记录审核操作日志到 pms_product_verify_record 表。
     *
     * @param ids 商品 ID 集合
     * @param verifyStatus 目标审核状态 (0:未审核, 1:通过, 2:拒绝)
     * @param detail 审核意见或原因
     * @return 成功修改的记录数
     */
    @Transactional
    int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail);

    /**
     * 批量修改商品上架/下架状态
     *
     * @param ids 商品 ID 集合
     * @param publishStatus 目标发布状态 (0:下架, 1:上架)
     * @return 成功修改的记录数
     */
    int updatePublishStatus(List<Long> ids, Integer publishStatus);

    /**
     * 批量修改商品推荐状态
     *
     * @param ids 商品 ID 集合
     * @param recommendStatus 目标推荐状态 (0:不推荐, 1:推荐)
     * @return 成功修改的记录数
     */
    int updateRecommendStatus(List<Long> ids, Integer recommendStatus);

    /**
     * 批量修改商品新品标签状态
     *
     * @param ids 商品 ID 集合
     * @param newStatus 目标新品状态 (0:非新品, 1:新品)
     * @return 成功修改的记录数
     */
    int updateNewStatus(List<Long> ids, Integer newStatus);

    /**
     * 批量修改商品逻辑删除状态
     *
     * @param ids 商品 ID 集合
     * @param deleteStatus 目标删除状态 (0:未删除, 1:已删除)
     * @return 成功修改的记录数
     */
    int updateDeleteStatus(List<Long> ids, Integer deleteStatus);

    /**
     * 根据关键词模糊查询商品
     * 支持按商品名称或货号 (productSn) 进行搜索。
     *
     * @param keyword 搜索关键词
     * @return 匹配的商品列表
     */
    List<PmsProduct> list(String keyword);
    
    /**
     * 查询指定商品的审核历史记录
     *
     * @param productId 商品 ID
     * @return 审核记录列表，按时间倒序排列
     */
    List<PmsProductVerifyRecord> getVerifyRecordList(Long productId);
}
