package com.example.city.VO;

import lombok.Data;

@Data
public class AddressVO {
    private String id;
    private String enterpriseName;
    private String latitude;
    private String longitude;
    private String typeName;
    //获取状态
    //已预约人数
    private String reservedCount;
    //可预约的总人数
    private String reservationCapacity;
    //已在线人数
    private String onlineCount;
    //可容纳总人数
    private String onlineCapacity;
}
