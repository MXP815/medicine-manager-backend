package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.SalesOrderDTO;
import com.medicine.medicine_manager_sysytem.DTO.SalesOrderItemDTO;
import com.medicine.medicine_manager_sysytem.entity.Customer;
import com.medicine.medicine_manager_sysytem.entity.Inventory;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.entity.SalesOrder;
import com.medicine.medicine_manager_sysytem.entity.SalesOrderItem;
import com.medicine.medicine_manager_sysytem.exception.BusinessException;
import com.medicine.medicine_manager_sysytem.mapper.SalesOrderMapper;
import com.medicine.medicine_manager_sysytem.service.CustomerService;
import com.medicine.medicine_manager_sysytem.service.InventoryService;
import com.medicine.medicine_manager_sysytem.service.MedicineService;
import com.medicine.medicine_manager_sysytem.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderMapper salesOrderMapper;
    private final CustomerService customerService;
    private final MedicineService medicineService;
    private final InventoryService inventoryService;

    @Override
    public Page<SalesOrder> page(Page<SalesOrder> page, Long customerId, Integer status) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (customerId != null) {
            wrapper.eq(SalesOrder::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(SalesOrder::getStatus, status);
        }
        wrapper.orderByDesc(SalesOrder::getCreateTime);
        return salesOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public int countTotalOrders() {
        return Math.toIntExact(salesOrderMapper.selectCount(null));
    }

    @Override
    public BigDecimal countTotalAmount() {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SalesOrder::getStatus, 4, 5); // 已完成或配送中
        List<SalesOrder> orders = salesOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int countPendingOrders() {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SalesOrder::getStatus, 1, 2); // 已提交或已审核
        return Math.toIntExact(salesOrderMapper.selectCount(wrapper));
    }

    @Override
    public int countCompletedOrders() {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getStatus, 4); // 已完成
        return Math.toIntExact(salesOrderMapper.selectCount(wrapper));
    }

    @Override
    public Map<String, Object> getDrugByBarcode(String barcode) {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medicine::getBarcode, barcode);
        Medicine medicine = medicineService.getOne(wrapper);
        
        if (medicine == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", medicine.getId());
        result.put("name", medicine.getName());
        result.put("barcode", medicine.getBarcode());
        result.put("specification", medicine.getSpecification());
        result.put("manufacturer", medicine.getManufacturer());
        result.put("unitPrice", medicine.getSalePrice());
        result.put("stock", getMedicineStock(medicine.getId()));
        
        return result;
    }
    
    private int getMedicineStock(Long medicineId) {
        List<Inventory> inventories = inventoryService.getByMedicineIdAndAvailable(medicineId);
        return inventories.stream().mapToInt(Inventory::getQuantity).sum();
    }

    @Override
    public SalesOrder getById(Long id) {
        return salesOrderMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SalesOrderDTO dto, Long userId) {
        Customer customer = customerService.getById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        SalesOrder order = new SalesOrder();
        order.setCustomerId(dto.getCustomerId());
        order.setStatus(0);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setDeliveryMethod(dto.getDeliveryMethod());
        order.setPrescriptionPath(dto.getPrescriptionPath());
        order.setRemark(dto.getRemark());
        order.setCreatorId(userId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;

        List<SalesOrderItem> items = new ArrayList<>();
        for (SalesOrderItemDTO itemDTO : dto.getItems()) {
            Medicine medicine = medicineService.getById(itemDTO.getMedicineId());
            if (medicine == null) {
                throw new BusinessException("药品不存在：ID=" + itemDTO.getMedicineId());
            }

            List<Inventory> inventories = inventoryService.getByMedicineIdAndAvailable(medicine.getId());
            if (inventories.isEmpty() || inventories.stream().mapToInt(Inventory::getQuantity).sum() < itemDTO.getQuantity()) {
                throw new BusinessException("库存不足：药品 ID=" + itemDTO.getMedicineId());
            }

            inventories.sort(Comparator.comparing(Inventory::getExpiryDate));

            int remainingQty = itemDTO.getQuantity();
            for (Inventory inventory : inventories) {
                if (remainingQty <= 0) break;

                int useQty = Math.min(inventory.getQuantity(), remainingQty);

                SalesOrderItem item = new SalesOrderItem();
                item.setMedicineId(medicine.getId());
                item.setBatchNumber(inventory.getBatchNumber());
                item.setQuantity(useQty);
                item.setUnitPrice(BigDecimal.valueOf(itemDTO.getUnitPrice()));
                item.setTotalPrice(BigDecimal.valueOf(itemDTO.getUnitPrice()).multiply(BigDecimal.valueOf(useQty)));
                items.add(item);

                remainingQty -= useQty;
            }

            BigDecimal itemTotal = BigDecimal.valueOf(itemDTO.getUnitPrice())
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            totalCount += itemDTO.getQuantity();
        }

        order.setTotalAmount(totalAmount);
        order.setTotalCount(totalCount);

        salesOrderMapper.insert(order);

        for (SalesOrderItem item : items) {
            item.setOrderId(order.getId());
        }

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SalesOrderDTO dto) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }

        if (!order.getStatus().equals(0)) {
            throw new BusinessException("只能修改草稿状态的订单");
        }

        order.setCustomerId(dto.getCustomerId());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setDeliveryMethod(dto.getDeliveryMethod());
        order.setPrescriptionPath(dto.getPrescriptionPath());
        order.setRemark(dto.getRemark());

        salesOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }

        if (!order.getStatus().equals(0)) {
            throw new BusinessException("只能删除草稿状态的订单");
        }

        salesOrderMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }

        if (!order.getStatus().equals(0)) {
            throw new BusinessException("只能提交草稿状态的订单");
        }

        order.setStatus(1);
        salesOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }

        if (!order.getStatus().equals(1)) {
            throw new BusinessException("只能审核已提交的订单");
        }

        order.setStatus(2);
        salesOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }

        if (order.getStatus().equals(4) || order.getStatus().equals(5)) {
            throw new BusinessException("已完成或已取消的订单不能取消");
        }

        order.setStatus(6);
        salesOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPrescription(Long orderId, String prescriptionNo) {
        SalesOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }

        order.setPrescriptionPath(prescriptionNo);
        salesOrderMapper.updateById(order);
    }
}
