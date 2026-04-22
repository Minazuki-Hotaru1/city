package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("enterprise_confirm")
@Data
public class EnterpriseConfirm {


    @TableId(type = IdType.AUTO)
    private String id;
    private String username;
    private String password;
    private String address;
    @TableField("type_id")
    private String typeID;
    private String roles;
    //0/1判断，默认0，表示管理员没看到，当管理员查询到该数据时，改为1，表示被看到了，拒绝为2
    @TableField("have_see_it")
    private String haveSeeIt;
}
