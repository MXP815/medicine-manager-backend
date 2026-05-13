package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_medicine")
public class Medicine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String specification;

    private String manufacturer;

    private String unit;

    private Integer shelfLife;

    private String storageCondition;

    private String category;

    private String form;

    private String approvalNumber;

    private String barcode;

    private String prescriptionType;

    private Double purchasePrice;

    private Double salePrice;

    private Integer minStock;

    private Integer status;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
