package com.example.city.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.mapper.UserMapper;
import com.example.city.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.example.city.entity.User;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService{


    @Resource
    private UserMapper userMapper;

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
        User user = new User();
        user.setUsername(data.get("username").toString());
        user.setPassword(data.get("password").toString());
        user.setAddress(data.get("address").toString());

        GetLatAndLong  gLAL = new GetLatAndLong();
        Map map = gLAL.getLatAndLong(data.get("address").toString());


        return Map.of();
    }
}
