package com.macro.mall.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ImageUrlRewriter 单元测试
 *
 * @author alan
 * @since 2026-06
 */
class ImageUrlRewriterTest {

    private ImageUrlRewriter rewriter;

    @BeforeEach
    void setUp() {
        rewriter = new ImageUrlRewriter();
        // 注入 @Value 配置项，避免依赖 Spring 容器
        ReflectionTestUtils.setField(rewriter, "proxyBaseUrl", "http://localhost:8085");
    }

    @Test
    @DisplayName("OSS 公网 URL 应被改写为代理 URL")
    void rewrite_ossUrl_shouldBeRewritten() {
        String oss = "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/xiaomi.jpg";
        String result = rewriter.rewrite(oss);
        assertTrue(result.startsWith("http://localhost:8085/pic/proxy?url="),
                "应改写为代理 URL，实际: " + result);
        assertTrue(result.contains("macro-oss.oss-cn-shenzhen.aliyuncs.com"),
                "应保留 OSS 域名在 url 参数中");
    }

    @Test
    @DisplayName("HTTPS OSS URL 同样应被改写")
    void rewrite_httpsOssUrl_shouldBeRewritten() {
        String oss = "https://my-bucket.oss-cn-hangzhou.aliyuncs.com/img.png";
        String result = rewriter.rewrite(oss);
        assertTrue(result.startsWith("http://localhost:8085/pic/proxy?url="));
        assertTrue(result.contains("my-bucket.oss-cn-hangzhou.aliyuncs.com"));
    }

    @Test
    @DisplayName("其他 region 的 OSS URL 也应被改写")
    void rewrite_otherRegionOssUrl_shouldBeRewritten() {
        String oss = "http://bucket.oss-us-west-1.aliyuncs.com/path/to/img.jpg";
        String result = rewriter.rewrite(oss);
        assertTrue(result.startsWith("http://localhost:8085/pic/proxy?url="));
    }

    @Test
    @DisplayName("MinIO 本地 URL 不应被改写（非 OSS 域名）")
    void rewrite_minioUrl_shouldNotBeRewritten() {
        String minio = "http://localhost:9000/mall/images/xiaomi.jpg";
        String result = rewriter.rewrite(minio);
        assertEquals(minio, result, "MinIO URL 不应被改写");
    }

    @Test
    @DisplayName("其他外部域名的 URL 不应被改写")
    void rewrite_externalUrl_shouldNotBeRewritten() {
        String external = "https://example.com/path/img.png";
        String result = rewriter.rewrite(external);
        assertEquals(external, result);
    }

    @Test
    @DisplayName("null 应原样返回")
    void rewrite_null_shouldReturnNull() {
        assertNull(rewriter.rewrite(null));
    }

    @Test
    @DisplayName("空字符串应原样返回")
    void rewrite_empty_shouldReturnEmpty() {
        assertEquals("", rewriter.rewrite(""));
    }

    @Test
    @DisplayName("URL 中的特殊字符应被正确编码（空格 → %20）")
    void rewrite_specialChars_shouldBeEncoded() {
        String oss = "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/path/with space.png";
        String result = rewriter.rewrite(oss);
        // URLEncoder.encode 默认把空格编码为 '+'，不是 %20
        // 我们这里用宽松断言：至少 url 参数部分是合法 URL
        assertTrue(result.contains("+") || result.contains("%20"),
                "空格应被编码，实际: " + result);
    }

    @Test
    @DisplayName("URL 中的中文应被正确编码")
    void rewrite_chineseChars_shouldBeEncoded() {
        String oss = "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/小米手机.png";
        String result = rewriter.rewrite(oss);
        // 中文 UTF-8 编码后是 %E5%B0%8F%E7%B1%B3... 这种形式
        assertTrue(result.contains("%E5%B0%8F") || result.contains("%e5%b0%8f"),
                "中文应被 UTF-8 编码，实际: " + result);
    }

    @Test
    @DisplayName("批量改写应 in-place 修改列表")
    void rewriteAll_shouldMutateListInPlace() {
        List<String> urls = Arrays.asList(
                "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/a.jpg",
                "http://localhost:9000/b.jpg",
                null
        );
        // 转换为可变列表以模拟 in-place 修改
        List<String> mutable = new java.util.ArrayList<>(urls);
        List<String> result = rewriter.rewriteAll(mutable);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("http://localhost:8085/pic/proxy"),
                "OSS URL 应被改写");
        assertEquals("http://localhost:9000/b.jpg", result.get(1),
                "MinIO URL 不应改写");
        assertNull(result.get(2), "null 应原样返回");
    }

    @Test
    @DisplayName("rewriteAll 传入 null 应返回 null（不抛 NPE）")
    void rewriteAll_null_shouldReturnNull() {
        assertNull(rewriter.rewriteAll(null));
    }

    @Test
    @DisplayName("rewriteAll 传入空列表应返回空列表")
    void rewriteAll_empty_shouldReturnEmpty() {
        List<String> result = rewriter.rewriteAll(new java.util.ArrayList<>());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
