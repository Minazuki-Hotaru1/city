package com.example.city.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.entity.EnterpriseConfirm;
import com.example.city.mapper.EnterpriseConfirmMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@CrossOrigin
public class TestController {


    @Resource
    private EnterpriseConfirmMapper confirmMapper;

    @GetMapping("/test")
    public List<EnterpriseConfirm> test(){
        Page<EnterpriseConfirm> page = new Page<>(1, 10);
        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        wrapper.eq("username", "");

        Page<EnterpriseConfirm> result = confirmMapper.selectPage(page, wrapper);
        return result.getRecords();
    }

}
