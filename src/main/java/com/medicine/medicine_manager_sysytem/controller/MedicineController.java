package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.DTO.MedicineDTO;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/basic/drug")
@RequiredArgsConstructor
@Tag(name = "药品信息管理", description = "药品基础数据管理接口")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping("/list")
    @Operation(summary = "分页查询药品列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public Result<PageResult<Medicine>> page(PageQuery query, String keyword) {
        Page<Medicine> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Medicine> result = medicineService.page(page, keyword);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询药品详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public Result<Medicine> getById(@PathVariable Long id) {
        return Result.success(medicineService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增药品")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody MedicineDTO dto) {
        return Result.success(medicineService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新药品信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MedicineDTO dto) {
        medicineService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除药品")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return Result.success();
    }

    @GetMapping("/category")
    @Operation(summary = "药品分类")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public Result<List<String>> getCategories() {
        return Result.success(List.of("处方药", "非处方药", "中药饮片", "医疗器械"));
    }

    @GetMapping("/form")
    @Operation(summary = "药品剂型")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public Result<List<String>> getForms() {
        return Result.success(List.of("片剂", "胶囊剂", "颗粒剂", "口服液", "注射剂"));
    }

    @GetMapping("/storage")
    @Operation(summary = "存储条件")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public Result<List<String>> getStorageConditions() {
        return Result.success(List.of("常温", "阴凉", "冷藏", "冷冻"));
    }
}
