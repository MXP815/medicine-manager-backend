package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.PurchaseOrderDTO;
import com.medicine.medicine_manager_sysytem.DTO.PurchaseOrderItemDTO;
import com.medicine.medicine_manager_sysytem.entity.Inventory;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.entity.PurchaseOrder;
import com.medicine.medicine_manager_sysytem.entity.PurchaseOrderItem;
import com.medicine.medicine_manager_sysytem.entity.Supplier;
import com.medicine.medicine_manager_sysytem.exception.BusinessException;
import com.medicine.medicine_manager_sysytem.mapper.PurchaseOrderItemMapper;
import com.medicine.medicine_manager_sysytem.mapper.PurchaseOrderMapper;
import com.medicine.medicine_manager_sysytem.service.InventoryService;
import com.medicine.medicine_manager_sysytem.service.MedicineService;
import com.medicine.medicine_manager_sysytem.service.PurchaseOrderService;
import com.medicine.medicine_manager_sysytem.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final SupplierService supplierService;
    private final MedicineService medicineService;
    private final InventoryService inventoryService;

    @Override
    public Page<PurchaseOrder> page(Integer pageNum, Integer pageSize, Long supplierId, Integer status) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        return purchaseOrderMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmArrival(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        if (!order.getStatus().equals(2)) {
            throw new BusinessException("只能确认已审核的订单");
        }

        // 查询订单明细
        List<PurchaseOrderItem> items = getPurchaseOrderItems(order.getId());
        if (items.isEmpty()) {
            throw new BusinessException("订单没有明细");
        }

        // 遍历订单明细，执行入库操作
        for (PurchaseOrderItem item : items) {
            Medicine medicine = medicineService.getById(item.getMedicineId());
            if (medicine == null) {
                throw new BusinessException("药品不存在：ID=" + item.getMedicineId());
            }

            // 调用入库服务
            inventoryService.stockIn(
                medicine.getId(),
                item.getQuantity(),
                item.getBatchNumber()
            );
        }

        // 更新订单状态为已完成
        order.setStatus(4);
        order.setActualDeliveryDate(LocalDateTime.now());
        purchaseOrderMapper.updateById(order);
    }

    @Override
    public List<Map<String, Object>> getPurchaseSuggestions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        // 查询库存不足的药品
        List<com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO> warningList = inventoryService.getWarningInventory();
        
        for (com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO vo : warningList) {
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("medicineId", vo.getMedicineId());
            suggestion.put("medicineName", vo.getMedicineName());
            suggestion.put("currentStock", vo.getQuantity());
            suggestion.put("minStock", vo.getMinStock());
            suggestion.put("suggestedQuantity", vo.getMinStock() != null ? vo.getMinStock() * 2 - vo.getQuantity() : vo.getQuantity());
            suggestion.put("reason", "库存不足");
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }

    @Override
    public Long countTotalOrders() {
        return purchaseOrderMapper.selectCount(null);
    }

    @Override
    public BigDecimal countTotalAmount() {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PurchaseOrder::getStatus, 4, 5); // 已完成或配送中
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Long countPendingOrders() {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PurchaseOrder::getStatus, 1, 2); // 已提交或已审核
        return purchaseOrderMapper.selectCount(wrapper);
    }

    @Override
    public Long countCompletedOrders() {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, 4); // 已完成
        return purchaseOrderMapper.selectCount(wrapper);
    }

    @Override
    public PurchaseOrder getById(Long id) {
        return purchaseOrderMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseOrderDTO dto, Long userId) {
        Supplier supplier = supplierService.getById(dto.getSupplierId());
        if (supplier == null || supplier.getBlacklisted()) {
            throw new BusinessException("供应商不存在或已被列入黑名单");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(dto.getSupplierId());
        order.setStatus(0);
        order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        order.setContractPath(dto.getContractPath());
        order.setRemark(dto.getRemark());
        order.setCreatorId(userId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;

        for (PurchaseOrderItemDTO itemDTO : dto.getItems()) {
            Medicine medicine = medicineService.getById(itemDTO.getMedicineId());
            if (medicine == null) {
                throw new BusinessException("药品不存在：ID=" + itemDTO.getMedicineId());
            }

            BigDecimal itemTotal = BigDecimal.valueOf(itemDTO.getUnitPrice())
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            totalCount += itemDTO.getQuantity();
        }

        order.setTotalAmount(totalAmount);
        order.setTotalCount(totalCount);

        purchaseOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PurchaseOrderDTO dto) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        if (!order.getStatus().equals(0)) {
            throw new BusinessException("只能修改草稿状态的订单");
        }

        order.setSupplierId(dto.getSupplierId());
        order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        order.setContractPath(dto.getContractPath());
        order.setRemark(dto.getRemark());

        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        if (!order.getStatus().equals(0)) {
            throw new BusinessException("只能删除草稿状态的订单");
        }

        purchaseOrderMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        if (!order.getStatus().equals(0)) {
            throw new BusinessException("只能提交草稿状态的订单");
        }

        order.setStatus(1);
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long userId) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        if (!order.getStatus().equals(1)) {
            throw new BusinessException("只能审核已提交的订单");
        }

        order.setStatus(2);
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String reason) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        if (!order.getStatus().equals(1)) {
            throw new BusinessException("只能拒绝已提交的订单");
        }

        order.setStatus(3);
        order.setRemark(reason);
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        if (order.getStatus().equals(4) || order.getStatus().equals(5)) {
            throw new BusinessException("已完成或已取消的订单不能取消");
        }

        order.setStatus(6);
        purchaseOrderMapper.updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchInbound() {
        System.out.println("=== 开始批量入库已通过订单 ===");
        
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, 2);
        List<PurchaseOrder> approvedOrders = purchaseOrderMapper.selectList(wrapper);
        
        if (approvedOrders.isEmpty()) {
            throw new BusinessException("没有待入库的订单");
        }
        
        int successCount = 0;
        int failCount = 0;
        int totalItemsCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        for (PurchaseOrder order : approvedOrders) {
            try {
                List<PurchaseOrderItem> items = getPurchaseOrderItems(order.getId());
                
                if (items.isEmpty()) {
                    System.out.println("⚠️ 订单 " + order.getOrderNo() + " 没有明细，跳过");
                    continue;
                }
                
                for (PurchaseOrderItem item : items) {
                    Medicine medicine = medicineService.getById(item.getMedicineId());
                    if (medicine == null) {
                        System.err.println("⚠️ 药品不存在，跳过，药品ID: " + item.getMedicineId());
                        continue;
                    }
                    
                    inventoryService.stockIn(
                        medicine.getId(),
                        item.getQuantity(),
                        item.getBatchNumber()
                    );
                    
                    totalItemsCount++;
                }
                
                order.setStatus(4);
                purchaseOrderMapper.updateById(order);
                successCount++;
                
                System.out.println("✅ 订单 " + order.getOrderNo() + " 入库完成，共" + items.size() + "项");
            } catch (Exception e) {
                failCount++;
                String errorMsg = "订单 " + order.getOrderNo() + " 入库失败: " + e.getMessage();
                errorMessages.add(errorMsg);
                System.err.println("❌ " + errorMsg);
                e.printStackTrace();
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", approvedOrders.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalItemsCount", totalItemsCount);
        result.put("errorMessages", errorMessages);
        
        System.out.println("✅ 批量入库完成 - 总订单数: " + approvedOrders.size() + 
                         ", 成功: " + successCount + 
                         ", 失败: " + failCount + 
                         ", 总明细数: " + totalItemsCount);
        
        return result;
    }

    /**
     * 获取采购订单项列表
     */
    private List<PurchaseOrderItem> getPurchaseOrderItems(Long orderId) {
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getOrderId, orderId);
        return purchaseOrderItemMapper.selectList(wrapper);
    }
}
