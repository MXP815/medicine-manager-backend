package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medicine.medicine_manager_sysytem.entity.FinanceTransaction;
import com.medicine.medicine_manager_sysytem.mapper.FinanceTransactionMapper;
import com.medicine.medicine_manager_sysytem.service.FinanceTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinanceTransactionServiceImpl extends ServiceImpl<FinanceTransactionMapper, FinanceTransaction> implements FinanceTransactionService {

    @Override
    public Page<FinanceTransaction> page(Page<FinanceTransaction> page) {
        LambdaQueryWrapper<FinanceTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FinanceTransaction::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public FinanceTransaction getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createIncome(BigDecimal amount, String source, String remark) {
        FinanceTransaction transaction = new FinanceTransaction();
        transaction.setTransactionType(1); // 1-收入
        transaction.setAmount(amount);
        transaction.setSource(source);
        transaction.setRemark(remark);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setStatus(1); // 1-已完成
        this.save(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createExpense(BigDecimal amount, String type, String remark) {
        FinanceTransaction transaction = new FinanceTransaction();
        transaction.setTransactionType(2); // 2-支出
        transaction.setAmount(amount);
        transaction.setSourceType(type);
        transaction.setRemark(remark);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setStatus(1); // 1-已完成
        this.save(transaction);
    }

    @Override
    public BigDecimal countTotalIncome() {
        LambdaQueryWrapper<FinanceTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceTransaction::getTransactionType, 1);
        wrapper.in(FinanceTransaction::getStatus, 1, 2); // 已完成或处理中
        return this.list(wrapper).stream()
                .map(FinanceTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal countTotalExpense() {
        LambdaQueryWrapper<FinanceTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceTransaction::getTransactionType, 2);
        wrapper.in(FinanceTransaction::getStatus, 1, 2); // 已完成或处理中
        return this.list(wrapper).stream()
                .map(FinanceTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
