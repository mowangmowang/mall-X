package com.macro.mall.pic;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

/**
 * 图片代理控制器
 * <p>
 * <b>问题背景</b>：阿里云 OSS 对所有跨域 {@code <img>} 请求的 {@code Origin} 头做 CORS 检查，
 * 不在白名单的源（开发期 localhost:7898）会得到 403。直接 curl 200 是因为 curl 不发 Origin。
 * </p>
 * <p>
 * <b>解决方案</b>：前端改用 {@code GET /pic/proxy?url=OSS_URL} 走本服务后端代理，
 * 后端用 {@link RestTemplate}（不发 Origin）调上游 OSS，透传字节流返回。
 * </p>
 * <p>
 * <b>安全</b>：仅允许白名单内 URL（防 SSRF），上限大小、错误码透传。
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@RestController
@RequestMapping("/pic")
@RequiredArgsConstructor
public class PicProxyController {

    private final RestTemplate picProxyRestTemplate;
    private final PicProxyProperties props;

    /**
     * 代理上游图片
     *
     * @param url 上游图片完整 URL（必须在 mall.pic.allowed-prefixes 白名单内）
     * @return 图片字节流 + CORS 头 + 缓存头
     */
    @GetMapping("/proxy")
    public ResponseEntity<byte[]> proxy(@RequestParam String url) {
        // 1. 校验 URL 在白名单
        List<String> prefixes = props.getAllowedPrefixes();
        boolean allowed = prefixes != null && prefixes.stream().anyMatch(url::startsWith);
        if (!allowed) {
            return ResponseEntity.badRequest()
                    .body(("URL not in whitelist: " + url).getBytes());
        }

        try {
            // 2. 后端 fetch（无 Origin 头 → OSS 不做 CORS 拒绝）
            ResponseEntity<byte[]> upstream = picProxyRestTemplate.getForEntity(url, byte[].class);
            HttpStatus status = (HttpStatus) upstream.getStatusCode();

            // 3. 透传字节流，加 CORS + 缓存头
            HttpHeaders headers = new HttpHeaders();
            if (upstream.getHeaders().getContentType() != null) {
                headers.setContentType(upstream.getHeaders().getContentType());
            }
            headers.setCacheControl(CacheControl.maxAge(Duration.ofSeconds(props.getCacheMaxAge())).cachePublic());
            // Access-Control-Allow-Origin 已在 CorsFilterRegistration 处理（预检阶段），
            // 这里只补 actual 请求阶段的 CORS 头，避免 <img> 被浏览器 CORS 拒绝
            headers.set("Access-Control-Allow-Origin", "*");

            return new ResponseEntity<>(upstream.getBody(), headers, status);
        } catch (ResourceAccessException e) {
            // 上游超时 / 连接失败
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
