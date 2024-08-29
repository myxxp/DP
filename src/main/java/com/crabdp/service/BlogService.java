package com.crabdp.service;


import com.crabdp.common.PageResult;
import com.crabdp.dto.UserDTO;
import com.crabdp.entity.Blog;
import com.crabdp.entity.ScrollResult;

import java.util.List;

public interface BlogService {

    void likeBlog(Long id);

    PageResult queryBlog(Integer current);

    List<Blog> queryHotBlog(Integer current);

    Blog queryBlogById(Long id);


    List<UserDTO> queryBlogLikes(Long id);

    long saveBlog(Blog blog);


    PageResult queryBlogByUserId(Integer current, Long id);

    ScrollResult queryBlogOfFollow(Long max, Integer offset);
}
