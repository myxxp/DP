package com.crabdp.controller;


import com.crabdp.dto.UserDTO;
import com.crabdp.entity.UserInfo;
import com.crabdp.common.Result;
import com.crabdp.dto.LoginFormDTO;
import com.crabdp.entity.User;
import com.crabdp.service.UserInfoService;
import com.crabdp.service.UserService;
import com.crabdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 发送手机验证码
     * 已同步
     * @param phone
     * @param session
     * @return
     */

    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 发送短信验证码并保存验证码
//        try {
//            // 发送短信验证码并保存验证码
//            userService.sendCode(phone, session);
//            return Result.success();
//        } catch (RuntimeException e) {
//            // 捕获到异常后，将异常信息包装成统一格式返回给客户端
//            return Result.error(e.getMessage());
//        }
        userService.sendCode(phone, session);
        return Result.ok();

    }
    /**
     * 登录功能
     * 已同步
     * @param loginFormDTO
     * @param session
     * @return
     */
    @PostMapping("login")

    public Result<String>  login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        // 登录
        String token = userService.login(loginFormDTO, session);
        return Result.ok(token);
    }

    /**
     * 获取当前登录用户并返回
     * 已同步
     * @return
     */
    @GetMapping("me")
    public Result<UserDTO> me() {
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    /**
     * 获取用户详情
     * 已同步
     * @param userId
     * @return
     */
    @GetMapping("/info/{id}")
    public Result<UserInfo> info(@PathVariable("id") Long userId) {
        UserInfo userInfo = userInfoService.info(userId);
        if (userInfo == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        userInfo.setCreateTime(null);
        userInfo.setUpdateTime(null);

        return Result.ok(userInfo);
    }

    /**
     * 根据用户id查询用户
     * 已同步
     * @param userId
     * @return
     */
    @GetMapping("/{id}")
    public Result<UserDTO> queryUserById(@PathVariable("id") Long userId) {
        UserDTO userDTO = userService.getById(userId);
        if (userDTO == null) {
            return Result.ok();
        }

        return Result.ok(userDTO);
    }

    /**
     * 签到
     * 已同步
     * @return
     */
    @PostMapping("/sign")
    public Result sign(){

        userService.sign();
        return Result.ok();
    }
    /**
     * 签到次数
     * //TODO 未实现
     * @return
     */

    @GetMapping("/signCount")
    public Result<Integer> signCount(){
        Integer count = 0;
        count = userService.signCount();
        return Result.ok(count);
    }


}
