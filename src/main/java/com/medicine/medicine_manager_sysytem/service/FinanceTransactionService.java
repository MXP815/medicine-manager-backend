package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medicine.medicine_manager_sysytem.entity.FinanceTransaction;

import java.math.BigDecimal;

public interface FinanceTransactionService extends IService<FinanceTransaction> {

    Page<FinanceTransaction> page(Page<FinanceTransaction> page);

    FinanceTransaction getById(Long id);

    void createIncome(BigDecimal amount, String source, String remark);

    void createExpense(BigDecimal amount, String type, String remark);

    BigDecimal countTotalIncome();

    BigDecimal countTotalExpense();
}
