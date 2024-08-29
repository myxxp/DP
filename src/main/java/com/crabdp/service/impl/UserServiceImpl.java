package com.crabdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.crabdp.config.ClientInputException;
import com.crabdp.dto.LoginFormDTO;
import com.crabdp.dto.UserDTO;
import com.crabdp.entity.User;
import com.crabdp.mapper.UserMapper;
import com.crabdp.service.UserService;
import com.crabdp.utils.RegexUtils;
import com.crabdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.crabdp.utils.RedisConstants.*;


@Service
@Slf4j

public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendCode(String phone, HttpSession session) {
        // 1.校验手机号

        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.不符合，返回错误信息
            log.error("phone:{}", phone);
            throw new ClientInputException("手机号格式不正确");
        }
        //3.符合，发送验证码
        String code = RandomUtil.randomNumbers(6);

        //4.保存验证码到session
//        session.setAttribute("code", code);
        //4.保存验证码到redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //5.发送验证码
        log.info("向手机号{}发送验证码：{}", phone, code);


    }

    /**
     * 登录校验
     *
     * @param loginFormDTO 登录表单
     * @param session      session
     */

    @Override
    public String login(LoginFormDTO loginFormDTO, HttpSession session) {

        String phone = loginFormDTO.getPhone();
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.不符合，返回错误信息
            throw new ClientInputException("手机号格式不正确");
        }
        //3.校验验证码
//        String code = (String) session.getAttribute("code");
        //3.从redis获取验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (!loginFormDTO.getCode().equals(cacheCode) || cacheCode == null) {
            //4.不符合，返回错误信息
            throw new ClientInputException("验证码不正确");
        }
        //5.判断用户是否存在
        User user = userMapper.getByPhone(phone);
        if (user == null) {
            //6.不存在，注册用户
            log.info("手机号{}未注册", phone);
            user = createUserWithPhone(phone);
        }

//        log.info("手机号{}登录成功", loginFormDTO.getPhone());
//        //7.保存用户信息到session
//        session.setAttribute("user", user);
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        System.out.println(userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? "null" : fieldValue.toString()));

        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        log.info("用户{}登录成功，token:{}", phone, token);
        return token;

    }

    /**
     * 创建用户
     *
     * @param phone 手机号
     * @return 用户
     */
    private User createUserWithPhone(String phone) {
        User user = User.builder()
                .phone(phone)
                .nickName("用户" + RandomUtil.randomNumbers(6))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return user;
    }

    @Override
    public UserDTO getById(Long userId) {
        User user = userMapper.getById(userId);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return userDTO;
    }

    public void sign() {
        // 1.获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.写入Redis SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    }


    public  Integer signCount() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        //getDayOfMonth 返回当天在月份的某一天
        int day0fMonth = now.getDayOfMonth();

        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()//位图操作子命令
                        .get(BitFieldSubCommands.BitFieldType.unsigned(day0fMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            return 0;
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return 0;
        }
        int count = 0;
        while (true) {
            if ((num & 1) == 0) {
                break;
            }else {
                count++;
            }

            num >>>= 1;
        }
        return count;
    }

    //查询所有用户
    @Override
    public List<User> list() {

        return userMapper.list();
    }

    public void logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return;
        }
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.delete(tokenKey);

    }


}
