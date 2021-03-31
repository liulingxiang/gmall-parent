package com.atguigu.gmall.gateway.config;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.result.ResultCodeEnum;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class AuthFilter implements GlobalFilter {

    @Autowired
    UserFeignClient userFeignClient;

    @Value("${authUrls.url}")
    String authUrls;

    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        String uri = request.getURI().toString();

        if (uri.contains(".passport") || uri.contains(".js") || uri.contains(".jpg") || uri.contains(".png") || uri.contains(".ico") || uri.contains(".css")) {
            return chain.filter(exchange);
        }
        System.out.println("uri = " + uri);

        //黑名单
        if (antPathMatcher.match("**/inner/**", uri)) {
            // 内部接口禁止访问
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 鉴权
        String token = getToken(request);
        String userId = null;
        if (!StringUtils.isEmpty(token)) {
            // 需要登录，调用cas对请求的token进行认证
            Map<String, Object> userMap = userFeignClient.verify(token);
            if (userMap != null) {
                userId = (String) userMap.get("userId");
            }
        }

        //白名单
        String[] splitAuthUrls = authUrls.split(",");
        for (String splitAuthUrl : splitAuthUrls) {
            if (uri.contains(splitAuthUrl)) {
                // 需要登录，调用cas对请求的token进行认证
                if (StringUtils.isEmpty(userId)){
                    // 认证失败，设置重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);// http重定向协议
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + uri);// 将重定向的地址信息设置到http的header中
                    Mono<Void> voidMono = response.setComplete();// 返回mono给springmvc容器处理
                    return voidMono;
                }
            }
        }

        // 如果是购物车，那么即便不用登录也能访问，但是如果登录需要得到userId
        if (!StringUtils.isEmpty(userId)) {
            // 将用户id放到http请求的header中
            request.mutate().header("userId", userId);
            // 认证成功，将用户id传递到后台服务
            exchange.mutate().request(request);
            return chain.filter(exchange);
        }
        // 某些功能，需要特殊的id，比如临时id，需要在网关中进行设置
        String userTempId = getUserTempId(request);
        if (!StringUtils.isEmpty(userTempId)) {
            // 将用户id放到http请求的header中
            request.mutate().header("userTempId", userTempId);
            // 认证成功，将用户id传递到后台服务
            exchange.mutate().request(request);
            return chain.filter(exchange);
        }
        return chain.filter(exchange);
    }

    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = null;
        //从cookie中获得token
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies != null) {
            List<HttpCookie> cookieList = cookies.get("userTempId");
            if (cookieList != null) {
                for (HttpCookie cookie : cookieList) {
                    if (cookie.getName().equals("userTempId")) {
                        userTempId = cookie.getValue();
                    }
                }
            }
        }
        //如果cookie中没有token，可以尝试从header中获取token
        if (StringUtils.isEmpty(userTempId)) {
            List<String> list = request.getHeaders().get("userTempId");
            if (list != null && list.size() > 0) {
                userTempId = list.get(0);
            }
        }
        return userTempId;
    }

    /*
     * 从cookie或者header中获取token
     */
    private String getToken(ServerHttpRequest request) {
        String token = null;
        //从cookie中获得token
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies != null) {
            List<HttpCookie> cookieList = cookies.get("token");
            if (cookieList != null) {
                for (HttpCookie tokenCookie : cookieList) {
                    if (tokenCookie.getName().equals("token")) {
                        token = tokenCookie.getValue();
                    }
                }
            }
        }
        //如果cookie中没有token，可以尝试从header中获取token
        if (StringUtils.isEmpty(token)) {
            List<String> tokenList = request.getHeaders().get("token");
            if (tokenList != null && tokenList.size() > 0) {
                token = tokenList.get(0);
            }
        }
        return token;
    }

    /*
     * 设置页面输出流
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 封装分会结构的字节数组
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        // 转化成mono的返回结果流
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        // 设置返回的编码格式，防止乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 返回的mono对象
        Mono<Void> voidMono = response.writeWith(Mono.just(wrap));
        return voidMono;
    }
}
