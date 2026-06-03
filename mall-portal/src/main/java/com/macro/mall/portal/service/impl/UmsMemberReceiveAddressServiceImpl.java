package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.UmsMemberReceiveAddressMapper;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.UmsMemberReceiveAddress;
import com.macro.mall.model.UmsMemberReceiveAddressExample;
import com.macro.mall.portal.service.UmsMemberReceiveAddressService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 会员收货地址管理Service实现类 (Member Receive Address Service Implementation)
 * <p>
 * 负责处理用户收货地址的增删改查操作，支持默认地址设置。
 */
@Service
public class UmsMemberReceiveAddressServiceImpl implements UmsMemberReceiveAddressService {
    /** 会员服务，用于获取当前登录用户信息 */
    @Autowired
    private UmsMemberService memberService;
    
    /** 收货地址Mapper，用于地址数据的持久化操作 */
    @Autowired
    private UmsMemberReceiveAddressMapper addressMapper;
    /**
     * 添加收货地址
     * <p>
     * 为当前登录用户添加新的收货地址
     *
     * @param address 收货地址对象
     * @return 影响行数
     */
    @Override
    public int add(UmsMemberReceiveAddress address) {
        UmsMember currentMember = memberService.getCurrentMember();
        address.setMemberId(currentMember.getId());
        return addressMapper.insert(address);
    }

    /**
     * 删除收货地址
     * <p>
     * 删除指定ID的收货地址（需验证归属权）
     *
     * @param id 地址唯一标识符 (Address ID)
     * @return 影响行数
     */
    @Override
    public int delete(Long id) {
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddressExample example = new UmsMemberReceiveAddressExample();
        example.createCriteria().andMemberIdEqualTo(currentMember.getId()).andIdEqualTo(id);
        return addressMapper.deleteByExample(example);
    }

    /**
     * 更新收货地址
     * <p>
     * 修改指定地址的信息，如果设置为默认地址则取消其他地址的默认状态
     *
     * @param id 地址唯一标识符 (Address ID)
     * @param address 新的地址信息
     * @return 影响行数
     */
    @Override
    public int update(Long id, UmsMemberReceiveAddress address) {
        address.setId(null);
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddressExample example = new UmsMemberReceiveAddressExample();
        example.createCriteria().andMemberIdEqualTo(currentMember.getId()).andIdEqualTo(id);
        if(address.getDefaultStatus()==null){
            address.setDefaultStatus(0);
        }
        if(address.getDefaultStatus()==1){
                // 先将原来的默认地址去除
                UmsMemberReceiveAddress record= new UmsMemberReceiveAddress();
            record.setDefaultStatus(0);
            UmsMemberReceiveAddressExample updateExample = new UmsMemberReceiveAddressExample();
            updateExample.createCriteria()
                    .andMemberIdEqualTo(currentMember.getId())
                    .andDefaultStatusEqualTo(1);
            addressMapper.updateByExampleSelective(record,updateExample);
        }
        return addressMapper.updateByExampleSelective(address,example);
    }

    /**
     * 查询收货地址列表
     * <p>
     * 获取当前登录用户的所有收货地址
     *
     * @return 收货地址列表
     */
    @Override
    public List<UmsMemberReceiveAddress> list() {
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddressExample example = new UmsMemberReceiveAddressExample();
        example.createCriteria().andMemberIdEqualTo(currentMember.getId());
        return addressMapper.selectByExample(example);
    }

    /**
     * 获取收货地址详情
     * <p>
     * 根据ID获取指定地址的详细信息（需验证归属权）
     *
     * @param id 地址唯一标识符 (Address ID)
     * @return 收货地址对象，不存在或无权限则返回null
     */
    @Override
    public UmsMemberReceiveAddress getItem(Long id) {
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddressExample example = new UmsMemberReceiveAddressExample();
        example.createCriteria().andMemberIdEqualTo(currentMember.getId()).andIdEqualTo(id);
        List<UmsMemberReceiveAddress> addressList = addressMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(addressList)){
            return addressList.get(0);
        }
        return null;
    }
}
