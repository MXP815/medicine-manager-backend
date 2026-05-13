package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_supplier")
public class Supplier {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String businessScope;

    private String licenseNumber;

    private String licensePath;

    private LocalDate licenseExpiryDate;

    private String cooperationLevel;

    private Integer rating;

    private Boolean blacklisted;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
