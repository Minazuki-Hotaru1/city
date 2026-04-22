package com.example.city.service;

import com.example.city.entity.Enterprise;

import java.util.Map;

public interface EnterpriseService {
    Map<String, Object> login(String username, String password);
    Map<String, Object> registration(Map<String, Object> data);
}
