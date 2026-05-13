package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_customer")
public class Customer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private String deliveryAddress;

    private BigDecimal creditLimit;

    private Integer customerLevel;

    private Integer totalOrders;

    private BigDecimal totalAmount;

    private Integer points;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
