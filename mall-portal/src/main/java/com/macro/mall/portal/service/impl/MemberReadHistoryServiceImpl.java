package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberReadHistory;
import com.macro.mall.portal.repository.MemberReadHistoryRepository;
import com.macro.mall.portal.service.MemberReadHistoryService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会员浏览记录管理Service实现类
 * <p>
 * 负责处理用户商品浏览记录的增删查操作，数据存储在MongoDB中。
 * 支持从MySQL同步商品基本信息到浏览记录中。
 */
@Service
public class MemberReadHistoryServiceImpl implements MemberReadHistoryService {

    /** 是否启用SQL查询获取商品信息 */
    @Value("${mongo.insert.sqlEnable}")
    private Boolean sqlEnable;
    
    /** 商品Mapper，用于查询商品详细信息 */
    @Autowired
    private PmsProductMapper productMapper;
    
    /** MongoDB浏览记录Repository */
    @Autowired
    private MemberReadHistoryRepository memberReadHistoryRepository;
    
    /** 会员服务，用于获取当前登录用户信息 */
    @Autowired
    private UmsMemberService memberService;
    /**
     * 创建浏览记录
     * <p>
     * 1. 验证商品ID有效性
     * 2. 获取当前登录用户信息并设置到记录中
     * 3. 根据配置决定是否从MySQL查询商品详情
     * 4. 保存浏览记录到MongoDB
     *
     * @param memberReadHistory 浏览记录对象，需包含商品ID
     * @return 1表示创建成功，0表示创建失败（商品不存在或已删除）
     */
    @Override
    public int create(MemberReadHistory memberReadHistory) {
        // 验证商品ID是否为空
        if (memberReadHistory.getProductId() == null) {
            return 0;
        }
        
        // 获取当前登录会员信息
        UmsMember member = memberService.getCurrentMember();
        memberReadHistory.setMemberId(member.getId());
        memberReadHistory.setMemberNickname(member.getNickname());
        memberReadHistory.setMemberIcon(member.getIcon());
        memberReadHistory.setId(null);
        memberReadHistory.setCreateTime(new Date());
        
        // 如果启用SQL查询，则从MySQL获取商品详细信息
        if (sqlEnable) {
            PmsProduct product = productMapper.selectByPrimaryKey(memberReadHistory.getProductId());
            // 商品不存在或已删除，不创建浏览记录
            if (product == null || product.getDeleteStatus() == 1) {
                return 0;
            }
            // 设置商品基本信息
            memberReadHistory.setProductName(product.getName());
            memberReadHistory.setProductSubTitle(product.getSubTitle());
            memberReadHistory.setProductPrice(product.getPrice() + "");
            memberReadHistory.setProductPic(product.getPic());
        }
        
        // 保存浏览记录到MongoDB
        memberReadHistoryRepository.save(memberReadHistory);
        return 1;
    }

    /**
     * 批量删除浏览记录
     * <p>
     * 根据ID列表批量删除用户的浏览历史记录
     *
     * @param ids 浏览记录ID列表
     * @return 删除的记录数量
     */
    @Override
    public int delete(List<String> ids) {
        // 构建待删除的浏览记录对象列表
        List<MemberReadHistory> deleteList = new ArrayList<>();
        for(String id:ids){
            MemberReadHistory memberReadHistory = new MemberReadHistory();
            memberReadHistory.setId(id);
            deleteList.add(memberReadHistory);
        }
        // 执行批量删除操作
        memberReadHistoryRepository.deleteAll(deleteList);
        return ids.size();
    }

    /**
     * 分页查询浏览记录
     * <p>
     * 查询当前登录用户的浏览历史，按创建时间倒序排列
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页后的浏览记录列表
     */
    @Override
    public Page<MemberReadHistory> list(Integer pageNum, Integer pageSize) {
        // 获取当前登录会员
        UmsMember member = memberService.getCurrentMember();
        // 构建分页参数（Spring Data页码从0开始）
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        // 按创建时间倒序查询该用户的浏览记录
        return memberReadHistoryRepository.findByMemberIdOrderByCreateTimeDesc(member.getId(),pageable);
    }

    /**
     * 清空浏览记录
     * <p>
     * 删除当前登录用户的所有浏览历史记录
     */
    @Override
    public void clear() {
        // 获取当前登录会员
        UmsMember member = memberService.getCurrentMember();
        // 删除该用户的所有浏览记录
        memberReadHistoryRepository.deleteAllByMemberId(member.getId());
    }
}
