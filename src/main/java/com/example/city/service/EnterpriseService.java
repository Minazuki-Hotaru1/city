package com.example.city.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.EnterpriseAppVO;

import java.util.Map;

public interface EnterpriseService {

    Map<String, Object> login(String username, String password);

    Map<String, Object> registration(Map<String, Object> data);

    Page<EnterpriseAppVO> getAllApp(String EnId, Long page, Long number);

    Page<EnterpriseAppVO> getPendingApp(String enId, Long page, Long number);

    Page<EnterpriseAppVO> getAllAppSorted(String enId, Long page, Long number);

    Map<String, Object> appPass(String appointmentId);

    Map<String, Object> getAppointmentChart(String enId);

    Map<String, Object> getEnterpriseProfile(String enId);

    Map<String, Object> updateEnterpriseAddress(Map<String, Object> data);

    Map<String, Object> updateEnterprisePassword(Map<String, Object> data);

    Long getNewAppointmentCount(String enId);
}
