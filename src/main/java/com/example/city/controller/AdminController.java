package com.example.city.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.AddressVO;
import com.example.city.VO.ConfirmVO;
import com.example.city.VO.EnterpriseVO;
import com.example.city.VO.UserVO;
import com.example.city.entity.*;
import com.example.city.mapper.EnterpriseConfirmMapper;
import com.example.city.mapper.EnterpriseMapper;
import com.example.city.service.AdminService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class AdminController {

    @Resource
    private AdminService adminService;
    @Autowired
    private EnterpriseConfirmMapper confirmMapper;
    @Autowired
    private EnterpriseMapper enterpriseMapper;

//    @PostMapping("/login")
//    public Map<String, Object> login(@RequestBody Map<String, String> data) {
//        String username = data.get("username");
//        String password = data.get("password");
//        return adminService.login(username, password);
//    }

    @GetMapping("/getConfirm")
    public Page<ConfirmVO> getConfirm(@RequestParam long page,
                                      @RequestParam long number,
                                      @RequestParam(defaultValue = "pending") String reviewStatus) {
        return adminService.getConfirm(page, number, reviewStatus);

    }

    @GetMapping("/getNewConfirmCount")
    public Map<String, Object> getNewConfirmCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", adminService.getNewConfirmCount());
        return result;
    }

    //测试
    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("username", claims.getSubject());
        result.put("adminId", claims.get("adminId"));
        return result;
    }

    //企业账号审核通过
    @GetMapping("/approved")
    public Map<String, Object> approved(@RequestParam String id) {
        return adminService.approved(id);
    }

    //企业账号审核不通过
    @GetMapping("/unApproved")
    public Map<String, Object> unApproved(@RequestParam String id) {
        return adminService.unApproved(id);
    }


    @GetMapping("/getAddress")
    public List<AddressVO> getAddress() {
        return adminService.getAddress();
    }

    //获取一列的账号
    @GetMapping("/getOneConfirm")
    public EnterpriseConfirm getOneConfirm(@RequestParam String id) {
        return confirmMapper.selectOne(new QueryWrapper<EnterpriseConfirm>().eq("id", id));
    }

    //分页获取所有的企业用户
    @GetMapping("/getEnterprise")
    public Page<EnterpriseVO> getAllEnterprise(@RequestParam long page, long number) {
        return adminService.getEnterprise(page, number);
    }

    //管理员用户获取企业的状态
    @GetMapping("/getEnStatus")
    public Map<String, Object> getEnStatus(@RequestParam String id) {
        return adminService.getEnStatus(id);
    }

    @GetMapping("/getAllUser")
    public List<Map<String, Object>> getAllUser() {
        return adminService.getAllUser();
    }

    @GetMapping("/getAllUserPage")
    Page<UserVO> getAllUserPage(@RequestParam long page, long number) {
        return adminService.getAllUserPage(page, number);
    }

    @GetMapping("/getUserAppointments")
    List<Map<String, Object>> getUserAppointments(@RequestParam String userId) {
        return adminService.getUserAppointments(userId);
    }

    @GetMapping("/getAdminProfile")
    Map<String, Object> getAdminProfile(@RequestParam String adminId) {
        return adminService.getAdminProfile(adminId);
    }

    @PutMapping("/updateAdminPassword")
    Map<String, Object> updateAdminPassword(@RequestBody Map<String, Object> data) {
        return adminService.updateAdminPassword(data);
    }
}
