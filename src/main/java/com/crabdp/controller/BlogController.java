package com.crabdp.controller;

import com.crabdp.common.PageResult;
import com.crabdp.common.Result;
import com.crabdp.entity.Blog;
import com.crabdp.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.ApplicationScope;

@RestController
@RequestMapping("/blog")
@Slf4j

public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 点赞博客
     * 已同步
     * @param id
     * @return
     */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id){

        blogService.likeBlog(id);
        return Result.ok();

    };

    /**
     * 查询我的博客
     * 已同步
     * @param current
     * @return
     */
    @GetMapping("/of/me")

    public Result<PageResult> queryBlog(@RequestParam(value = "current", defaultValue = "1") Integer current){
        log.info("查询我的博客：{}", current);
        PageResult pageResult = blogService.queryBlog(current);

        return Result.ok(pageResult);
    }

    /**
     * 查询热门博客
     * 已同步
     * @param current
     * @return
     */
    @GetMapping("/hot")
    public Result<PageResult> queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current){
        log.info("查询热门博客：{}", current);
        PageResult pageResult = blogService.queryHotBlog(current);
        return Result.ok(pageResult);
    }

    /**
     * 查询博客详情
     * 已同步
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Blog> queryBlogById(@PathVariable("id") Long id){
        log.info("查询博客详情：{}", id);
        Blog blog = blogService.queryBlogById(id);
        return Result.ok(blog);
    }

    /**
     * 查询博客点赞数
     * 已同步
     * @param id
     * @return
     */
    @GetMapping("/likes/{id}")
    public Result<Integer> queryBlogLikes(@PathVariable("id") Long id){
        log.info("查询博客点赞数：{}", id);
        Integer likes = blogService.queryBlogLikes(id);
    return Result.ok();
    }



}
