package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.PmsBrandMapper;
import com.macro.mall.model.PmsBrand;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberBrandAttention;
import com.macro.mall.portal.repository.MemberBrandAttentionRepository;
import com.macro.mall.portal.service.MemberAttentionService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 会员品牌关注管理Service实现类 (Member Brand Attention Service Implementation)
 * <p>
 * 负责处理用户对品牌的关注操作，数据存储在MongoDB中。
 * 支持从MySQL同步品牌基本信息到关注记录中。
 */
@Service
public class MemberAttentionServiceImpl implements MemberAttentionService {
    /** 是否启用SQL查询获取品牌信息 */
    @Value("${mongo.insert.sqlEnable}")
    private Boolean sqlEnable;
    
    /** 品牌Mapper，用于查询品牌详细信息 */
    @Autowired
    private PmsBrandMapper brandMapper;
    
    /** MongoDB品牌关注Repository */
    @Autowired
    private MemberBrandAttentionRepository memberBrandAttentionRepository;
    
    /** 会员服务，用于获取当前登录用户信息 */
    @Autowired
    private UmsMemberService memberService;

    /**
     * 添加品牌关注
     * <p>
     * 1. 验证品牌ID有效性
     * 2. 检查是否已关注该品牌（避免重复关注）
     * 3. 获取当前登录用户信息并设置到记录中
     * 4. 根据配置决定是否从MySQL查询品牌详情
     * 5. 保存关注记录到MongoDB
     *
     * @param memberBrandAttention 品牌关注对象，需包含品牌ID
     * @return 1表示关注成功，0表示关注失败（品牌不存在或已关注）
     */
    @Override
    public int add(MemberBrandAttention memberBrandAttention) {
        int count = 0;
        // 验证品牌ID是否为空
        if(memberBrandAttention.getBrandId()==null){
            return 0;
        }
        
        // 获取当前登录会员信息
        UmsMember member = memberService.getCurrentMember();
        memberBrandAttention.setMemberId(member.getId());
        memberBrandAttention.setMemberNickname(member.getNickname());
        memberBrandAttention.setMemberIcon(member.getIcon());
        memberBrandAttention.setCreateTime(new Date());
        
        // 检查是否已经关注过该品牌
        MemberBrandAttention findAttention = memberBrandAttentionRepository.findByMemberIdAndBrandId(memberBrandAttention.getMemberId(), memberBrandAttention.getBrandId());
        if (findAttention == null) {
            // 如果启用SQL查询，则从MySQL获取品牌详细信息
            if(sqlEnable){
                PmsBrand brand = brandMapper.selectByPrimaryKey(memberBrandAttention.getBrandId());
                if(brand==null){
                    return 0;
                }else{
                    memberBrandAttention.setBrandCity(null);
                    memberBrandAttention.setBrandName(brand.getName());
                    memberBrandAttention.setBrandLogo(brand.getLogo());
                }
            }
            // 保存关注记录到MongoDB
            memberBrandAttentionRepository.save(memberBrandAttention);
            count = 1;
        }
        return count;
    }

    /**
     * 取消品牌关注
     * <p>
     * 根据品牌ID删除当前用户的关注记录
     *
     * @param brandId 品牌唯一标识符 (Brand ID)
     * @return 删除的记录数量
     */
    @Override
    public int delete(Long brandId) {
        UmsMember member = memberService.getCurrentMember();
        return memberBrandAttentionRepository.deleteByMemberIdAndBrandId(member.getId(),brandId);
    }

    /**
     * 分页查询关注列表
     * <p>
     * 查询当前登录用户关注的品牌列表
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页后的品牌关注列表
     */
    @Override
    public Page<MemberBrandAttention> list(Integer pageNum, Integer pageSize) {
        UmsMember member = memberService.getCurrentMember();
        Pageable pageable = PageRequest.of(pageNum-1,pageSize);
        return memberBrandAttentionRepository.findByMemberId(member.getId(),pageable);
    }

    /**
     * 查询关注详情
     * <p>
     * 查询当前用户对指定品牌的关注记录
     *
     * @param brandId 品牌唯一标识符 (Brand ID)
     * @return 品牌关注详情，未关注则返回null
     */
    @Override
    public MemberBrandAttention detail(Long brandId) {
        UmsMember member = memberService.getCurrentMember();
        return memberBrandAttentionRepository.findByMemberIdAndBrandId(member.getId(), brandId);
    }

    /**
     * 清空关注列表
     * <p>
     * 删除当前登录用户的所有品牌关注记录
     */
    @Override
    public void clear() {
        UmsMember member = memberService.getCurrentMember();
        memberBrandAttentionRepository.deleteAllByMemberId(member.getId());
    }
}
