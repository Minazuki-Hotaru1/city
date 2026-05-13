package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("appointment")
@Data
public class Appointment {
    @TableId(type = IdType.AUTO)
    private String id;
    @TableField("user_id")
    private String userID;
    @TableField("enterprise_id")
    private String enterpriseID;
    @TableField("app_start_time")
    private String startTime;
    @TableField("app_end_time")
    private String endTime;
    @TableField("app_status")
    //1：已预约未到场 2：已到场 3：预约未到场 4：已离开
    private String appStatus;
    @TableField("remarks")
    private String remarks;
}
