package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.OperationLog;
import com.medicine.medicine_manager_sysytem.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "操作日志", description = "系统操作日志接口")
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    @GetMapping("/page")
    @Operation(summary = "分页查询操作日志")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<OperationLog>> page(
            PageQuery query,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<OperationLog> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(OperationLog::getUsername, keyword)
                    .or()
                    .like(OperationLog::getOperation, keyword));
        }

        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }

        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> result = operationLogMapper.selectPage(page, wrapper);

        return Result.success(PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getSize(),
                result.getCurrent()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询日志详情")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OperationLog> getById(@PathVariable Long id) {
        return Result.success(operationLogMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除日志")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        operationLogMapper.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除日志")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDelete(@RequestBody java.util.List<Long> ids) {
        operationLogMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空日志")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> clear() {
        operationLogMapper.delete(null);
        return Result.success();
    }
}
