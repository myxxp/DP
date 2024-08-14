package com.crabdp.mapper;

import com.crabdp.dto.UserDTO;
import com.crabdp.entity.Blog;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import javax.management.Query;

@Mapper
public interface BlogMapper {
    boolean incrementLikeCount(Long blogId);

    boolean decrementLikeCount(Long id);



    Page<Blog> pageQuery(UserDTO user);

    Page<Blog> queryHotBlog(UserDTO user);

    @Select("select * from tb_blog where id = #{id}")
    Blog selectById(Long id);
}
