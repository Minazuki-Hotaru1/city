package com.example.city.service;

import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface UserService {

    Map<String, Object> userRegister(Map<String, Object> data);

    Map<String, Object> login(String username, String password);
}
