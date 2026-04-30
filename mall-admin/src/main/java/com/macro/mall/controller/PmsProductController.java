package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.component.EsProductSender;
import com.macro.mall.dto.PmsProductParam;
import com.macro.mall.dto.PmsProductQueryParam;
import com.macro.mall.dto.PmsProductResult;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductVerifyRecord;
import com.macro.mall.service.PmsProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理 Controller
 * 提供商品的增删改查、上下架、审核、推荐等功能
 * 集成 Elasticsearch 搜索引擎，通过 RabbitMQ 异步同步商品数据
 */
@Controller
@Api(tags = "PmsProductController")
@Tag(name = "PmsProductController", description = "商品管理")
@RequestMapping("/product")
public class PmsProductController {
    /**
     * 商品服务
     */
    @Autowired
    private PmsProductService productService;
    
    /**
     * ES 产品消息发送器（用于同步商品到 Elasticsearch）
     */
    @Autowired
    private EsProductSender esProductSender;

    /**
     * 创建新商品
     * 包括基本信息、SKU库存、属性值等完整信息
     * 创建成功后通过 MQ 异步同步到 ES
     * @param productParam 商品参数对象（包含所有商品信息）
     * @return 操作结果
     */
    @ApiOperation("创建商品")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody PmsProductParam productParam) {
        Long productId = productService.create(productParam);
        if (productId != null) {
            // 发送MQ消息，同步到ES
            esProductSender.send(productId, "ADD");
            return CommonResult.success(1);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 根据商品 ID 获取编辑信息
     * 返回商品的完整信息，用于前端编辑页面展示
     * @param id 商品 ID
     * @return 商品编辑信息
     */
    @ApiOperation("根据商品id获取商品编辑信息")
    @RequestMapping(value = "/updateInfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<PmsProductResult> getUpdateInfo(@PathVariable Long id) {
        PmsProductResult productResult = productService.getUpdateInfo(id);
        return CommonResult.success(productResult);
    }

    /**
     * 修改商品信息
     * 更新后通过 MQ 异步同步到 ES
     * @param id 商品 ID
     * @param productParam 待更新的商品信息
     * @return 操作结果
     */
    @ApiOperation("根据ID修改商品信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody PmsProductParam productParam) {
        int count = productService.update(id, productParam);
        if (count > 0) {
            // 发送MQ消息，同步到ES
            esProductSender.send(id, "UPDATE");
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 分页查询商品列表
     * 支持按多种条件筛选（名称、货号、分类等）
     * @param productQueryParam 查询参数
     * @param pageSize 每页条数，默认5条
     * @param pageNum 页码，默认第1页
     * @return 分页商品列表
     */
    @ApiOperation("查询商品")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<PmsProduct>> getList(PmsProductQueryParam productQueryParam,
                                                        @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<PmsProduct> productList = productService.list(productQueryParam, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(productList));
    }

    /**
     * 模糊查询商品（简单列表）
     * 根据商品名称或货号进行模糊搜索
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    @ApiOperation("根据商品名称或货号模糊查询")
    @RequestMapping(value = "/simpleList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> getList(String keyword) {
        List<PmsProduct> productList = productService.list(keyword);
        return CommonResult.success(productList);
    }

    /**
     * 批量修改商品审核状态
     * @param ids 商品 ID 列表
     * @param verifyStatus 审核状态：0-未审核，1-审核通过，2-审核不通过
     * @param detail 审核详情/原因
     * @return 操作结果
     */
    @ApiOperation("批量修改审核状态")
    @RequestMapping(value = "/update/verifyStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateVerifyStatus(@RequestParam("ids") List<Long> ids,
                                           @RequestParam("verifyStatus") Integer verifyStatus,
                                           @RequestParam("detail") String detail) {
        int count = productService.updateVerifyStatus(ids, verifyStatus, detail);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量上下架商品
     * @param ids 商品 ID 列表
     * @param publishStatus 上架状态：0-下架，1-上架
     * @return 操作结果
     */
    @ApiOperation("批量上下架商品")
    @RequestMapping(value = "/update/publishStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updatePublishStatus(@RequestParam("ids") List<Long> ids,
                                            @RequestParam("publishStatus") Integer publishStatus) {
        int count = productService.updatePublishStatus(ids, publishStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量推荐/取消推荐商品
     * @param ids 商品 ID 列表
     * @param recommendStatus 推荐状态：0-不推荐，1-推荐
     * @return 操作结果
     */
    @ApiOperation("批量推荐商品")
    @RequestMapping(value = "/update/recommendStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateRecommendStatus(@RequestParam("ids") List<Long> ids,
                                              @RequestParam("recommendStatus") Integer recommendStatus) {
        int count = productService.updateRecommendStatus(ids, recommendStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量设置/取消新品标签
     * @param ids 商品 ID 列表
     * @param newStatus 新品状态：0-非新品，1-新品
     * @return 操作结果
     */
    @ApiOperation("批量设为新品")
    @RequestMapping(value = "/update/newStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateNewStatus(@RequestParam("ids") List<Long> ids,
                                        @RequestParam("newStatus") Integer newStatus) {
        int count = productService.updateNewStatus(ids, newStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量修改商品删除状态（逻辑删除）
     * 删除时同步从 ES 中移除
     * @param ids 商品 ID 列表
     * @param deleteStatus 删除状态：0-未删除，1-删除
     * @return 操作结果
     */
    @ApiOperation("批量修改删除状态")
    @RequestMapping(value = "/update/deleteStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateDeleteStatus(@RequestParam("ids") List<Long> ids,
                                           @RequestParam("deleteStatus") Integer deleteStatus) {
        int count = productService.updateDeleteStatus(ids, deleteStatus);
        if (count > 0) {
            // 如果是删除操作，发送MQ消息
            if (deleteStatus == 1) {
                for (Long id : ids) {
                    esProductSender.send(id, "DELETE");
                }
            }
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }
    
    /**
     * 查询商品的审核记录历史
     * @param productId 商品 ID
     * @return 审核记录列表
     */
    @ApiOperation("根据商品ID查询审核记录")
    @RequestMapping(value = "/vertifyRecord/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProductVerifyRecord>> getVerifyRecord(@PathVariable Long productId) {
        List<PmsProductVerifyRecord> list = productService.getVerifyRecordList(productId);
        return CommonResult.success(list);
    }
}
