package com.macro.mall.common.util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 请求工具类 (Request Utility)
 * 提供获取客户端真实 IP 地址等常用方法
 */
public class RequestUtil {

    /**
     * 获取请求真实 IP 地址
     * 依次从多个代理头中尝试获取，适用于经过 Nginx、负载均衡等反向代理的场景
     *
     * @param request HTTP 请求对象
     * @return 客户端真实 IP
     */
    public static String getRequestIp(HttpServletRequest request) {
        // 1. 尝试从 x-forwarded-for 获取（常见于 Nginx 反向代理）
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            // 2. 尝试从 Proxy-Client-IP 获取（Apache 服务器）
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            // 3. 尝试从 WL-Proxy-Client-IP 获取（WebLogic 服务器）
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            // 4. 直接获取远程地址
            ipAddress = request.getRemoteAddr();
            // 从本地访问时根据网卡取本机配置的 IP
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ipAddress = inetAddress.getHostAddress();
            }
        }
        // 5. 处理多级代理情况：x-forwarded-for 可能包含多个 IP，取第一个作为真实 IP
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

}
