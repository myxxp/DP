package com.crabdp.service;

import cn.hutool.system.UserInfo;
import com.crabdp.dto.LoginFormDTO;
import com.crabdp.dto.UserDTO;

import javax.servlet.http.HttpSession;


public interface UserService{


    void sendCode(String phone, HttpSession session);

    String login(LoginFormDTO loginFormDTO, HttpSession session);


    UserDTO getById(Long userId);

    void  sign();

    Integer  signCount();
}
