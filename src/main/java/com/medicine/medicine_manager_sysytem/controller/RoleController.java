package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.Role;
import com.medicine.medicine_manager_sysytem.mapper.RoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "系统角色管理接口")
public class RoleController {

    private final RoleMapper roleMapper;

    @GetMapping("/list")
    @Operation(summary = "查询角色列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Role>> list() {
        List<Role> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Role>> page(PageQuery query) {
        Page<Role> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Role> result = roleMapper.selectPage(page, null);
        return Result.success(PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getSize(),
                result.getCurrent()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询角色")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "新增角色")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@RequestBody Role role) {
        role.setStatus(1);
        roleMapper.insert(role);
        return Result.success(role.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/menu-tree")
    @Operation(summary = "获取菜单树")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Object>> getMenuTree() {
        List<Object> menuTree = List.of(
                createMenuNode("dashboard", "首页", null),
                createMenuNode("basic-data", "基础数据", List.of(
                        createMenuNode("medicine", "药品管理", null),
                        createMenuNode("supplier", "供应商管理", null),
                        createMenuNode("customer", "客户管理", null)
                )),
                createMenuNode("purchase", "采购管理", null),
                createMenuNode("sales", "销售管理", null),
                createMenuNode("warehouse", "仓储管理", null),
                createMenuNode("quality", "质量管理", null),
                createMenuNode("finance", "财务管理", null),
                createMenuNode("system", "系统管理", List.of(
                        createMenuNode("users", "用户管理", null),
                        createMenuNode("roles", "角色管理", null),
                        createMenuNode("logs", "操作日志", null),
                        createMenuNode("config", "系统配置", null)
                ))
        );
        return Result.success(menuTree);
    }

    private Object createMenuNode(String key, String title, List<Object> children) {
        java.util.Map<String, Object> node = new java.util.HashMap<>();
        node.put("key", key);
        node.put("title", title);
        if (children != null) {
            node.put("children", children);
        }
        return node;
    }
}
