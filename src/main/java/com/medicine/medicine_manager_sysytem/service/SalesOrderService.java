package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.SalesOrderDTO;
import com.medicine.medicine_manager_sysytem.entity.SalesOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SalesOrderService {

    Page<SalesOrder> page(Page<SalesOrder> page, Long customerId, Integer status);

    int countTotalOrders();
    BigDecimal countTotalAmount();
    int countPendingOrders();
    int countCompletedOrders();
    Map<String, Object> getDrugByBarcode(String barcode);

    SalesOrder getById(Long id);

    Long create(SalesOrderDTO dto, Long userId);

    void update(Long id, SalesOrderDTO dto);

    void delete(Long id);

    void submit(Long id);

    void approve(Long id);

    void cancel(Long id);

    void bindPrescription(Long orderId, String prescriptionNo);
}
