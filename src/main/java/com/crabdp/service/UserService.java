package com.crabdp.service;

import com.crabdp.dto.LoginFormDTO;
import com.crabdp.dto.UserDTO;
import com.crabdp.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;


public interface UserService{


    void sendCode(String phone, HttpSession session);

    String login(LoginFormDTO loginFormDTO, HttpSession session);


    UserDTO getById(Long userId);

    void  sign();

    Integer  signCount();

    List<User> list();

    void logout(HttpServletRequest request);
}
