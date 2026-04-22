package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("type")
@Data
public class Type {
    @TableField("type_id")
    private String typeID;
    @TableField("type_name")
    private String typeName;
}
