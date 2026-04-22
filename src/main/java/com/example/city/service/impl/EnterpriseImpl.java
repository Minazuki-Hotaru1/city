package com.example.city.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.city.entity.EnterpriseConfirm;
import com.example.city.entity.Enterprise;
import com.example.city.mapper.EnterpriseConfirmMapper;
import com.example.city.mapper.EnterpriseMapper;
import com.example.city.service.EnterpriseService;
import com.example.city.Utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class EnterpriseImpl implements EnterpriseService {

    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private EnterpriseConfirmMapper enterpriseConfirmMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Enterprise> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
                .eq("password", password);

        Enterprise enterprise = enterpriseMapper.selectOne(wrapper);
        if (enterprise == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        String token = jwtUtil.generateToken(
                enterprise.getUsername(),
                Collections.singletonMap("enterpriseID", enterprise.getId())
        );

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", enterprise.getUsername());
        result.put("enterpriseId", enterprise.getId());
        return result;

    }



    //企业用户注册
    @Override
    public Map<String, Object> registration(Map<String, Object> data) {
        Enterprise enterprise = enterpriseMapper.selectOne(new QueryWrapper<Enterprise>().eq("username", data.get("username")));
        EnterpriseConfirm co = enterpriseConfirmMapper.selectOne(new QueryWrapper<EnterpriseConfirm>().eq("username", data.get("username")));
        Map<String, Object> result = new HashMap<>();

        //判断账号重复(判断已在注册完毕的用户表)
        if (enterprise != null){
            result.put("success", false);
            result.put("message", "账号重复，请换个账号");
            return result;
        }

        //判断账号重复(判断还在审核的注册表)
        if (co != null){
            result.put("success", false);
            result.put("message", "该账号正在审核中，请勿重复注册");
            return result;
        }


        EnterpriseConfirm confirm = new EnterpriseConfirm();
        confirm.setUsername((String) data.get("username"));
        confirm.setPassword((String) data.get("password"));
        confirm.setTypeID((String) data.get("typeID"));
        confirm.setRoles((String) data.get("roles"));
        confirm.setAddress((String) data.get("address"));
        confirm.setHaveSeeIt("1");

        try {
            int i = enterpriseConfirmMapper.insert(confirm);
            result.put("success", true);
            result.put("message", "创建成功，请等待管理员审核账号");
        }catch (Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }


}
