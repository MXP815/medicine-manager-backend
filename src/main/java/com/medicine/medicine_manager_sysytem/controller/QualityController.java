package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.QualityAcceptance;
import com.medicine.medicine_manager_sysytem.mapper.PurchaseOrderItemMapper;
import com.medicine.medicine_manager_sysytem.service.InventoryService;
import com.medicine.medicine_manager_sysytem.service.MedicineService;
import com.medicine.medicine_manager_sysytem.service.QualityAcceptanceService;
import com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
@Tag(name = "质量管理", description = "GSP 质量管理接口")
public class QualityController {

    private final QualityAcceptanceService qualityAcceptanceService;
    private final InventoryService inventoryService;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final MedicineService medicineService;

    // ==================== 药品验收 ====================
    
    @GetMapping("/acceptance/list")
    @Operation(summary = "分页查询验收记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<PageResult<QualityAcceptance>> page(PageQuery query) {
        Page<QualityAcceptance> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<QualityAcceptance> result = qualityAcceptanceService.page(page);
        
        fillMedicineNames(result.getRecords());
        
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @GetMapping("/acceptance/{id}")
    @Operation(summary = "根据 ID 查询验收详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<QualityAcceptance> getById(@PathVariable Long id) {
        return Result.success(qualityAcceptanceService.getById(id));
    }

    @PostMapping("/acceptance")
    @Operation(summary = "创建验收记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Long> create(@Valid @RequestBody QualityAcceptance acceptance) {
        qualityAcceptanceService.save(acceptance);
        return Result.success(acceptance.getId());
    }

    @PutMapping("/acceptance/{id}/approve")
    @Operation(summary = "审核通过")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approve(@PathVariable Long id) {
        qualityAcceptanceService.approve(id, 1L);
        return Result.success();
    }

    @PutMapping("/acceptance/{id}/reject")
    @Operation(summary = "审核拒绝")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        qualityAcceptanceService.reject(id, reason);
        return Result.success();
    }

    @GetMapping("/acceptance/pending")
    @Operation(summary = "待验收列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<List<QualityAcceptance>> getPendingAcceptance() {
        LambdaQueryWrapper<QualityAcceptance> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(QualityAcceptance::getStatus, 0, 1)
               .orderByDesc(QualityAcceptance::getCreateTime);
        List<QualityAcceptance> list = qualityAcceptanceService.list(wrapper);
        
        fillMedicineNames(list);
        
        return Result.success(list);
    }

    @GetMapping("/first-business/list")
    @Operation(summary = "首营企业列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Object> firstBusinessList(PageQuery query) {
        // TODO: 实现首营企业查询，需要创建首营企业实体类
        return Result.success(null);
    }

    @PutMapping("/first-business/{id}/audit")
    @Operation(summary = "首营企业审核")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditFirstBusiness(@PathVariable Long id, @RequestParam Boolean passed) {
        // TODO: 实现首营企业审核逻辑
        return Result.success();
    }

    @GetMapping("/first-product/list")
    @Operation(summary = "首营品种列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Object> firstProductList(PageQuery query) {
        // TODO: 实现首营品种查询，需要创建首营品种实体类
        return Result.success(null);
    }

    @PutMapping("/first-product/{id}/audit")
    @Operation(summary = "首营品种审核")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditFirstProduct(@PathVariable Long id, @RequestParam Boolean passed) {
        // TODO: 实现首营品种审核逻辑
        return Result.success();
    }

    @GetMapping("/expired/list")
    @Operation(summary = "过期药品列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Object> expiredList(PageQuery query) {
        // 查询过期药品（通过效期预警）
        List<InventoryWarningVO> expiredList = inventoryService.getExpiringInventory(0);
        return Result.success(expiredList);
    }

    @PostMapping("/expired/destroy")
    @Operation(summary = "销毁申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Void> applyDestroy(@RequestBody Map<String, Object> dto) {
        // TODO: 实现销毁申请逻辑，创建销毁记录
        return Result.success();
    }

    @PostMapping("/expired/apply")
    @Operation(summary = "销毁申请 (兼容前端)")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Void> applyDestroyAlias(@RequestBody Map<String, Object> dto) {
        return applyDestroy(dto);
    }

    @PutMapping("/expired/{id}/confirm")
    @Operation(summary = "确认销毁")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> confirmDestroy(@PathVariable Long id) {
        // TODO: 实现确认销毁逻辑，更新销毁记录状态
        return Result.success();
    }

    @GetMapping("/destroy/list")
    @Operation(summary = "销毁记录列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Object> destroyList(PageQuery query) {
        // TODO: 实现销毁记录查询
        return Result.success(null);
    }

    @GetMapping("/statistics")
    @Operation(summary = "质量统计数据")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalAcceptance", 0);
        data.put("qualifiedCount", 0);
        data.put("unqualifiedCount", 0);
        return Result.success(data);
    }

    private void fillMedicineNames(List<QualityAcceptance> records) {
        if (records == null || records.isEmpty()) return;
        
        for (QualityAcceptance acceptance : records) {
            if (acceptance.getPurchaseOrderItemId() != null) {
                try {
                    var item = purchaseOrderItemMapper.selectById(acceptance.getPurchaseOrderItemId());
                    if (item != null && item.getMedicineId() != null) {
                        acceptance.setMedicineId(item.getMedicineId());
                        var medicine = medicineService.getById(item.getMedicineId());
                        if (medicine != null) {
                            acceptance.setMedicineName(medicine.getName());
                        }
                    }
                } catch (Exception e) {
                    log.warn("填充药品名称失败，验收ID: {}", acceptance.getId(), e);
                }
            }
        }
    }
}
