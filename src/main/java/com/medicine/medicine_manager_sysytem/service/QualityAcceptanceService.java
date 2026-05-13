package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medicine.medicine_manager_sysytem.entity.QualityAcceptance;

public interface QualityAcceptanceService extends IService<QualityAcceptance> {

    Page<QualityAcceptance> page(Page<QualityAcceptance> page);

    void approve(Long id, Long checkerId);

    void reject(Long id, String reason);
}
