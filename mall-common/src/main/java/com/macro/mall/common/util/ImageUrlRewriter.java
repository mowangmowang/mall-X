package com.macro.mall.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 图片 URL 改写器
 * <p>
 * <b>问题背景</b>：数据库中 {@code pms_product.pic} 等字段存的是阿里云 OSS 完整 URL，
 * 前端 {@code <img>} 加载时浏览器自动加 {@code Origin} 头，OSS 对非白名单 origin 返回 403，
 * 导致图片静默失败。
 * </p>
 * <p>
 * <b>解决方案</b>：在 Service 层返回响应前调用本工具类，把 OSS URL 改写为后端代理 URL
 * （{@code http://<当前服务地址>/pic/proxy?url=OSS_URL}），前端零改动即可正确加载图片。
 * </p>
 * <p>
 * <b>为什么不修改 MBG 生成的 model</b>：
 * <ul>
 *   <li>mbg 重新生成会覆盖手动修改</li>
 *   <li>不同环境的代理地址可能不同（如 nginx 反代后域名变了）</li>
 *   <li>Service 层调用更灵活：可选改写（某些内部接口可能需要原始 URL）</li>
 * </ul>
 * </p>
 * <p>
 * <b>使用示例</b>（在 Service 层）：
 * <pre>{@code
 * List<PmsProduct> products = productMapper.selectByExample(example);
 * products.forEach(p -> p.setPic(imageUrlRewriter.rewrite(p.getPic())));
 * return products;
 * }</pre>
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@Component
public class ImageUrlRewriter {

    /**
     * 匹配 OSS 公网域名的正则
     * <p>匹配 {@code http(s)://<bucket>.<region>.aliyuncs.com/...} 形式</p>
     */
    private static final Pattern OSS_URL_PATTERN = Pattern.compile(
            "^https?://[a-z0-9-]+\\.[a-z0-9-]+\\.aliyuncs\\.com/.*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 当前服务的对外访问地址（从配置读取，默认 localhost）
     * <p>对应 application.yml 的 {@code mall.pic.proxy-base-url}</p>
     */
    @Value("${mall.pic.proxy-base-url:http://localhost:8085}")
    private String proxyBaseUrl;

    /**
     * 改写单个 URL
     * <ul>
     *   <li>非 OSS URL：原样返回</li>
     *   <li>空 / null：原样返回</li>
     *   <li>OSS URL：改写为 {@code <proxyBaseUrl>/pic/proxy?url=<encoded>}</li>
     * </ul>
     *
     * @param originalUrl 原始图片 URL
     * @return 改写后的 URL；非 OSS 或空值则原样返回
     */
    public String rewrite(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }
        if (!OSS_URL_PATTERN.matcher(originalUrl).matches()) {
            return originalUrl;
        }
        String encoded = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
        return proxyBaseUrl + "/pic/proxy?url=" + encoded;
    }

    /**
     * 批量改写 URL 列表
     *
     * @param urls 原始 URL 列表
     * @return 改写后的列表（同 List 实例 in-place 改写）
     */
    public List<String> rewriteAll(List<String> urls) {
        if (urls == null) {
            return null;
        }
        for (int i = 0; i < urls.size(); i++) {
            urls.set(i, rewrite(urls.get(i)));
        }
        return urls;
    }
}
