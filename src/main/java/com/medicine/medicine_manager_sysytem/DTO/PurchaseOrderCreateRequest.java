package com.medicine.medicine_manager_sysytem.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseOrderCreateRequest {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private List<OrderItem> items;

    private String remark;

    private Long suggestionId;

    @Data
    public static class OrderItem {
        @NotNull(message = "药品ID不能为空")
        private Long medicineId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        private String batchNo;
    }
}
