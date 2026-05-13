package com.medicine.medicine_manager_sysytem.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderDTO {

    private Long id;

    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    private LocalDateTime expectedDeliveryDate;

    private String contractPath;

    private String remark;

    @NotEmpty(message = "订单明细不能为空")
    private List<PurchaseOrderItemDTO> items;
}
