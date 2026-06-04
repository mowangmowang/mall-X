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
 * CORS 预检 (Preflight) 集成测试
 * <p>
 * 验证目标：当浏览器从 {@code http://127.0.0.1:3000} 发起带 {@code Authorization} 请求头的跨域
 * OPTIONS 预检时，后端响应头 {@code Access-Control-Allow-Headers} 必须显式包含
 * {@code authorization}，否则浏览器会阻断真实请求并报错：
 * "Request header field authorization is not allowed by Access-Control-Allow-Headers in preflight response."
 * </p>
 * <p>
 * 该测试在 Spring Security 6 升级后必须通过。回归到以下任一情况即视为不通过：
 * <ul>
 *   <li>{@code SecurityConfig.filterChain} 未调用 {@code http.cors(...)}</li>
 *   <li>仅注册 {@code CorsFilter} Bean 而未在 SecurityFilterChain 中启用 CORS</li>
 *   <li>{@code setAllowCredentials(true)} 与 {@code allowedOrigin("*")} 组合触发浏览器拒绝</li>
 * </ul>
 * </p>
 * <p>
 * 运行方式：{@code mvn test -pl mall-admin -DskipTests=false -Dtest=CorsPreflightTest}。
 * 需要 dev profile 的依赖（MySQL/Redis/RabbitMQ）可用，详见 AGENTS.md。
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@SpringBootTest(classes = com.macro.mall.MallAdminApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CorsPreflightTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OPTIONS 预检带 Authorization 头时，Access-Control-Allow-Headers 必须放行 authorization")
    void preflight_authorizationHeader_shouldBeAllowed() throws Exception {
        // 模拟浏览器从 http://127.0.0.1:3000 发起跨域预检 /coupon/list
        mockMvc.perform(options("/coupon/list")
                        .header(HttpHeaders.ORIGIN, "http://127.0.0.1:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        // 关键断言来源：业务侧 axios 拦截器在 http.ts:19 设置 Authorization 头
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        Matchers.containsString("authorization")));
    }
}
