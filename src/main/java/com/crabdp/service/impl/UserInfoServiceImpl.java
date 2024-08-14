package com.crabdp.service.impl;

import com.crabdp.entity.UserInfo;
import com.crabdp.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.crabdp.mapper.UserInfoMapper;
@Service
public class UserInfoServiceImpl implements UserInfoService {


    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo info(Long userId) {
        UserInfo userInfo = userInfoMapper.getById(userId);
        return userInfo;
    }

}
