package com.macro.mall.security;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS 预检 (Preflight) 集成测试 - mall-portal 版
 * <p>
 * 验证目标：当浏览器从 {@code http://127.0.0.1:3000} 发起带 {@code Authorization} 请求头的跨域
 * OPTIONS 预检时，后端响应头 {@code Access-Control-Allow-Headers} 必须显式包含
 * {@code authorization}，否则浏览器会阻断真实请求。
 * </p>
 * <p>
 * mall-portal 通过依赖 mall-security 间接获得 {@code CorsConfigurationSource} Bean（由
 * {@code mall-common-cors} 模块提供），并由 {@code SecurityConfig.filterChain().cors(...)} 接入
 * Security 过滤器链。本测试在重构后必须保持通过，确保 CORS 单一来源策略有效。
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@SpringBootTest(classes = com.macro.mall.portal.MallPortalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CorsPreflightTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OPTIONS 预检带 Authorization 头时，Access-Control-Allow-Headers 必须放行 authorization")
    void preflight_authorizationHeader_shouldBeAllowed() throws Exception {
        mockMvc.perform(options("/home/content")
                        .header(HttpHeaders.ORIGIN, "http://127.0.0.1:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        Matchers.containsString("authorization")));
    }
}
