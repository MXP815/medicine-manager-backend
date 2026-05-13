package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medicine.medicine_manager_sysytem.entity.Inventory;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.mapper.InventoryMapper;
import com.medicine.medicine_manager_sysytem.mapper.MedicineMapper;
import com.medicine.medicine_manager_sysytem.service.InventoryService;
import com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final MedicineMapper medicineMapper;

    @Override
    public Page<Inventory> page(Page<Inventory> page, Long medicineId, String batchNumber) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (medicineId != null) {
            wrapper.eq(Inventory::getMedicineId, medicineId);
        }
        if (batchNumber != null && !batchNumber.isEmpty()) {
            wrapper.like(Inventory::getBatchNumber, batchNumber);
        }
        wrapper.orderByDesc(Inventory::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Page<Object> getStockRecordPage(Long inventoryId, Page<Object> page) {
        // TODO: 实现库存变动记录查询
        // 这通常需要关联查询库存变动日志表
        // 暂时返回空的分页结果
        return new Page<>(page.getCurrent(), page.getSize());
    }

    @Override
    public Inventory getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }

    @Override
    public List<Inventory> getByMedicineIdAndAvailable(Long medicineId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getMedicineId, medicineId)
               .gt(Inventory::getQuantity, 0)
               .ge(Inventory::getExpiryDate, LocalDate.now())
               .orderByAsc(Inventory::getExpiryDate);
        return this.list(wrapper);
    }

    @Override
    public List<InventoryWarningVO> getWarningInventory() {
        List<InventoryWarningVO> warningList = new ArrayList<>();
        
        // 查询所有库存记录
        LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
        invWrapper.gt(Inventory::getQuantity, 0);
        List<Inventory> inventories = this.list(invWrapper);
        
        for (Inventory inventory : inventories) {
            // 获取药品信息
            Medicine medicine = medicineMapper.selectById(inventory.getMedicineId());
            if (medicine == null) {
                continue;
            }
            
            // 检查是否低于最小库存
            Integer minStock = inventory.getMinStock();
            if (minStock == null) {
                minStock = medicine.getMinStock();
            }
            
            if (minStock != null && inventory.getQuantity() < minStock) {
                InventoryWarningVO vo = new InventoryWarningVO();
                vo.setInventoryId(inventory.getId());
                vo.setMedicineId(inventory.getMedicineId());
                vo.setMedicineName(medicine.getName());
                vo.setBatchNumber(inventory.getBatchNumber());
                vo.setQuantity(inventory.getQuantity());
                vo.setMinStock(minStock);
                vo.setWarningType("STOCK_LOW");
                vo.setWarningLevel(inventory.getQuantity() < minStock / 2 ? "HIGH" : "MEDIUM");
                vo.setWarningMessage(String.format("库存不足：当前 %d，最低 %d", inventory.getQuantity(), minStock));
                warningList.add(vo);
            }
        }
        
        return warningList;
    }

    @Override
    public List<InventoryWarningVO> getExpiringInventory(int days) {
        LocalDate thresholdDate = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Inventory::getExpiryDate, thresholdDate)
               .ge(Inventory::getExpiryDate, LocalDate.now())
               .orderByAsc(Inventory::getExpiryDate);
        
        List<Inventory> expiringList = this.list(wrapper);
        
        List<InventoryWarningVO> result = new ArrayList<>();
        for (Inventory inv : expiringList) {
            InventoryWarningVO vo = new InventoryWarningVO();
            vo.setInventoryId(inv.getId());
            vo.setMedicineId(inv.getMedicineId());
            vo.setBatchNumber(inv.getBatchNumber());
            vo.setQuantity(inv.getQuantity());
            vo.setExpiryDate(inv.getExpiryDate());
            
            // 获取药品名称
            Medicine medicine = medicineMapper.selectById(inv.getMedicineId());
            if (medicine != null) {
                vo.setMedicineName(medicine.getName());
            }
            
            // 计算距离过期的天数
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), inv.getExpiryDate());
            vo.setWarningType("EXPIRY_SOON");
            vo.setWarningLevel(daysUntilExpiry <= 7 ? "HIGH" : "MEDIUM");
            vo.setWarningMessage(String.format("即将过期：剩余 %d 天", daysUntilExpiry));
            
            result.add(vo);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(Long medicineId, Integer quantity, String batchNo) {
        // 查找该药品是否存在库存记录
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getMedicineId, medicineId);
        if (batchNo != null && !batchNo.isEmpty()) {
            wrapper.eq(Inventory::getBatchNumber, batchNo);
        } else {
            // 如果没有批号，找最新的库存记录
            wrapper.orderByDesc(Inventory::getCreateTime);
        }
        
        Inventory inventory = this.getOne(wrapper);
        if (inventory == null) {
            // 创建新的库存记录
            inventory = new Inventory();
            inventory.setMedicineId(medicineId);
            inventory.setQuantity(quantity);
            inventory.setBatchNumber(batchNo);
            inventory.setExpiryDate(LocalDate.now().plusYears(2)); // 默认 2 年有效期
            this.save(inventory);
        } else {
            // 更新现有库存
            inventory.setQuantity(inventory.getQuantity() + quantity);
            this.updateById(inventory);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockOut(Long medicineId, Integer quantity, String batchNo) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getMedicineId, medicineId);
        if (batchNo != null && !batchNo.isEmpty()) {
            wrapper.eq(Inventory::getBatchNumber, batchNo);
        } else {
            wrapper.orderByAsc(Inventory::getExpiryDate); // 先进先出
        }
        
        Inventory inventory = this.getOne(wrapper);
        if (inventory != null && inventory.getQuantity() >= quantity) {
            inventory.setQuantity(inventory.getQuantity() - quantity);
            this.updateById(inventory);
        } else {
            throw new RuntimeException("库存不足");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockCheck(Long inventoryId, Integer actualQuantity) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory != null) {
            Integer difference = actualQuantity - inventory.getQuantity();
            inventory.setQuantity(actualQuantity);
            this.updateById(inventory);
            
            // 记录盘点差异日志（后续可扩展）
            if (difference != 0) {
                System.out.println(String.format("库存盘点差异：ID=%d, 差异=%d, 原因待查", inventoryId, difference));
            }
        } else {
            throw new RuntimeException("库存记录不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long inventoryId, Integer quantity, String reason) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory != null) {
            inventory.setQuantity(inventory.getQuantity() + quantity);
            this.updateById(inventory);
            
            // 记录库存变动日志（后续可扩展）
            System.out.println(String.format("库存调整：ID=%d, 数量=%d, 原因=%s", inventoryId, quantity, reason));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarningStatus() {
        // 批量更新预警状态
        LocalDate now = LocalDate.now();
        
        // 更新过期药品状态
        LambdaQueryWrapper<Inventory> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.lt(Inventory::getExpiryDate, now);
        List<Inventory> expiredList = this.list(expiredWrapper);
        
        for (Inventory inventory : expiredList) {
            inventory.setStatus(0); // 设置为不可用
        }
        
        if (!expiredList.isEmpty()) {
            this.updateBatchById(expiredList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMinStock(Long medicineId, Integer minStock) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getMedicineId, medicineId);
        List<Inventory> inventories = this.list(wrapper);
        
        for (Inventory inventory : inventories) {
            inventory.setMinStock(minStock);
        }
        
        if (!inventories.isEmpty()) {
            this.updateBatchById(inventories);
        }
    }
}
