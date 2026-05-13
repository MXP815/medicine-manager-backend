package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medicine.medicine_manager_sysytem.entity.QualityAcceptance;
import com.medicine.medicine_manager_sysytem.mapper.QualityAcceptanceMapper;
import com.medicine.medicine_manager_sysytem.service.QualityAcceptanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QualityAcceptanceServiceImpl extends ServiceImpl<QualityAcceptanceMapper, QualityAcceptance> implements QualityAcceptanceService {

    @Override
    public Page<QualityAcceptance> page(Page<QualityAcceptance> page) {
        LambdaQueryWrapper<QualityAcceptance> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(QualityAcceptance::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public void approve(Long id, Long checkerId) {
        QualityAcceptance acceptance = this.getById(id);
        if (acceptance == null) {
            throw new RuntimeException("验收记录不存在");
        }

        acceptance.setCheckerId(checkerId);
        acceptance.setCheckTime(LocalDateTime.now());
        acceptance.setAcceptanceResult(1); // 1-合格
        this.updateById(acceptance);
    }

    @Override
    @Transactional
    public void reject(Long id, String reason) {
        QualityAcceptance acceptance = this.getById(id);
        if (acceptance == null) {
            throw new RuntimeException("验收记录不存在");
        }

        acceptance.setCheckTime(LocalDateTime.now());
        acceptance.setAcceptanceResult(2); // 2-不合格
        acceptance.setRejectionReason(reason);
        this.updateById(acceptance);
    }
}
