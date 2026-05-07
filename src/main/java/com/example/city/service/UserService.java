package com.example.city.service;

import com.example.city.VO.AddressVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface UserService {

    Map<String, Object> userRegister(Map<String, Object> data);

    Map<String, Object> login(String username, String password);

    List<AddressVO> getAllEn();

    Map<String, Object> userReserveEnterprise(String userId, String enterpriseId, String enterpriseType);

    Map<String, Object> userReserveEnterpriseSuccess(Map<String, Object> data);
}
