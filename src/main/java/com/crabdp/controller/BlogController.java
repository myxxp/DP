package com.crabdp.controller;

import com.crabdp.common.PageResult;
import com.crabdp.common.Result;
import com.crabdp.dto.UserDTO;
import com.crabdp.entity.Blog;
import com.crabdp.entity.ScrollResult;
import com.crabdp.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog")
@Slf4j

public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 保存博客
     * 已同步
     * @param blog
     * @return
     */
    @PostMapping
    public Result savaBlog(@RequestBody Blog blog) {
        long id = blogService.saveBlog(blog);
        return Result.ok(id);

    }

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

    public Result queryBlog(@RequestParam(value = "current", defaultValue = "1") Integer current){
        log.info("查询我的博客：{}", current);
        PageResult pageResult = blogService.queryBlog(current);
        return Result.ok(pageResult.getRecords());
    }

    /**
     * 查询热门博客
     * 已同步
     * @param
     * @return
     */
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current){
        log.info("查询热门博客：{}", current);
        List<Blog> records = blogService.queryHotBlog(current);
        return Result.ok(records);
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
     * 点赞排行榜功能
     * 已同步
     * @param id
     * @return
     */
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id){
        log.info("查询博客点赞数：{}", id);
        List<UserDTO> userDTOS = blogService.queryBlogLikes(id);
    return Result.ok(userDTOS);
    }


    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        PageResult pageResult = blogService.queryBlogByUserId(current, id);
        return Result.ok(pageResult.getRecords());
    }


    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
            ScrollResult r =  blogService.queryBlogOfFollow(max, offset);
            return Result.ok(r);

    }

}
