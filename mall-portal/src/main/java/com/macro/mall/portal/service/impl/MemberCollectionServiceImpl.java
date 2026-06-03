package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberProductCollection;
import com.macro.mall.portal.repository.MemberProductCollectionRepository;
import com.macro.mall.portal.service.MemberCollectionService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 会员商品收藏管理Service实现类 (Member Product Collection Service Implementation)
 * <p>
 * 负责处理用户对商品的收藏操作，数据存储在MongoDB中。
 * 支持从MySQL同步商品基本信息到收藏记录中。
 */
@Service
public class MemberCollectionServiceImpl implements MemberCollectionService {
    /** 是否启用SQL查询获取商品信息 */
    @Value("${mongo.insert.sqlEnable}")
    private Boolean sqlEnable;
    
    /** 商品Mapper，用于查询商品详细信息 */
    @Autowired
    private PmsProductMapper productMapper;
    
    /** MongoDB商品收藏Repository */
    @Autowired
    private MemberProductCollectionRepository productCollectionRepository;
    
    /** 会员服务，用于获取当前登录用户信息 */
    @Autowired
    private UmsMemberService memberService;

    /**
     * 添加商品收藏
     * <p>
     * 1. 验证商品ID有效性
     * 2. 检查是否已收藏该商品（避免重复收藏）
     * 3. 获取当前登录用户信息并设置到记录中
     * 4. 根据配置决定是否从MySQL查询商品详情
     * 5. 保存收藏记录到MongoDB
     *
     * @param productCollection 商品收藏对象，需包含商品ID
     * @return 1表示收藏成功，0表示收藏失败（商品不存在、已删除或已收藏）
     */
    @Override
    public int add(MemberProductCollection productCollection) {
        int count = 0;
        // 验证商品ID是否为空
        if (productCollection.getProductId() == null) {
            return 0;
        }
        
        // 获取当前登录会员信息
        UmsMember member = memberService.getCurrentMember();
        productCollection.setMemberId(member.getId());
        productCollection.setMemberNickname(member.getNickname());
        productCollection.setMemberIcon(member.getIcon());
        
        // 检查是否已经收藏过该商品
        MemberProductCollection findCollection = productCollectionRepository.findByMemberIdAndProductId(productCollection.getMemberId(), productCollection.getProductId());
        if (findCollection == null) {
            // 如果启用SQL查询，则从MySQL获取商品详细信息
            if (sqlEnable) {
                PmsProduct product = productMapper.selectByPrimaryKey(productCollection.getProductId());
                // 商品不存在或已删除，不创建收藏记录
                if (product == null || product.getDeleteStatus() == 1) {
                    return 0;
                }
                // 设置商品基本信息
                productCollection.setProductName(product.getName());
                productCollection.setProductSubTitle(product.getSubTitle());
                productCollection.setProductPrice(product.getPrice() + "");
                productCollection.setProductPic(product.getPic());
            }
            // 保存收藏记录到MongoDB
            productCollectionRepository.save(productCollection);
            count = 1;
        }
        return count;
    }

    /**
     * 取消商品收藏
     * <p>
     * 根据商品ID删除当前用户的收藏记录
     *
     * @param productId 商品唯一标识符 (Product ID)
     * @return 删除的记录数量
     */
    @Override
    public int delete(Long productId) {
        UmsMember member = memberService.getCurrentMember();
        return productCollectionRepository.deleteByMemberIdAndProductId(member.getId(), productId);
    }

    /**
     * 分页查询收藏列表
     * <p>
     * 查询当前登录用户收藏的商品列表
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页后的商品收藏列表
     */
    @Override
    public Page<MemberProductCollection> list(Integer pageNum, Integer pageSize) {
        UmsMember member = memberService.getCurrentMember();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return productCollectionRepository.findByMemberId(member.getId(), pageable);
    }

    /**
     * 查询收藏详情
     * <p>
     * 查询当前用户对指定商品的收藏记录
     *
     * @param productId 商品唯一标识符 (Product ID)
     * @return 商品收藏详情，未收藏则返回null
     */
    @Override
    public MemberProductCollection detail(Long productId) {
        UmsMember member = memberService.getCurrentMember();
        return productCollectionRepository.findByMemberIdAndProductId(member.getId(), productId);
    }

    /**
     * 清空收藏列表
     * <p>
     * 删除当前登录用户的所有商品收藏记录
     */
    @Override
    public void clear() {
        UmsMember member = memberService.getCurrentMember();
        productCollectionRepository.deleteAllByMemberId(member.getId());
    }
}
