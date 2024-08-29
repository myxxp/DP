package com.crabdp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.crabdp.common.PageResult;
import com.crabdp.dto.UserDTO;
import com.crabdp.entity.Blog;
import com.crabdp.entity.Follow;
import com.crabdp.entity.ScrollResult;
import com.crabdp.entity.User;
import com.crabdp.mapper.BlogMapper;
import com.crabdp.mapper.FollowMapper;
import com.crabdp.mapper.UserMapper;
import com.crabdp.service.BlogService;
import com.crabdp.utils.SystemConstants;
import com.crabdp.utils.UserHolder;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.crabdp.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.crabdp.utils.RedisConstants.FEED_KEY;

@Service
@Slf4j
public class BlogServiceImpl implements BlogService{

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FollowMapper followMapper;
    @Override
    public void likeBlog(Long id){
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.判断当前用户是否已经点赞
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null){
            //3.如果未点赞，可以点赞
            boolean isSuccess = blogMapper.incrementLikeCount(id);
            //3.2.保存用户到Redis的set集合 zadd key value score
            if (isSuccess){
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }

        } else {
            //4.如果已点赞，取消点赞
            //4.1.数据库点赞数 -1
            boolean isSuccess = blogMapper.decrementLikeCount(id);
            //4.2.把用户从Redis的set集合移除
            if (isSuccess){
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }

    }


    public PageResult queryBlog(Integer current) {
        //获取登录用户
        UserDTO user = UserHolder.getUser();
        PageHelper.startPage(current, SystemConstants.MAX_PAGE_SIZE);
        Page<Blog> page = blogMapper.pageQuery(user);


        //获取当前页数据
        long total = page.getTotal();
        List<Blog> records = page.getResult();

        return new PageResult(total, records);

    }
    /**
     * 查询热门博客
     * @param current
     * @return
     */

    public List<Blog> queryHotBlog(Integer current) {
        // 根据用户查询
        UserDTO user = UserHolder.getUser();
        PageHelper.startPage(current, SystemConstants.MAX_PAGE_SIZE);
        Page<Blog> page = blogMapper.queryHotBlog(user);
        // 获取当前页数据
        List<Blog> records = page.getResult();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return records;
    }
    /**
     * 查询博客详情
     * @param id
     * @return
     */

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userMapper.selectById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    /**
     * 查询博客是否被当前登录用户点赞
     * @param blog
     */
    private void isBlogLiked(Blog blog) {
        // 1.获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            // 用户未登录，无需查询是否点赞
            return;
        }
        Long userId = user.getId();
        // 2.判断当前登录用户是否已经点赞
        String key = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    /**
     * 查询博客详情
     * @param id
     * @return
     */
    public Blog queryBlogById(Long id) {
        // 1.查询blog
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw  new RuntimeException("笔记不存在！");
        }
        // 2.查询blog有关的用户
        queryBlogUser(blog);
        // 3.查询blog是否被点赞
        isBlogLiked(blog);
        return blog;
    }

    /**
     * 统计top5点赞用户
     * @param id
     * @return
     */

    public List<UserDTO> queryBlogLikes(Long id){
        String key = BLOG_LIKED_KEY + id;
        //统计top5点赞用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().reverseRange(key, 0, 4);
        if (top5 == null || top5.isEmpty()){
            return Collections.emptyList();
//            List<UserDTO> userDTOS = userMapper.selectByIds(idStr);
        }
        //2.解析用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());

        List<User> users = userMapper.selectByIds(ids);

        List<UserDTO> userDTOS = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        return userDTOS;


    }

    public long saveBlog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        boolean isSave =  blogMapper.saveBlog(blog);
        if (!isSave){
            throw new RuntimeException("新增笔记失败！");
        }

        List<Follow> follows = followMapper.queryFollows(user.getId());
        for (Follow follow : follows) {
            Long userId = follow.getUserId();
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }


        return blog.getId();
    }

    public PageResult queryBlogByUserId(Integer current, Long id) {
        // 根据用户查询
        PageHelper.startPage(current, SystemConstants.MAX_PAGE_SIZE);
        Page<Blog> page = blogMapper.queryBlogByUserId(id);
        // 获取当前页数据
        long total = page.getTotal();
        List<Blog> records = page.getResult();
        return new PageResult(total, records);
    }

    public ScrollResult queryBlogOfFollow(Long max, Integer offest) {
        Long userId = UserHolder.getUser().getId();
        String key = FEED_KEY + userId;

        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offest, 2);

        if (typedTuples == null || typedTuples.isEmpty()) {
            return null;
        }
        //解析数据
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            ids.add(Long.valueOf(tuple.getValue()));
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            }else {
                minTime = time;
                os = 1;
            }
        }

        List<Blog> blogs = blogMapper.queryBlogByIds(ids);

        for (Blog blog : blogs) {
            queryBlogUser(blog);
            isBlogLiked(blog);
        }

        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);

        return r;

    }



}
