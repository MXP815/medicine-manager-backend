package com.medicine.medicine_manager_sysytem.VO;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryVO {

    private Long id;

    private Long medicineId;

    private String medicineName;

    private String specification;

    private String batchNumber;

    private String warehouseLocation;

    private Integer quantity;

    private LocalDate expiryDate;

    private Integer warningStatus;

    private Integer minStock;

    private Integer status;

    private String storageTemperature;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
