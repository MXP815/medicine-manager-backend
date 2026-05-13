package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.SystemConfig;
import com.medicine.medicine_manager_sysytem.mapper.SystemConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "系统配置", description = "系统配置管理接口")
public class SystemConfigController {

    private final SystemConfigMapper systemConfigMapper;

    @GetMapping("/list")
    @Operation(summary = "查询所有配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SystemConfig>> list() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        return Result.success(configs);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类查询配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SystemConfig>> getByCategory(@PathVariable String category) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getCategory, category);
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        return Result.success(configs);
    }

    @GetMapping("/value/{key}")
    @Operation(summary = "根据 Key 查询配置值")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> getValue(@PathVariable String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        return Result.success(config != null ? config.getConfigValue() : null);
    }

    @PostMapping
    @Operation(summary = "新增配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@RequestBody SystemConfig config) {
        systemConfigMapper.insert(config);
        return Result.success(config.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SystemConfig config) {
        config.setId(id);
        systemConfigMapper.updateById(config);
        return Result.success();
    }

    @PutMapping("/batch")
    @Operation(summary = "批量更新配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchUpdate(@RequestBody Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, entry.getKey());
            SystemConfig config = systemConfigMapper.selectOne(wrapper);
            if (config != null) {
                config.setConfigValue(entry.getValue());
                systemConfigMapper.updateById(config);
            }
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        systemConfigMapper.deleteById(id);
        return Result.success();
    }
}
