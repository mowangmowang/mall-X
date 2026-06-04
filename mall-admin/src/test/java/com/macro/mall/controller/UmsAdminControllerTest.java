package com.macro.mall.controller;

import com.macro.mall.model.UmsAdmin;
import com.macro.mall.service.UmsAdminService;
import com.macro.mall.service.UmsRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UmsAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class UmsAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UmsAdminService adminService;

    @MockBean
    private UmsRoleService roleService;

    @Test
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        when(adminService.login("admin", "password")).thenReturn("test-jwt-token");

        mockMvc.perform(post("/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"));
    }

    @Test
    void login_withInvalidCredentials_shouldReturnFailedResult() throws Exception {
        when(adminService.login("admin", "wrong")).thenReturn(null);

        mockMvc.perform(post("/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void register_withValidParams_shouldReturn200() throws Exception {
        UmsAdmin registeredAdmin = new UmsAdmin();
        registeredAdmin.setId(1L);
        registeredAdmin.setUsername("newuser");
        when(adminService.register(any())).thenReturn(registeredAdmin);

        mockMvc.perform(post("/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"password123\",\"nickName\":\"New User\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    void getAdminInfo_withNullPrincipal_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
