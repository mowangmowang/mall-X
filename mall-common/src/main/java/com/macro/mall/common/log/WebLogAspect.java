package com.macro.mall.common.log;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.macro.mall.common.domain.WebLog;
import com.macro.mall.common.util.RequestUtil;
import io.swagger.annotations.ApiOperation;
import net.logstash.logback.marker.Markers;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一日志处理切面 (Unified Log Processing Aspect)
 * 用于拦截 Controller 层请求，记录请求参数、响应结果及耗时等信息，并输出至日志系统。
 */
@Aspect // 定义切面
@Component // 将切面类声明为组件
@Order(1) // 设置优先级，数字越小优先级越高
public class WebLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);

    /**
     * 定义切点 (Pointcut)：拦截所有 Controller 包下的公共方法
     */
    @Pointcut("execution(public * com.macro.mall.controller.*.*(..))||execution(public * com.macro.mall.*.controller.*.*(..))")
    public void webLog() {
    }

    /**
     * 前置通知 (Before Advice)：在目标方法执行前触发
     *
     * @param joinPoint 连接点
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
    }

    /**
     * 后置返回通知 (After Returning Advice)：在目标方法成功执行后触发
     *
     * @param ret 返回值
     */
    @AfterReturning(value = "webLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
    }

    /**
     * 环绕通知 (Around Advice)：核心逻辑，记录请求详细信息
     *
     * @param joinPoint 连接点
     * @return 目标方法执行结果
     * @throws Throwable 异常
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取当前请求对象 (HttpServletRequest)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        // 初始化 WebLog 对象，用于封装请求日志信息
        WebLog webLog = new WebLog();
        
        // 执行目标方法
        Object result = joinPoint.proceed();
        
        // 获取签名信息以提取方法详情
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        
        // 如果方法上有 ApiOperation 注解，提取其描述信息
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation log = method.getAnnotation(ApiOperation.class);
            webLog.setDescription(log.value());
        }
        
        long endTime = System.currentTimeMillis();
        
        // 填充 WebLog 各项属性
        String urlStr = request.getRequestURL().toString();
        webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
        webLog.setUsername(request.getRemoteUser());
        webLog.setIp(RequestUtil.getRequestIp(request));
        webLog.setMethod(request.getMethod());
        // 提取并设置请求参数
        webLog.setParameter(getParameter(method, joinPoint.getArgs()));
        webLog.setResult(result);
        webLog.setSpendTime((int) (endTime - startTime));
        webLog.setStartTime(startTime);
        webLog.setUri(request.getRequestURI());
        webLog.setUrl(request.getRequestURL().toString());
        
        // 构建用于日志输出的 Map，便于结构化日志采集 (如 Logstash/Elasticsearch)
        Map<String,Object> logMap = new HashMap<>();
        logMap.put("url", webLog.getUrl());
        logMap.put("method", webLog.getMethod());
        logMap.put("parameter", webLog.getParameter());
        logMap.put("spendTime", webLog.getSpendTime());
        logMap.put("description", webLog.getDescription());
        
        // 输出 INFO 级别日志，包含结构化标记和完整 JSON 字符串
        // LOGGER.info("{}", JSONUtil.parse(webLog)); // 旧版简单输出
        LOGGER.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());
        
        return result;
    }

    /**
     * 根据方法和传入的参数获取请求参数
     * 主要处理 @RequestBody 和 @RequestParam 注解修饰的参数
     *
     * @param method 目标方法
     * @param args   方法参数数组
     * @return 格式化后的请求参数对象
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        
        for (int i = 0; i < parameters.length; i++) {
            // 处理 @RequestBody 注解：直接将参数对象加入列表
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            
            // 处理 @RequestParam 注解：将参数封装为 Map{key: value} 后加入列表
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                // 优先使用注解指定的 value 作为 key，否则使用参数名
                String key = parameters[i].getName();
                if (!StrUtil.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                // 仅当参数值不为 null 时记录
                if (args[i] != null) {
                    map.put(key, args[i]);
                    argList.add(map);
                }
            }
        }
        
        // 根据参数列表大小返回不同格式，优化日志可读性
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }
}
