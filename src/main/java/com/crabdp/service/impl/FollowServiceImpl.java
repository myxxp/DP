package com.crabdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crabdp.dto.UserDTO;
import com.crabdp.entity.Follow;
import com.crabdp.entity.User;
import com.crabdp.mapper.FollowMapper;
import com.crabdp.mapper.UserMapper;
import com.crabdp.service.FollowService;
import com.crabdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: crabdp
 * @description: FollowService实现类
 * @author: snow
 * @create: 2024-08-28 13:48
 **/
@Service
public class FollowServiceImpl implements FollowService {

    @Resource
    private FollowMapper followMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserMapper userMapper;

    @Override
    public void follow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        if (isFollow) {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSave =  followMapper.save(follow);
            if (isSave) {
                // 关注
                stringRedisTemplate.opsForSet().add("follows:" + userId, followUserId.toString());
            }
        } else {
            // 取消关注
            boolean isDelete = followMapper.delete(userId, followUserId);
            if (isDelete) {
                stringRedisTemplate.opsForSet().remove("follows:" + userId, followUserId.toString());
            }
        }

    }

    @Override
    public Boolean isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();

        Boolean isFollowResult = followMapper.isFollow(userId, followUserId);
        return isFollowResult;

    }

    public List<UserDTO> followCommons(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;

        //2交集
        String key2 = "follows:" + id;

        Set<String> set = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (set == null || set.isEmpty()) {
            return Collections.emptyList();
        }
        //3解析id集合
        List<Long> followUserIds = set.stream().map(Long::valueOf).collect(Collectors.toList());

        //4.查询用户

        List<User> users = userMapper.selectByIds(followUserIds);

        List<UserDTO> userDTOS = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return userDTOS;
        // 通用关注
    }


}
