package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.FinanceTransaction;
import com.medicine.medicine_manager_sysytem.service.FinanceTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Tag(name = "财务管理", description = "财务管理接口")
public class FinanceController {

    private final FinanceTransactionService financeTransactionService;

    @GetMapping("/flow/list")
    @Operation(summary = "收支流水列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<PageResult<FinanceTransaction>> flowList(PageQuery query) {
        Page<FinanceTransaction> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<FinanceTransaction> result = financeTransactionService.page(page);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @GetMapping("/flow/{id}")
    @Operation(summary = "流水详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<FinanceTransaction> flowDetail(@PathVariable Long id) {
        return Result.success(financeTransactionService.getById(id));
    }

    @PostMapping("/income")
    @Operation(summary = "收入登记")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Void> income(@RequestParam BigDecimal amount,
                               @RequestParam String source,
                               @RequestParam(required = false) String remark) {
        financeTransactionService.createIncome(amount, source, remark);
        return Result.success();
    }

    @PostMapping("/expense")
    @Operation(summary = "支出登记")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Void> expense(@RequestParam BigDecimal amount,
                                @RequestParam String type,
                                @RequestParam(required = false) String remark) {
        financeTransactionService.createExpense(amount, type, remark);
        return Result.success();
    }

    @GetMapping("/receivable/list")
    @Operation(summary = "应收账款列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Object> receivableList(PageQuery query) {
        // TODO: 实现应收账款查询
        return Result.success(null);
    }

    @GetMapping("/payable/list")
    @Operation(summary = "应付账款列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Object> payableList(PageQuery query) {
        // TODO: 实现应付账款查询
        return Result.success(null);
    }

    @PutMapping("/receivable/{id}/confirm")
    @Operation(summary = "确认收款")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Void> confirmReceivable(@PathVariable Long id) {
        // TODO: 实现确认收款逻辑
        return Result.success();
    }

    @PutMapping("/payable/{id}/confirm")
    @Operation(summary = "确认付款")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Void> confirmPayable(@PathVariable Long id) {
        // TODO: 实现确认付款逻辑
        return Result.success();
    }

    @GetMapping("/report/cashflow")
    @Operation(summary = "现金流量表")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Object> cashflowReport(@RequestParam String startDate, @RequestParam String endDate) {
        // TODO: 实现现金流量表
        return Result.success(null);
    }

    @GetMapping("/report/profit")
    @Operation(summary = "利润表")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Object> profitReport(@RequestParam String startDate, @RequestParam String endDate) {
        // TODO: 实现利润表
        return Result.success(null);
    }

    @GetMapping("/statistics")
    @Operation(summary = "财务统计")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalIncome", financeTransactionService.countTotalIncome());
        data.put("totalExpense", financeTransactionService.countTotalExpense());
        data.put("netProfit", financeTransactionService.countTotalIncome().subtract(financeTransactionService.countTotalExpense()));
        return Result.success(data);
    }
}
