package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.user.service.UserApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user/passport")
public class UserApiController {

    @Autowired
    UserApiService userApiService;

    @RequestMapping("getUserAddresses")
    List<UserAddress> getUserAddresses(HttpServletRequest request){

        String userId = request.getHeader("userId");

        List<UserAddress> addressesList = userApiService.getUserAddresses(userId);
        return addressesList;
    }

    @RequestMapping("verify/{token}")
    Map<String, Object> verify(@PathVariable("token") String token){

        Map<String, Object> map = userApiService.verify(token);

        return map;
    }

    @RequestMapping("login")
    public Result login(@RequestBody UserInfo userInfo) {

        userInfo = userApiService.login(userInfo);

        if (null != userInfo) {
            return Result.ok(userInfo);
        } else {
            return Result.fail("登录失败");
        }
    }

    @RequestMapping("ping")
    public String ping() {
        return "pong";
    }
}
