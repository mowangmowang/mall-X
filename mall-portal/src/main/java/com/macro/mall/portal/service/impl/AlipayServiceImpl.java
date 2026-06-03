package com.macro.mall.portal.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.portal.config.AlipayConfig;
import com.macro.mall.portal.domain.AliPayParam;
import com.macro.mall.portal.service.AlipayService;
import com.macro.mall.portal.service.OmsPortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付宝支付Service实现类 (Alipay Payment Service Implementation)
 * <p>
 * 负责处理支付宝相关的支付操作，包括电脑网站支付、手机网站支付、支付回调和订单查询。
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {
    /** 支付宝配置信息 */
    @Autowired
    private AlipayConfig alipayConfig;
    
    /** 支付宝客户端，用于调用支付宝API */
    @Autowired
    private AlipayClient alipayClient;
    
    /** 订单Mapper，用于订单数据操作 */
    @Autowired
    private OmsOrderMapper orderMapper;
    
    /** 前台订单服务，用于处理支付成功后的业务逻辑 */
    @Autowired
    private OmsPortalOrderService portalOrderService;
    /**
     * 电脑网站支付
     * <p>
     * 生成PC端支付宝支付表单HTML
     *
     * @param aliPayParam 支付参数，包含订单号、金额、标题等
     * @return 支付宝支付表单HTML字符串
     */
    @Override
    public String pay(AliPayParam aliPayParam) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 异步接收地址，公网可访问
        if(StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())){
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        // 同步跳转地址
        if(StrUtil.isNotEmpty(alipayConfig.getReturnUrl())){
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }
        //******必传参数******
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", aliPayParam.getOutTradeNo());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", aliPayParam.getTotalAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", aliPayParam.getSubject());
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());
        String formHtml = null;
        try {
            formHtml = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return formHtml;
    }

    /**
     * 支付回调处理
     * <p>
     * 接收支付宝异步通知，验证签名并更新订单状态
     *
     * @param params 支付宝回调参数
     * @return "success"表示处理成功，"failure"表示处理失败
     */
    @Override
    public String notify(Map<String, String> params) {
        String result = "failure";
        boolean signVerified = false;
        try {
            // 调用SDK验证签名
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付回调签名校验异常！",e);
            e.printStackTrace();
        }
        if (signVerified) {
            String tradeStatus = params.get("trade_status");
            if("TRADE_SUCCESS".equals(tradeStatus)){
                result = "success";
                log.info("notify方法被调用了，tradeStatus:{}",tradeStatus);
                String outTradeNo = params.get("out_trade_no");
                portalOrderService.paySuccessByOrderSn(outTradeNo,1);
            }else{
                log.warn("订单未支付成功，trade_status:{}",tradeStatus);
            }
        } else {
            log.warn("支付回调签名校验失败！");
        }
        return result;
    }

    /**
     * 查询支付宝订单状态
     * <p>
     * 主动查询支付宝侧的订单支付状态
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo 支付宝交易号（与outTradeNo至少传一个）
     * @return 交易状态：WAIT_BUYER_PAY、TRADE_CLOSED、TRADE_SUCCESS、TRADE_FINISHED
     */
    @Override
    public String query(String outTradeNo, String tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //******必传参数******
        JSONObject bizContent = new JSONObject();
        // 设置查询参数，out_trade_no和trade_no至少传一个
        if(StrUtil.isNotEmpty(outTradeNo)){
            bizContent.put("out_trade_no",outTradeNo);
        }
        if(StrUtil.isNotEmpty(tradeNo)){
            bizContent.put("trade_no",tradeNo);
        }
        //交易结算信息: trade_settle_info
        String[] queryOptions = {"trade_settle_info"};
        bizContent.put("query_options", queryOptions);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝账单异常！",e);
        }
        if(response.isSuccess()){
            log.info("查询支付宝账单成功！");
            if("TRADE_SUCCESS".equals(response.getTradeStatus())){
                portalOrderService.paySuccessByOrderSn(outTradeNo,1);
            }
        } else {
            log.error("查询支付宝账单失败！");
        }
        //交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、TRADE_SUCCESS（交易支付成功）、TRADE_FINISHED（交易结束，不可退款）
        return response.getTradeStatus();
    }

    /**
     * 手机网站支付
     * <p>
     * 生成移动端支付宝支付表单HTML
     *
     * @param aliPayParam 支付参数，包含订单号、金额、标题等
     * @return 支付宝支付表单HTML字符串
     */
    @Override
    public String webPay(AliPayParam aliPayParam) {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest ();
        // 异步接收地址，公网可访问
        if(StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())){
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        // 同步跳转地址
        if(StrUtil.isNotEmpty(alipayConfig.getReturnUrl())){
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }
        //******必传参数******
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", aliPayParam.getOutTradeNo());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", aliPayParam.getTotalAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", aliPayParam.getSubject());
        //手机网站支付默认传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
        request.setBizContent(bizContent.toString());
        String formHtml = null;
        try {
            formHtml = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return formHtml;
    }
}
