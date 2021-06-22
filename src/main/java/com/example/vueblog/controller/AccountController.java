package com.example.vueblog.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.vueblog.comm.dto.LoginDto;
import com.example.vueblog.entity.User;
import com.example.vueblog.result.Result;
import com.example.vueblog.service.UserService;
import com.example.vueblog.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
@Api(tags = "用户注册登录管理")
@RestController
public class AccountController {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;
    @CrossOrigin
    @ApiOperation(value = "注册接口")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Result register(@Validated @RequestBody User user){
        if(userService.getOne(new QueryWrapper<User>().eq("username",user.getUsername()))!=null){
            return Result.fail("用户名已存在");
        }
        String password=user.getPassword();
        user.setPassword(SecureUtil.md5(password));
        user.setCreated(LocalDateTime.now());
        if(userService.save(user)){
            return Result.succ(user);
        }
        return Result.fail("注册失败请重试");

    }

    @ApiOperation(value = "登录接口")
    @CrossOrigin
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {

        User user = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername()));
        Assert.notNull(user, "用户不存在");

        if(!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))){
            return Result.fail("密码不正确");
        }
        String jwt = jwtUtils.generateToken(user.getId());

        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        return Result.succ(MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("avatar", user.getAvatar())
                .put("email", user.getEmail())
                .map()
        );
    }

    @ApiOperation(value = "注销接口")
    @RequiresAuthentication
    @GetMapping("/logout")
    public Result logout() {
        SecurityUtils.getSubject().logout();
        return Result.succ(null);
    }

}