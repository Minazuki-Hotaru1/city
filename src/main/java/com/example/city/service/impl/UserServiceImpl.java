package com.example.city.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.Utils.JwtUtil;
import com.example.city.entity.Admin;
import com.example.city.mapper.UserMapper;
import com.example.city.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.example.city.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService{


    @Resource
    private UserMapper userMapper;
    @Resource
    private GetLatAndLong getLatAndLong;
    @Resource
    private JwtUtil jwtUtil;


    //普通用户注册方法
    @Override
    public Map<String, Object> userRegister(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username",data.get("username"));
        if(userMapper.selectCount(wrapper)>0){
            result.put("success", false);
            result.put("message", "该账号已有用户注册");
            return result;
        }

        try {
            User user = new User();
            user.setUsername(data.get("username").toString());
            user.setPassword(data.get("password").toString());
            user.setAddress(data.get("address").toString());
            //获取用户传入的地址转变为经纬度
            Map map = getLatAndLong.getLatAndLong(data.get("address").toString());
            //纬度 横着的
            user.setLatitude(map.get("lat").toString());
            //经度 竖着的
            user.setLongitude(map.get("lng").toString());


            userMapper.insert(user);
            result.put("success", true);
            result.put("message", "注册成功，请返回登录界面进行登录");
            return result;
        }catch (Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    //登录的方法
    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
                .eq("password", password);

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                Collections.singletonMap("userID", user.getId())
        );


        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("userID", user.getId());
        return result;
    }
}
