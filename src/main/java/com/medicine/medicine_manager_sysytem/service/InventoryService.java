package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.entity.Inventory;
import com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO;

import java.util.List;

public interface InventoryService {

    Page<Inventory> page(Page<Inventory> page, Long medicineId, String batchNumber);

    Page<Object> getStockRecordPage(Long inventoryId, Page<Object> page);

    Inventory getById(Long id);

    List<Inventory> getByMedicineIdAndAvailable(Long medicineId);

    List<InventoryWarningVO> getWarningInventory();

    void adjustStock(Long inventoryId, Integer quantity, String reason);

    void updateWarningStatus();

    void stockIn(Long medicineId, Integer quantity, String batchNo);

    void stockOut(Long medicineId, Integer quantity, String batchNo);

    void stockCheck(Long inventoryId, Integer actualQuantity);

    List<InventoryWarningVO> getExpiringInventory(int days);
    
    void updateMinStock(Long medicineId, Integer minStock);
}
