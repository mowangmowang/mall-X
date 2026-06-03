package com.macro.mall.portal.controller;

import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.model.UmsMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UmsMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class UmsMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UmsMemberService memberService;

    @Test
    void register_withValidParams_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/sso/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "testuser")
                .param("password", "password123")
                .param("telephone", "13800138000")
                .param("authCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        when(memberService.login("testuser", "password")).thenReturn("test-jwt-token");

        mockMvc.perform(post("/sso/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "testuser")
                .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"));
    }

    @Test
    void login_withInvalidCredentials_shouldReturnFailed() throws Exception {
        when(memberService.login("testuser", "wrong")).thenReturn(null);

        mockMvc.perform(post("/sso/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "testuser")
                .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void getCurrentMember_withNullPrincipal_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/sso/current"))
                .andExpect(status().isOk());
    }
}
