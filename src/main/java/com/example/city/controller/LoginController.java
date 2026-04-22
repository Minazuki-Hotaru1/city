package com.example.city.controller;


import com.example.city.service.AdminService;
import com.example.city.service.EnterpriseService;
import com.example.city.Utils.JwtUtil;
import com.example.city.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class LoginController {

    @Resource
    private EnterpriseService enterpriseService;
    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        String userType = (String) data.get("userType");
        return switch (userType) {
            //管理员用户user1
            case "user1" -> adminService.login(username, password);
            case "user2" -> enterpriseService.login(username, password);
            case "user3" -> userService.login(username, password);
            default -> null;
        };
    }
}
