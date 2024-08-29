package com.crabdp.mapper;

import com.crabdp.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @program: crabdp
 * @description:
 * @author: snow
 * @create: 2024-08-28 14:01
 **/
@Mapper
public interface FollowMapper {

    boolean save(Follow follow);

    boolean delete(Long userId, Long followUserId);

    boolean isFollow(Long userId, Long followUserId);

    List<Follow> queryFollows(Long id);
}
