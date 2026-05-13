package com.medicine.medicine_manager_sysytem.controller;

import com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.exception.BusinessException;
import com.medicine.medicine_manager_sysytem.service.InventoryService;
import com.medicine.medicine_manager_sysytem.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse/warning")
@RequiredArgsConstructor
@Tag(name = "库存预警", description = "库存预警管理接口")
public class WarehouseWarningController {

    private final InventoryService inventoryService;
    private final MedicineService medicineService;

    @GetMapping("/stock")
    @Operation(summary = "库存不足预警")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<List<InventoryWarningVO>> stockWarning() {
        List<InventoryWarningVO> warnings = inventoryService.getWarningInventory();
        return Result.success(warnings);
    }

    @GetMapping("/expiry")
    @Operation(summary = "效期预警")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<List<InventoryWarningVO>> expiryWarning(@RequestParam(defaultValue = "30") int days) {
        List<InventoryWarningVO> warnings = inventoryService.getExpiringInventory(days);
        return Result.success(warnings);
    }

    @GetMapping("/statistics")
    @Operation(summary = "库存统计")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalItems", 0);
        data.put("warningItems", 0);
        data.put("expiredItems", 0);
        return Result.success(data);
    }

    @PostMapping("/threshold")
    @Operation(summary = "设置库存阈值")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> setThreshold(@RequestParam Long medicineId,
                                      @RequestParam Integer minStock) {
        // 更新药品的最小库存阈值
        Medicine medicine = medicineService.getById(medicineId);
        if (medicine == null) {
            throw new BusinessException("药品不存在");
        }
        
        medicine.setMinStock(minStock);
        medicineService.updateById(medicine);
        
        // 同时更新库存记录的阈值
        inventoryService.updateMinStock(medicineId, minStock);
        
        return Result.success();
    }
}
