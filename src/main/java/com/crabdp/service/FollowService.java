package com.crabdp.service;

import com.crabdp.dto.UserDTO;

import java.util.List;

/**
 * @program: crabdp
 * @description:
 * @author: snow
 * @create: 2024-08-28 13:27
 **/
public interface FollowService {

    void follow(Long followUserId, Boolean isFollow);

    Boolean isFollow(Long followUserId);

    List<UserDTO> followCommons(Long id);
}
