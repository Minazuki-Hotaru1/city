package com.example.city.service;

import java.util.Map;

public interface EnterpriseService {

    Map<String, Object> login(String username, String password);

    Map<String, Object> registration(Map<String, Object> data);

    Map<String, Object> getAllApp(String EnId);

    Map<String, Object> appPass(String id);
}
