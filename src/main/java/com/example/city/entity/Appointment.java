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
    private String appStatus;
}
