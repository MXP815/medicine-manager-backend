package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_finance_transaction")
public class FinanceTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transactionNo;

    private Integer businessType;

    private Long relatedOrderId;

    private Long customerId;

    private Long supplierId;

    private BigDecimal amount;

    private Integer transactionType;

    private String paymentMethod;

    private String source;

    private String sourceType;

    private Integer status;

    private LocalDateTime transactionTime;

    private String remark;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
