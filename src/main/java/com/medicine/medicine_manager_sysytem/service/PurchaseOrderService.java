package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.PurchaseOrderDTO;
import com.medicine.medicine_manager_sysytem.entity.PurchaseOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PurchaseOrderService {

    Page<PurchaseOrder> page(Integer pageNum, Integer pageSize, Long supplierId, Integer status);

    PurchaseOrder getById(Long id);

    Long create(PurchaseOrderDTO dto, Long userId);

    void update(Long id, PurchaseOrderDTO dto);

    void delete(Long id);

    void submit(Long id);

    void approve(Long id, Long approverId);

    void reject(Long id, String reason);

    void cancel(Long id);

    void confirmArrival(Long id);

    List<Map<String, Object>> getPurchaseSuggestions();

    Long countTotalOrders();

    Long countPendingOrders();

    Long countCompletedOrders();

    BigDecimal countTotalAmount();
    
    Map<String, Object> batchInbound();
}
