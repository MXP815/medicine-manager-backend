package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.DTO.SupplierDTO;
import com.medicine.medicine_manager_sysytem.entity.Supplier;
import com.medicine.medicine_manager_sysytem.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "供应商管理", description = "供应商信息管理接口")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/page")
    @Operation(summary = "分页查询供应商")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public Result<PageResult<Supplier>> page(PageQuery query, String keyword) {
        Page<Supplier> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Supplier> result = supplierService.page(page, keyword);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询供应商")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增供应商")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody SupplierDTO dto) {
        return Result.success(supplierService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新供应商")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SupplierDTO dto) {
        supplierService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/blacklist")
    @Operation(summary = "加入黑名单")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> addToBlacklist(@PathVariable Long id) {
        supplierService.addToBlacklist(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/blacklist")
    @Operation(summary = "移出黑名单")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> removeFromBlacklist(@PathVariable Long id) {
        supplierService.removeFromBlacklist(id);
        return Result.success();
    }

    @GetMapping("/expiring")
    @Operation(summary = "获取资质即将到期供应商")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public Result<List<Supplier>> getExpiringSuppliers(@RequestParam(defaultValue = "30") int days) {
        return Result.success(supplierService.getExpiringSuppliers(days));
    }
}
