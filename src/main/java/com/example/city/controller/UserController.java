package com.example.city.controller;


import com.example.city.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class UserController {

    @Resource
    private UserService userService;

    //普通用户注册账号
    @PostMapping("/userRegister")
    public Map<String, Object> login(@RequestBody Map<String, Object> data) {
        return userService.userRegister(data);
    }



}
