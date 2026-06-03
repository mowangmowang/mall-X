package com.macro.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.macro.mall.mapper.OmsCartItemMapper;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.OmsCartItemExample;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.dao.PortalProductDao;
import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.OmsPromotionService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车管理Service实现类 (Shopping Cart Service Implementation)
 * <p>
 * 负责处理用户购物车的增删改查操作，支持促销计算和库存检查。
 * 购物车数据存储在MySQL中，通过逻辑删除标记实现软删除。
 */
@Service
public class OmsCartItemServiceImpl implements OmsCartItemService {
    /** 购物车Mapper，用于购物车数据的持久化操作 */
    @Autowired
    private OmsCartItemMapper cartItemMapper;
    
    /** 商品DAO，用于查询商品详细信息 */
    @Autowired
    private PortalProductDao productDao;
    
    /** 促销服务，用于计算购物车商品的促销优惠 */
    @Autowired
    private OmsPromotionService promotionService;
    
    /** 会员服务，用于获取当前登录用户信息 */
    @Autowired
    private UmsMemberService memberService;

    /**
     * 添加商品到购物车
     * <p>
     * 1. 获取当前登录用户信息
     * 2. 检查购物车中是否已存在相同商品（同SKU）
     * 3. 如果存在则更新数量，否则新增记录
     *
     * @param cartItem 购物车项对象，需包含商品ID、SKU ID和数量
     * @return 影响行数，1表示成功
     */
    @Override
    public int add(OmsCartItem cartItem) {
        int count;
        // 获取当前登录会员信息
        UmsMember currentMember =memberService.getCurrentMember();
        cartItem.setMemberId(currentMember.getId());
        // 当 nickname 为 NULL 时，使用 username 作为 fallback
        String nickname = currentMember.getNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = currentMember.getUsername();
        }
        cartItem.setMemberNickname(nickname);
        cartItem.setDeleteStatus(0);
        
        // 检查购物车中是否已存在相同商品
        OmsCartItem existCartItem = getCartItem(cartItem);
        if (existCartItem == null) {
            // 不存在则新增
            cartItem.setCreateDate(new Date());
            count = cartItemMapper.insert(cartItem);
        } else {
            // 存在则累加数量
            cartItem.setModifyDate(new Date());
            existCartItem.setQuantity(existCartItem.getQuantity() + cartItem.getQuantity());
            count = cartItemMapper.updateByPrimaryKey(existCartItem);
        }
        return count;
    }

    /**
     * 根据会员ID、商品ID和规格获取购物车中商品
     * <p>
     * 用于判断购物车中是否已存在相同的商品项（同一SKU）
     *
     * @param cartItem 查询条件，包含会员ID、商品ID和SKU ID
     * @return 存在的购物车项，不存在则返回null
     */
    private OmsCartItem getCartItem(OmsCartItem cartItem) {
        OmsCartItemExample example = new OmsCartItemExample();
        OmsCartItemExample.Criteria criteria = example.createCriteria().andMemberIdEqualTo(cartItem.getMemberId())
                .andProductIdEqualTo(cartItem.getProductId()).andDeleteStatusEqualTo(0);
        if (cartItem.getProductSkuId()!=null) {
            criteria.andProductSkuIdEqualTo(cartItem.getProductSkuId());
        }
        List<OmsCartItem> cartItemList = cartItemMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(cartItemList)) {
            return cartItemList.get(0);
        }
        return null;
    }

    /**
     * 查询用户的购物车列表
     * <p>
     * 获取指定用户的所有未删除购物车项
     *
     * @param memberId 会员唯一标识符 (Member ID)
     * @return 购物车项列表
     */
    @Override
    public List<OmsCartItem> list(Long memberId) {
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andMemberIdEqualTo(memberId);
        return cartItemMapper.selectByExample(example);
    }

    /**
     * 查询购物车商品的促销信息
     * <p>
     * 根据购物车ID列表计算每个商品的促销优惠价格
     *
     * @param memberId 会员唯一标识符 (Member ID)
     * @param cartIds 购物车项ID列表，为空则查询全部
     * @return 包含促销信息的购物车项列表
     */
    @Override
    public List<CartPromotionItem> listPromotion(Long memberId, List<Long> cartIds) {
        List<OmsCartItem> cartItemList = list(memberId);
        if(CollUtil.isNotEmpty(cartIds)){
            cartItemList = cartItemList.stream().filter(item->cartIds.contains(item.getId())).collect(Collectors.toList());
        }
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(cartItemList)){
            cartPromotionItemList = promotionService.calcCartPromotion(cartItemList);
        }
        return cartPromotionItemList;
    }

    /**
     * 更新购物车商品数量
     * <p>
     * 修改指定购物车项的购买数量
     *
     * @param id 购物车项ID
     * @param memberId 会员ID（用于权限校验）
     * @param quantity 新的购买数量
     * @return 影响行数
     */
    @Override
    public int updateQuantity(Long id, Long memberId, Integer quantity) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setQuantity(quantity);
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andDeleteStatusEqualTo(0)
                .andIdEqualTo(id).andMemberIdEqualTo(memberId);
        return cartItemMapper.updateByExampleSelective(cartItem, example);
    }

    /**
     * 批量删除购物车商品
     * <p>
     * 采用逻辑删除方式，将deleteStatus设置为1
     *
     * @param memberId 会员ID（用于权限校验）
     * @param ids 购物车项ID列表
     * @return 影响行数
     */
    @Override
    public int delete(Long memberId, List<Long> ids) {
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andIdIn(ids).andMemberIdEqualTo(memberId);
        return cartItemMapper.updateByExampleSelective(record, example);
    }

    /**
     * 获取购物车商品详情
     * <p>
     * 查询商品的完整信息，包括SKU、属性等，用于购物车页面展示
     *
     * @param productId 商品唯一标识符 (Product ID)
     * @return 购物车商品详情对象
     */
    @Override
    public CartProduct getCartProduct(Long productId) {
        return productDao.getCartProduct(productId);
    }

    /**
     * 修改购物车商品规格
     * <p>
     * 先删除原购物车项，再创建新的购物车项（因为SKU变化）
     *
     * @param cartItem 新的购物车项信息
     * @return 1表示成功
     */
    @Override
    public int updateAttr(OmsCartItem cartItem) {
        // 删除原购物车信息（逻辑删除）
        OmsCartItem updateCart = new OmsCartItem();
        updateCart.setId(cartItem.getId());
        updateCart.setModifyDate(new Date());
        updateCart.setDeleteStatus(1);
        cartItemMapper.updateByPrimaryKeySelective(updateCart);
        // 创建新的购物车项
        cartItem.setId(null);
        add(cartItem);
        return 1;
    }

    /**
     * 清空购物车
     * <p>
     * 逻辑删除指定用户的所有购物车项
     *
     * @param memberId 会员唯一标识符 (Member ID)
     * @return 影响行数
     */
    @Override
    public int clear(Long memberId) {
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andMemberIdEqualTo(memberId);
        return cartItemMapper.updateByExampleSelective(record,example);
    }
}
