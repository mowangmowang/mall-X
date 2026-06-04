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
 * CORS 预检 (Preflight) 集成测试 - mall-ai 版
 * <p>
 * 验证目标：mall-ai 不依赖 mall-security，跨域通过 {@code CorsFilterRegistration}
 * 显式注册的 {@code CorsFilter}（order=HIGHEST_PRECEDENCE）处理。
 * </p>
 * <p>
 * 该测试回归以下任一情况即视为不通过：
 * <ul>
 *   <li>{@code CorsFilterRegistration} 未设置 {@code HIGHEST_PRECEDENCE}，导致预检被 MVC 404 拦截</li>
 *   <li>{@code mall-common-cors} 的 {@code CorsConfigurationSource} Bean 未被发现</li>
 *   <li>application-dev.yml 未配置 {@code mall.security.cors.*}，Bean 创建时属性为空</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@SpringBootTest(classes = com.macro.mall.ai.MallAiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CorsPreflightTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OPTIONS 预检带 Authorization 头时，Access-Control-Allow-Headers 必须放行 authorization")
    void preflight_authorizationHeader_shouldBeAllowed() throws Exception {
        mockMvc.perform(options("/ai/chat")
                        .header(HttpHeaders.ORIGIN, "http://127.0.0.1:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        Matchers.containsString("authorization")));
    }
}
