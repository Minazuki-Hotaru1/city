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
    private String time;
    @TableField("app_status")
    //1：已预约但未到现场 2： 已预约并到现场 3：预约未到现场
    private String appStatus;
}
