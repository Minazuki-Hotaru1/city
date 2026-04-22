package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("type")
@Data
public class EnterpriseType {

    @TableId("type_id")
    private String typeID;
    @TableId("type_name")
    private String typeName;
}
