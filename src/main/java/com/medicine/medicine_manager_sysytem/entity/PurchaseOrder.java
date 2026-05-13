package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long supplierId;

    private Integer status;

    private BigDecimal totalAmount;

    private Integer totalCount;

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime actualDeliveryDate;

    private String contractPath;

    private String invoicePath;

    private String remark;

    private Long creatorId;

    private Long approverId;

    private LocalDateTime approveTime;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
