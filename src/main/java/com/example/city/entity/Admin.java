package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("admin")
@Data
public class Admin {

    @TableId(type = IdType.AUTO)
    private String id;
    private String username;
    private String password;
    @TableField("login_time")
    private String loginTime;
}
