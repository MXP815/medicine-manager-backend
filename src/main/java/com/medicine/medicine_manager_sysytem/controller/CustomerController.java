package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.DTO.CustomerDTO;
import com.medicine.medicine_manager_sysytem.entity.Customer;
import com.medicine.medicine_manager_sysytem.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales/customer")
@RequiredArgsConstructor
@Tag(name = "客户管理", description = "客户信息管理接口")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/list")
    @Operation(summary = "分页查询客户列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<PageResult<Customer>> page(PageQuery query, String keyword) {
        Page<Customer> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Customer> result = customerService.page(page, keyword);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询客户详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Customer> getById(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增客户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody CustomerDTO dto) {
        return Result.success(customerService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新客户信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        customerService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "查询客户购药历史")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<List<Map<String, Object>>> getPurchaseHistory(@PathVariable Long id) {
        List<Map<String, Object>> history = customerService.getPurchaseHistory(id);
        return Result.success(history != null ? history : List.of());
    }

    @GetMapping("/{id}/points")
    @Operation(summary = "查询客户积分")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Map<String, Object>> getPoints(@PathVariable Long id) {
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", id);
        data.put("points", customerService.getCustomerPoints(id));
        return Result.success(data);
    }

    @PostMapping("/checkCredit")
    @Operation(summary = "检查客户信用额度")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Map<String, Object>> checkCredit(@RequestParam Long customerId,
                                                    @RequestParam BigDecimal amount) {
        Customer customer = customerService.getById(customerId);
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", customerId);
        data.put("creditLimit", customer != null ? customer.getCreditLimit() : BigDecimal.ZERO);
        data.put("amount", amount);
        data.put("available", customer != null && customer.getCreditLimit().compareTo(amount) >= 0);
        return Result.success(data);
    }
}
