package com.example.city.VO;

import lombok.Data;

@Data
public class UserVO {
    private String id;
    private String username;
    private String address;
    private String latitude;
    private String longitude;
    private String appID;
    private String appSecret;
    private String startTime;
    private String endTime;
    private Integer appointmentCount;
}
