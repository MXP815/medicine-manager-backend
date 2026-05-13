package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.Inventory;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.mapper.MedicineMapper;
import com.medicine.medicine_manager_sysytem.service.InventoryService;
import com.medicine.medicine_manager_sysytem.VO.InventoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouse/stock")
@RequiredArgsConstructor
@Tag(name = "库存管理", description = "库存管理接口")
public class InventoryController {

    private final InventoryService inventoryService;
    private final MedicineMapper medicineMapper;

    @GetMapping("/list")
    @Operation(summary = "分页查询库存列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<PageResult<InventoryVO>> page(PageQuery query, String keyword) {
        Page<Inventory> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Inventory> result = inventoryService.page(page, null, keyword);
        
        List<InventoryVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        PageResult<InventoryVO> pageResult = PageResult.of(
                voList, 
                result.getTotal(), 
                result.getSize(), 
                result.getCurrent()
        );
        
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询库存详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<InventoryVO> getById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getById(id);
        return Result.success(convertToVO(inventory));
    }

    @PostMapping("/in")
    @Operation(summary = "入库操作")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<Void> stockIn(@RequestBody Map<String, Object> dto) {
        Long medicineId = Long.valueOf(dto.get("medicineId").toString());
        Integer quantity = Integer.valueOf(dto.get("quantity").toString());
        String batchNo = (String) dto.get("batchNo");
        inventoryService.stockIn(medicineId, quantity, batchNo);
        return Result.success();
    }

    @PostMapping("/out")
    @Operation(summary = "出库操作")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<Void> stockOut(@RequestBody Map<String, Object> dto) {
        Long medicineId = Long.valueOf(dto.get("medicineId").toString());
        Integer quantity = Integer.valueOf(dto.get("quantity").toString());
        String batchNo = (String) dto.get("batchNo");
        inventoryService.stockOut(medicineId, quantity, batchNo);
        return Result.success();
    }

    @PostMapping("/check")
    @Operation(summary = "库存盘点")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<Void> stockCheck(@RequestBody Map<String, Object> dto) {
        Long inventoryId = Long.valueOf(dto.get("inventoryId").toString());
        Integer actualQuantity = Integer.valueOf(dto.get("actualQuantity").toString());
        inventoryService.stockCheck(inventoryId, actualQuantity);
        return Result.success();
    }

    @GetMapping("/record")
    @Operation(summary = "查询库存变动记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public Result<PageResult<Object>> getStockRecordList(PageQuery query, @RequestParam Long inventoryId) {
        Page<Object> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Object> result = inventoryService.getStockRecordPage(inventoryId, page);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }
    
    private InventoryVO convertToVO(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        
        InventoryVO vo = new InventoryVO();
        vo.setId(inventory.getId());
        vo.setMedicineId(inventory.getMedicineId());
        vo.setBatchNumber(inventory.getBatchNumber());
        vo.setWarehouseLocation(inventory.getWarehouseLocation());
        vo.setQuantity(inventory.getQuantity());
        vo.setExpiryDate(inventory.getExpiryDate());
        vo.setWarningStatus(inventory.getWarningStatus());
        vo.setMinStock(inventory.getMinStock());
        vo.setStatus(inventory.getStatus());
        vo.setStorageTemperature(inventory.getStorageTemperature());
        vo.setCreateTime(inventory.getCreateTime());
        vo.setUpdateTime(inventory.getUpdateTime());
        
        // 查询药品信息
        Medicine medicine = medicineMapper.selectById(inventory.getMedicineId());
        if (medicine != null) {
            vo.setMedicineName(medicine.getName());
            vo.setSpecification(medicine.getSpecification());
        }
        
        return vo;
    }
}
