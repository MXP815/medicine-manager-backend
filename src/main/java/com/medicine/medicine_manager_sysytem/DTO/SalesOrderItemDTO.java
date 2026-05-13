package com.medicine.medicine_manager_sysytem.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class SalesOrderItemDTO {

    private Long id;

    @NotNull(message = "药品不能为空")
    private Long medicineId;

    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于 0")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须大于 0")
    private Double unitPrice;
}
