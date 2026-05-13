package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_quality_acceptance")
public class QualityAcceptance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long purchaseOrderId;

    private Long purchaseOrderItemId;

    private Long medicineId;

    @TableField(exist = false)
    private String medicineName;

    private String batchNumber;

    private Integer checkQuantity;

    private Integer qualifiedQuantity;

    private Integer unqualifiedQuantity;

    private Integer acceptanceResult;

    private Integer status;

    private String qualityReportPath;

    private String rejectionReason;

    private Long checkerId;

    private LocalDateTime checkTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
