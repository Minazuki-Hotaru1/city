package com.example.city.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.AddressVO;
import com.example.city.VO.ConfirmVO;
import com.example.city.VO.EnterpriseVO;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> login(String username, String password);

    Page<ConfirmVO> getConfirm(long page, long number);

    Map<String, Object> approved(String id);

    List<AddressVO> getAddress();

    Map<String, Object> unApproved(String id);

    Page<EnterpriseVO> getEnterprise(long page, long number);


}
