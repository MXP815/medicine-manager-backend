package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_inventory")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long medicineId;

    private String batchNumber;

    private String warehouseLocation;

    private Integer quantity;

    private LocalDate expiryDate;

    private Integer warningStatus;

    private Integer minStock;

    private Integer status;

    private String storageTemperature;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
