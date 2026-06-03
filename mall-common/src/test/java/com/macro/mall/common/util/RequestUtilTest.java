package com.macro.mall.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestUtilTest {

    @Test
    void getRequestIpWithRemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        String ip = RequestUtil.getRequestIp(request);
        assertEquals("192.168.1.1", ip);
    }

    @Test
    void getRequestIpWithXForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn("10.0.0.1, 192.168.1.1");
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);

        String ip = RequestUtil.getRequestIp(request);
        assertEquals("10.0.0.1", ip);
    }

    @Test
    void getRequestIpWithLocalhost() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = RequestUtil.getRequestIp(request);
        assertNotNull(ip);
    }
}