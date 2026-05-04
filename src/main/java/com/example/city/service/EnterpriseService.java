package com.example.city.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.EnterpriseAppVO;

import java.util.Map;

public interface EnterpriseService {

    Map<String, Object> login(String username, String password);

    Map<String, Object> registration(Map<String, Object> data);

    Page<EnterpriseAppVO> getAllApp(String EnId, Long page, Long number);

    Map<String, Object> appPass(String id);
}
