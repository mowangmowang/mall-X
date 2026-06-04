package com.macro.mall.pic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PicProxyController 单元测试（基于 standalone MockMvc + Mock RestTemplate）
 * <p>
 * <b>测试策略</b>：不依赖 Spring 容器或外部网络（OSS），通过 {@link MockMvc} 的
 * {@code standaloneSetup} 模式直接装配控制器，使用 Mockito 模拟上游
 * {@link RestTemplate} 的响应。
 * </p>
 * <p>
 * <b>为什么不走 {@code @SpringBootTest}</b>：mall-common-pic 是库模块，没有
 * {@code @SpringBootApplication} 类，强行启用会引入不必要的 Spring 上下文。
 * 单元测试 + MockMvc 已能覆盖全部控制器逻辑（包括 URL 白名单、错误码透传、
 * CORS 响应头、缓存头）。
 * </p>
 * <p>
 * <b>网络依赖测试</b>：如需验证"实际代理 OSS"的端到端行为，应在 dev 环境中手动
 * 执行 {@code curl http://localhost:8085/pic/proxy?url=<OSS_URL> -i}，不纳入
 * 自动化测试（CI 环境通常无法访问 OSS 公网）。
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
class PicProxyControllerTest {

    private MockMvc mockMvc;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        PicProxyProperties props = new PicProxyProperties();
        // 默认白名单已包含 macro-oss 与 localhost:9000
        PicProxyController controller = new PicProxyController(restTemplate, props);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("合法 OSS URL：上游 200 + image/png，应返回 200 + 字节流 + CORS 头 + 缓存头")
    void proxy_validOssUrl_shouldSucceed() throws Exception {
        // mock 上游返回 200 + PNG 字节
        byte[] fakePng = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        ResponseEntity<byte[]> upstream = ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(fakePng);
        when(restTemplate.getForEntity(eq("http://macro-oss.oss-cn-shenzhen.aliyuncs.com/test.png"), eq(byte[].class)))
                .thenReturn(upstream);

        mockMvc.perform(get("/pic/proxy").param("url", "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/test.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().exists("Cache-Control"))
                .andExpect(content().bytes(fakePng));
    }

    @Test
    @DisplayName("非白名单 URL 应返回 400 BadRequest")
    void proxy_invalidUrl_shouldReturn400() throws Exception {
        mockMvc.perform(get("/pic/proxy").param("url", "http://evil.com/malware.png"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("缺少 url 参数应返回 400")
    void proxy_missingUrl_shouldReturn400() throws Exception {
        mockMvc.perform(get("/pic/proxy"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("空字符串 URL 应返回 400")
    void proxy_emptyUrl_shouldReturn400() throws Exception {
        mockMvc.perform(get("/pic/proxy").param("url", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("上游 404 时应透传 404 状态码")
    void proxy_upstreamNotFound_shouldPassThrough() throws Exception {
        String ossUrl = "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/non-existent-99999.png";
        ResponseEntity<byte[]> upstream = ResponseEntity.status(404)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Not Found".getBytes());
        when(restTemplate.getForEntity(eq(ossUrl), eq(byte[].class))).thenReturn(upstream);

        mockMvc.perform(get("/pic/proxy").param("url", ossUrl))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("上游连接 / 读取超时（ResourceAccessException）应返回 504 GATEWAY_TIMEOUT")
    void proxy_upstreamTimeout_shouldReturn504() throws Exception {
        when(restTemplate.getForEntity(any(String.class), eq(byte[].class)))
                .thenThrow(new ResourceAccessException("connect timed out"));

        mockMvc.perform(get("/pic/proxy").param("url", "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/x.png"))
                .andExpect(status().isGatewayTimeout());
    }

    @Test
    @DisplayName("上游其他异常应返回 502 BAD_GATEWAY")
    void proxy_upstreamOtherError_shouldReturn502() throws Exception {
        when(restTemplate.getForEntity(any(String.class), eq(byte[].class)))
                .thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get("/pic/proxy").param("url", "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/x.png"))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("合法 MinIO URL（白名单内）应放行")
    void proxy_validMinioUrl_shouldSucceed() throws Exception {
        byte[] bytes = new byte[]{1, 2, 3, 4};
        ResponseEntity<byte[]> upstream = ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(bytes);
        when(restTemplate.getForEntity(eq("http://localhost:9000/bucket/img.jpg"), eq(byte[].class)))
                .thenReturn(upstream);

        mockMvc.perform(get("/pic/proxy").param("url", "http://localhost:9000/bucket/img.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes));
    }
}
