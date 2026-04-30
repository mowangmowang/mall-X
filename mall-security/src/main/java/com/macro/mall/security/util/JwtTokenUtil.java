package com.macro.mall.security.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具类 (JWT Token Utility)
 * <p>
 * 提供 JWT 令牌的生成、解析、验证和刷新功能。
 * JWT 结构：header.payload.signature
 * </p>
 * <ul>
 *   <li><b>Header（头部）：</b>声明加密算法和令牌类型，如 {"alg": "HS512", "typ": "JWT"}</li>
 *   <li><b>Payload（负载）：</b>存储用户信息，如 {"sub": "admin", "created": 1489079981393, "exp": 1489684781}</li>
 *   <li><b>Signature（签名）：</b>使用 HMACSHA512(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret) 生成</li>
 * </ul>
 */
public class JwtTokenUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);
    
    /** JWT Payload 中的用户名字段名 */
    private static final String CLAIM_KEY_USERNAME = "sub";
    
    /** JWT Payload 中的创建时间字段名 */
    private static final String CLAIM_KEY_CREATED = "created";
    
    /** JWT 签名密钥（从配置文件读取：jwt.secret） */
    @Value("${jwt.secret}")
    private String secret;
    
    /** JWT 令牌有效期（秒，从配置文件读取：jwt.expiration） */
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /** JWT 令牌前缀（从配置文件读取：jwt.tokenHead，如 "Bearer "） */
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    /**
     * 根据 Claims（负载）生成 JWT 令牌
     *
     * @param claims JWT 负载数据，包含用户信息和自定义字段
     * @return 生成的 JWT 令牌字符串
     */
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims) // 设置负载
                .setExpiration(generateExpirationDate()) // 设置过期时间
                .signWith(SignatureAlgorithm.HS512, secret) // 使用 HS512 算法和密钥签名
                .compact(); // 压缩为字符串
    }

    /**
     * 从 JWT 令牌中解析负载（Payload）
     *
     * @param token JWT 令牌字符串
     * @return Claims 对象，包含负载中的所有字段
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret) // 设置签名密钥用于验证
                    .parseClaimsJws(token) // 解析并验证 JWT
                    .getBody(); // 获取负载部分
        } catch (Exception e) {
            LOGGER.info("JWT格式验证失败:{}", token);
        }
        return claims;
    }

    /**
     * 计算令牌的过期时间
     *
     * @return 过期时间点（当前时间 + 配置的有效期）
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 从 JWT 令牌中提取用户名
     *
     * @param token JWT 令牌字符串
     * @return 用户名，若解析失败则返回 null
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject(); // 从 "sub" 字段获取用户名
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 验证 JWT 令牌是否有效
     *
     * @param token 客户端传入的 JWT 令牌
     * @param userDetails 从数据库查询的用户信息（用于比对用户名和权限）
     * @return true 表示令牌有效，false 表示无效或已过期
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);
        // 验证条件：1. 用户名匹配 2. 令牌未过期
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * 判断令牌是否已过期
     *
     * @param token JWT 令牌字符串
     * @return true 表示已过期，false 表示未过期
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate.before(new Date()); // 过期时间早于当前时间即为过期
    }

    /**
     * 从令牌中提取过期时间
     *
     * @param token JWT 令牌字符串
     * @return 过期时间点
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 根据用户信息生成 JWT 令牌
     *
     * @param userDetails Spring Security 用户详情对象（包含用户名和权限）
     * @return 生成的 JWT 令牌字符串
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername()); // 设置用户名
        claims.put(CLAIM_KEY_CREATED, new Date()); // 设置创建时间
        return generateToken(claims);
    }

    /**
     * 刷新令牌（在旧令牌未过期时生成新令牌）
     * <p>
     * 刷新规则：
     * 1. 若旧令牌为空或格式错误，返回 null
     * 2. 若旧令牌已过期，不支持刷新，返回 null
     * 3. 若旧令牌在 30 分钟内刚刷新过，返回原令牌（避免频繁刷新）
     * 4. 否则生成新令牌（更新创建时间，延长有效期）
     * </p>
     *
     * @param oldToken 带前缀的旧令牌（如 "Bearer xxxxx.yyyyy.zzzzz"）
     * @return 刷新后的令牌，若无法刷新则返回 null
     */
    public String refreshHeadToken(String oldToken) {
        if(StrUtil.isEmpty(oldToken)){
            return null;
        }
        
        // 去除前缀（如 "Bearer "），提取纯令牌字符串
        String token = oldToken.substring(tokenHead.length());
        if(StrUtil.isEmpty(token)){
            return null;
        }
        
        // 解析令牌，若校验失败则返回 null
        Claims claims = getClaimsFromToken(token);
        if(claims==null){
            return null;
        }
        
        // 若令牌已过期，不支持刷新
        if(isTokenExpired(token)){
            return null;
        }
        
        // 防抖机制：若 30 分钟内刚刷新过，直接返回原令牌
        if(tokenRefreshJustBefore(token, 30*60)){
            return token;
        } else {
            // 更新创建时间为当前时间，重新生成令牌（相当于延长有效期）
            claims.put(CLAIM_KEY_CREATED, new Date());
            return generateToken(claims);
        }
    }

    /**
     * 判断令牌是否在指定时间内刚刚刷新过
     *
     * @param token 原始令牌字符串
     * @param time 指定时间阈值（秒）
     * @return true 表示在阈值内刚刷新过，false 表示可以刷新
     */
    private boolean tokenRefreshJustBefore(String token, int time) {
        Claims claims = getClaimsFromToken(token);
        Date created = claims.get(CLAIM_KEY_CREATED, Date.class); // 获取令牌创建时间
        Date refreshDate = new Date(); // 当前时间
        
        // 判断：当前时间在创建时间之后，且在创建时间 + time 秒之前
        if(refreshDate.after(created) && refreshDate.before(DateUtil.offsetSecond(created, time))){
            return true;
        }
        return false;
    }
}
