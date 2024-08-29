package com.crabdp.controller;

import com.crabdp.common.Result;
import com.crabdp.dto.UserDTO;
import com.crabdp.service.FollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: crabdp
 * @description: 粉丝关注类
 * @author: snow
 * @create: 2024-08-28 13:23
 **/

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {

        followService.follow(followUserId, isFollow);
        return Result.ok();
    }


    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        Boolean isFollowResult = followService.isFollow(followUserId);
        return Result.ok(isFollowResult);
    }

    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id) {
        List<UserDTO> userDTOS = followService.followCommons(id);
        return Result.ok(userDTOS);
    }



}
