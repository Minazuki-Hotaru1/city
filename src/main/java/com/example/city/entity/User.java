package com.example.city.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private String id;
    private String username;
    private String password;
    private String address;
    private String latitude;
    private String longitude;
}
