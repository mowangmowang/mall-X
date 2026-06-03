package com.macro.mall.search.controller;

import com.macro.mall.search.service.EsProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EsProductController.class)
class EsProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EsProductService esProductService;

    @Test
    void searchProducts_shouldReturn200() throws Exception {
        Page<Object> emptyPage = new PageImpl<>(Collections.emptyList());
        when(esProductService.search(anyString(), anyInt(), anyInt())).thenReturn((Page) emptyPage);

        mockMvc.perform(get("/esProduct/search/simple")
                .param("keyword", "手机")
                .param("pageNum", "0")
                .param("pageSize", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void searchProducts_withNoKeyword_shouldReturn200() throws Exception {
        Page<Object> emptyPage = new PageImpl<>(Collections.emptyList());
        when(esProductService.search(isNull(), anyInt(), anyInt())).thenReturn((Page) emptyPage);

        mockMvc.perform(get("/esProduct/search/simple"))
                .andExpect(status().isOk());
    }

    @Test
    void recommendProducts_shouldReturn200() throws Exception {
        Page<Object> emptyPage = new PageImpl<>(Collections.emptyList());
        when(esProductService.recommend(anyLong(), anyInt(), anyInt())).thenReturn((Page) emptyPage);

        mockMvc.perform(get("/esProduct/recommend/1")
                .param("pageNum", "0")
                .param("pageSize", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void searchRelatedInfo_shouldReturn200() throws Exception {
        when(esProductService.searchRelatedInfo(anyString())).thenReturn(null);

        mockMvc.perform(get("/esProduct/search/relate")
                .param("keyword", "手机"))
                .andExpect(status().isOk());
    }

    @Test
    void importAllList_shouldReturn200() throws Exception {
        when(esProductService.importAll()).thenReturn(10);

        mockMvc.perform(get("/esProduct/importAll"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_shouldReturn200() throws Exception {
        mockMvc.perform(get("/esProduct/delete/1"))
                .andExpect(status().isOk());
    }

    @Test
    void createProduct_shouldReturn200() throws Exception {
        when(esProductService.create(anyLong())).thenReturn(null);

        mockMvc.perform(get("/esProduct/create/1"))
                .andExpect(status().isOk());
    }
}