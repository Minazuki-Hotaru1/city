package com.example.city.controller;


import com.example.city.VO.AddressVO;
import com.example.city.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    //获取所有企业用户信息，在地图上显示
    @GetMapping("/userGetAllEn")
    public List<AddressVO> getAllEn() {
        return userService.getAllEn();
    }

    //判断用户能否预约的接口
    @PostMapping("/userReserveEnterprise")
    public Map<String, Object> userReserveEnterprise(@RequestParam String userId, String enterpriseId, String enterpriseType) {
        return userService.userReserveEnterprise(userId, enterpriseId, enterpriseType);
    }

    //用户预约的接口
    @PutMapping("/userReserveEnterpriseSuccess")
    public Map<String, Object> userReserveEnterpriseSuccess(@RequestBody Map<String, Object> data) {
        return userService.userReserveEnterpriseSuccess(data);
    }

}
