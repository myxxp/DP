package com.crabdp.mapper;

import com.crabdp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据手机号查询用户
     * @param phone
     * @return
     */
    @Select("select * from tb_user where phone = #{phone}")
    User getByPhone(String phone);

    /**
     * 插入用户
     * @param user
     */
    void insert(User user);

    @Select("select * from tb_user where id = #{id}")
    User getById(Long userId);


    @Select("select * from tb_user where id = #{userId}")
    User selectById(Long userId);

    List<User> list();
}
