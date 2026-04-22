package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("enterprise_user")
@Data
public class Enterprise {
    @TableId(type = IdType.AUTO)
    private String id;
    private String username;
    private String password;
    @TableField("type_id")
    private String typeID;
    private String roles;
}




