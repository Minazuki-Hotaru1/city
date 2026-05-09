package com.example.city.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.EnterpriseAppVO;
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

    //企业分页获取所有预约用户
    @GetMapping("/getAllApp")
    public Page<EnterpriseAppVO> getAllApp(@RequestParam String enId, @RequestParam long page, @RequestParam long number) {
        return enterpriseService.getAllApp(enId, page, number);
    }

    //企业分页获取待到场预约（仅状态为1）
    @GetMapping("/getPendingApp")
    public Page<EnterpriseAppVO> getPendingApp(@RequestParam String enId, @RequestParam long page, @RequestParam long number) {
        return enterpriseService.getPendingApp(enId, page, number);
    }

    //企业分页获取所有预约（状态1/2/3），按日期从新到旧排列
    @GetMapping("/getAllAppSorted")
    public Page<EnterpriseAppVO> getAllAppSorted(@RequestParam String enId, @RequestParam long page, @RequestParam long number) {
        return enterpriseService.getAllAppSorted(enId, page, number);
    }

    //企业更新用户状态
    @PutMapping("/appPass")
    public Map<String, Object> appPass(@RequestParam String userId) {
        return enterpriseService.appPass(userId);
    }

    //获取企业预约柱状图数据
    @GetMapping("/getAppointmentChart")
    public Map<String, Object> getAppointmentChart(@RequestParam String enId) {
        return enterpriseService.getAppointmentChart(enId);
    }

}
