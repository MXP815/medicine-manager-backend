package com.medicine.medicine_manager_sysytem.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class SalesOrderDTO {

    private Long id;

    @NotNull(message = "客户不能为空")
    private Long customerId;

    private String paymentMethod;

    private String deliveryMethod;

    private String prescriptionPath;

    private String remark;

    @NotEmpty(message = "订单明细不能为空")
    private List<SalesOrderItemDTO> items;
}
