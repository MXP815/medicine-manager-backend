package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.DTO.SalesOrderDTO;
import com.medicine.medicine_manager_sysytem.entity.SalesOrder;
import com.medicine.medicine_manager_sysytem.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sales/order")
@RequiredArgsConstructor
@Tag(name = "销售订单管理", description = "销售订单管理接口")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping("/list")
    @Operation(summary = "分页查询销售订单")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<PageResult<SalesOrder>> page(PageQuery query,
                                               @RequestParam(required = false) Long customerId,
                                               @RequestParam(required = false) Integer status) {
        Page<SalesOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<SalesOrder> result = salesOrderService.page(page, customerId, status);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询销售订单详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<SalesOrder> getById(@PathVariable Long id) {
        return Result.success(salesOrderService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建销售订单")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Long> create(@Valid @RequestBody SalesOrderDTO dto, Principal principal) {
        return Result.success(salesOrderService.create(dto, 1L));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新销售订单")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SalesOrderDTO dto) {
        salesOrderService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除销售订单")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Void> delete(@PathVariable Long id) {
        salesOrderService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核销售订单")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approve(@PathVariable Long id) {
        salesOrderService.approve(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消销售订单")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Void> cancel(@PathVariable Long id) {
        salesOrderService.cancel(id);
        return Result.success();
    }

    @PutMapping("/{id}/prescription")
    @Operation(summary = "绑定处方")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Void> bindPrescription(@PathVariable Long id, @RequestParam String prescriptionNo) {
        salesOrderService.bindPrescription(id, prescriptionNo);
        return Result.success();
    }

    @GetMapping("/statistics")
    @Operation(summary = "销售统计")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", salesOrderService.countTotalOrders());
        data.put("pendingOrders", salesOrderService.countPendingOrders());
        data.put("completedOrders", salesOrderService.countCompletedOrders());
        
        // 将 BigDecimal 转换为 String，避免类型推断问题
        var totalAmount = salesOrderService.countTotalAmount();
        data.put("totalAmount", totalAmount != null ? totalAmount.toString() : "0");
        
        return Result.success(data);
    }

    @GetMapping("/drug/barcode")
    @Operation(summary = "通过条形码查询药品")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Map<String, Object>> getDrugByBarcode(@RequestParam String barcode) {
        Map<String, Object> drugInfo = salesOrderService.getDrugByBarcode(barcode);
        return drugInfo != null ? Result.success(drugInfo) : Result.error("药品不存在");
    }
}
