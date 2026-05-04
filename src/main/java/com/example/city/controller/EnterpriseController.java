package com.example.city.controller;


import com.example.city.service.EnterpriseService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

//    @PutMapping("")
//    public ResponseEntity<?> putEnterprise(@RequestBody Map<String, String> data){
//        String username = data.get("username");
//        String password = data.get("password");
//        return null;
//
//    }

    //企业用户注册
    @PostMapping("/EnRegistration")
    public Map<String, Object> register(@RequestBody Map<String, Object> data) {
        return enterpriseService.registration(data);
    }

    //企业获取预约用户
    @GetMapping("/getAllApp")
    public Map<String, Object> getAllApp(@RequestParam String EnId) {
        return enterpriseService.getAllApp(EnId);
    }

    //企业更新用户状态
    @PutMapping("/appPass")
    public Map<String, Object> appPass(@RequestParam String userId) {
        return enterpriseService.appPass(userId);
    }


}
