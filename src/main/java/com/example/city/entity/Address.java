package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("address")
@Data
public class Address {

    @TableId(type = IdType.AUTO)
    private String id;
    @TableField("enterprise_id")
    private String enterpriseID;
    private String latitude;
    private String longitude;

}
