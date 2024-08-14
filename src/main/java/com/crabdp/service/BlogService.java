package com.crabdp.service;


import com.crabdp.common.PageResult;
import com.crabdp.entity.Blog;

public interface BlogService {

    void likeBlog(Long id);

    PageResult queryBlog(Integer current);

    PageResult queryHotBlog(Integer current);

    Blog queryBlogById(Long id);


    Integer queryBlogLikes(Long id);



}
