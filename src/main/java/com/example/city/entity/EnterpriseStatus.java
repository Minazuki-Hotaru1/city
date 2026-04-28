package com.example.city.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("enterprise_status")
@Data
public class EnterpriseStatus {
    @TableId(type = IdType.AUTO)
    private String id;
    @TableField("enterprise_id")
    private String enterpriseID;
    @TableField("reserved_count")
    private String reservedCount;
    @TableField("reservation_capacity")
    private String reservationCapacity;

    @TableField("online_count")
    private String onlineCount;
    @TableField("online_capacity")
    private String onlineCapacity;
}
