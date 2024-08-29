package com.crabdp.mapper;

import com.crabdp.dto.UserDTO;
import com.crabdp.entity.Blog;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BlogMapper {
    boolean incrementLikeCount(Long blogId);

    boolean decrementLikeCount(Long id);



    Page<Blog> pageQuery(UserDTO user);

    Page<Blog> queryHotBlog(UserDTO user);

    @Select("select * from tb_blog where id = #{id}")
    Blog selectById(Long id);

    boolean saveBlog(Blog blog);

    Page<Blog> queryBlogByUserId(Long id);

    List<Blog> queryBlogByIds(List<Long> ids);
}
