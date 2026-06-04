package com.macro.mall.security.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具类
 * <p>
 * 提供 JWT 的生成、解析、验证、刷新等核心能力。
 * 被 {@code JwtAuthenticationTokenFilter} 用于解析请求头中的 Token，
 * 被各业务模块的 Service（{@code UmsAdminServiceImpl} / {@code UmsMemberServiceImpl}）
 * 用于用户登录与 Token 刷新。
 * </p>
 * <p>
 * <b>算法与安全约束</b>：
 * <ul>
 *   <li>签名算法：{@code Jwts.SIG.HS512}（HMAC-SHA512，对称密钥）</li>
 *   <li>密钥长度：必须 ≥ 64 字节（512 bits），符合 RFC 7518 §3.2 / JJWT 0.12+ 强制要求</li>
 *   <li>密钥来源：{@code jwt.secret} 配置项，UTF-8 编码</li>
 *   <li>Token 前缀：{@code tokenHead}（默认 "Bearer "），用于从请求头剥离前缀</li>
 * </ul>
 * </p>
 * <p>
 * <b>关于 secret 与 tokenHead 的区别</b>：
 * <ul>
 *   <li>{@code secret}：签名密钥明文，<b>不能泄露</b>，配置在 {@code application.yml}</li>
 *   <li>{@code tokenHead}：请求头中 Token 的可读前缀（如 "Bearer "），用于解析时剥离，
 *       <b>不属于密钥</b>，泄露不影响安全</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
public class JwtTokenUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);

    /** JWT 标准 claim 字段名：用户名（subject） */
    private static final String CLAIM_KEY_USERNAME = "sub";

    /** 自定义 claim 字段名：创建时间戳，用于刷新窗口判定 */
    private static final String CLAIM_KEY_CREATED = "created";

    /** Token 刷新窗口：30 分钟内不重复刷新，避免刷新风暴 */
    private static final int TOKEN_REFRESH_WINDOW_SECONDS = 30 * 60;

    /**
     * JWT 签名密钥（明文，来自 {@code jwt.secret} 配置项）
     * <p>
     * 每次签名/解析时由 {@link #parseSigningKey()} 派生为 {@link SecretKey} 使用。
     * </p>
     */
    @Value("${jwt.secret}")
    private String secret;

    /** Token 有效期（秒），默认 7 天 */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Token 请求头前缀（默认 "Bearer "），用于从请求头剥离时使用
     * <p>不属于密钥，仅是可读的协议前缀。</p>
     */
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    // ====================== Token 生成 ======================

    /**
     * 根据 Spring Security 用户详情生成签名 Token
     * <p>
     * 用户登录时由业务 Service 调用。将用户名放入 JWT 标准 {@code sub} claim，
     * 当前时间放入自定义 {@code created} claim。
     * </p>
     *
     * @param userDetails Spring Security 用户详情
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * 根据 claim 集合生成签名 Token（内部方法，刷新时重签用）
     *
     * @param claims 自定义 claim 键值对
     * @return 签名后的 JWT 字符串
     */
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .expiration(generateExpirationDate())
                .signWith(parseSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    // ====================== Token 解析 ======================

    /**
     * 从纯 JWT 字符串解析 claim 集合
     * <p>
     * 解析失败（签名错误、过期、格式错误等）时返回 {@code null}，由调用方决定如何处理。
     * </p>
     * <p>
     * <b>安全说明</b>：异常日志只记录异常类型/消息，<b>绝不打印完整 token</b>
     * （token 等同于用户凭证，泄露到日志会构成安全风险）。
     * </p>
     *
     * @param token 纯 JWT 字符串（不含 "Bearer " 前缀）
     * @return claim 集合；解析失败返回 {@code null}
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(parseSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            LOGGER.info("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中提取用户名
     *
     * @param token 纯 JWT 字符串
     * @return 用户名；解析失败或 claim 中无 sub 字段时返回 {@code null}
     */
    public String getUserNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 Token 中提取过期时间
     *
     * @param token 纯 JWT 字符串
     * @return 过期时间；解析失败返回 {@code null}
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    // ====================== Token 验证 ======================

    /**
     * 校验 Token 是否属于指定用户且未过期
     *
     * @param token       纯 JWT 字符串
     * @param userDetails Spring Security 用户详情
     * @return {@code true} 表示有效；用户名不匹配、已过期、解析失败时返回 {@code false}
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token 纯 JWT 字符串
     * @return {@code true} 表示已过期或解析失败（过期与失败统一视为"不可用"）
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate == null || expiredDate.before(new Date());
    }

    // ====================== Token 刷新 ======================

    /**
     * 刷新请求头中的 Token
     * <p>
     * 调用链：剥离 "Bearer " 前缀 → 解析 claim → 校验未过期 → 窗口内原样返回 / 窗口外重签。
     * 窗口外重签时更新 {@code created} claim，使刷新后的 Token 在下一个 30 分钟窗口内不再被刷新。
     * </p>
     *
     * @param oldToken 完整请求头 Token（含 "Bearer " 前缀）
     * @return 新 Token；输入为空、解析失败、已过期时返回 {@code null}
     */
    public String refreshToken(String oldToken) {
        if (StrUtil.isEmpty(oldToken)) {
            return null;
        }

        // 剥离 "Bearer " 前缀，得到纯 JWT 字符串
        String pureToken = oldToken.substring(tokenHead.length());
        if (StrUtil.isEmpty(pureToken)) {
            return null;
        }

        Claims claims = getClaimsFromToken(pureToken);
        if (claims == null || isTokenExpired(pureToken)) {
            return null;
        }

        // 刷新窗口内不重复刷新；窗口外更新 created claim 并重签
        if (tokenRefreshJustBefore(pureToken, TOKEN_REFRESH_WINDOW_SECONDS)) {
            return pureToken;
        }
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * 判断当前时间是否在 Token 创建后的 {@code time} 秒窗口内
     * <p>
     * 用于 {@link #refreshToken(String)} 的窗口判定，避免短时间内的重复重签。
     * </p>
     *
     * @param token 纯 JWT 字符串
     * @param time  窗口大小（秒）
     * @return {@code true} 表示当前时间落在 {@code [created, created + time]} 区间内
     */
    private boolean tokenRefreshJustBefore(String token, int time) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        Date created = claims.get(CLAIM_KEY_CREATED, Date.class);
        if (created == null) {
            return false;
        }
        Date now = new Date();
        return now.after(created) && now.before(DateUtil.offsetSecond(created, time));
    }

    // ====================== 内部辅助 ======================

    /**
     * 从 {@link #secret} 字符串派生 JJWT 签名所需的 {@link SecretKey} 对象
     * <p>
     * 每次签名/解析都重新派生：JJWT 的 {@code Keys.hmacShaKeyFor(...)} 是无状态工厂方法，
     * 重复调用零成本（仅做一次 UTF-8 编码 + 长度校验），且避免缓存 SecretKey 带来的状态污染。
     * </p>
     *
     * @return JJWT 签名密钥
     */
    private SecretKey parseSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 Token 过期时间 = 当前时间 + {@code expiration} 秒
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }
}
