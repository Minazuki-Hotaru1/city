package com.example.city.VO;


import lombok.Data;

//企业用户获取自己企业的预约状态的对象
@Data
public class EnterpriseAppVO {
    private String id;
    private String userId;
    private String userName;
    private String userAddress;
    private String appStartTime;
    private String appEndTime;
    private String appStatus;
}
