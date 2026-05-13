package com.medicine.medicine_manager_sysytem.VO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InventoryWarningVO {

    private Long inventoryId;
    private Long medicineId;
    private String medicineName;
    private String specification;
    private String batchNumber;
    private String warehouseLocation;
    private Integer quantity;
    private Integer minStock;
    private LocalDate expiryDate;
    private Integer warningStatus;
    private Integer daysToExpiry;
    private String warningType;
    private String warningLevel;
    private String warningMessage;
}
