package com.medicine.medicine_manager_sysytem.VO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderVO {

    private Long id;

    private String orderNo;

    private Long supplierId;

    private String supplierName;

    private Integer status;

    private BigDecimal totalAmount;

    private Integer totalCount;

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime actualDeliveryDate;

    private String contractPath;

    private String invoicePath;

    private String remark;

    private Long creatorId;

    private String creatorName;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
