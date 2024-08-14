package com.crabdp.mapper;

import com.crabdp.entity.User;
import com.crabdp.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserInfoMapper {

    @Select("select * from tb_user_info where user_id = #{userId}")
    UserInfo getById(Long userId);




}
