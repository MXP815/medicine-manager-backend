package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medicine.medicine_manager_sysytem.DTO.MedicineDTO;
import com.medicine.medicine_manager_sysytem.entity.Inventory;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.mapper.InventoryMapper;
import com.medicine.medicine_manager_sysytem.mapper.MedicineMapper;
import com.medicine.medicine_manager_sysytem.service.MedicineService;
import com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    private final InventoryMapper inventoryMapper;

    @Override
    public Page<Medicine> page(Page<Medicine> page, String keyword) {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(Medicine::getCode, keyword)
                .or()
                .like(Medicine::getName, keyword)
                .or()
                .like(Medicine::getSpecification, keyword)
            );
        }
        wrapper.orderByDesc(Medicine::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Medicine getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }

    @Override
    public Long create(MedicineDTO dto) {
        Medicine medicine = new Medicine();
        medicine.setCode(dto.getCode());
        medicine.setName(dto.getName());
        medicine.setSpecification(dto.getSpecification());
        medicine.setManufacturer(dto.getManufacturer());
        medicine.setApprovalNumber(dto.getApprovalNumber());
        medicine.setCategory(dto.getCategory());
        medicine.setForm(dto.getForm());
        medicine.setUnit(dto.getUnit());
        medicine.setPurchasePrice(dto.getPurchasePrice());
        medicine.setSalePrice(dto.getSalePrice());
        medicine.setBarcode(dto.getBarcode());
        medicine.setMinStock(dto.getMinStock());
        medicine.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(medicine);
        return medicine.getId();
    }

    @Override
    public void update(Long id, MedicineDTO dto) {
        Medicine medicine = this.getById(id);
        if (medicine != null) {
            medicine.setCode(dto.getCode());
            medicine.setName(dto.getName());
            medicine.setSpecification(dto.getSpecification());
            medicine.setManufacturer(dto.getManufacturer());
            medicine.setApprovalNumber(dto.getApprovalNumber());
            medicine.setCategory(dto.getCategory());
            medicine.setForm(dto.getForm());
            medicine.setUnit(dto.getUnit());
            medicine.setPurchasePrice(dto.getPurchasePrice());
            medicine.setSalePrice(dto.getSalePrice());
            medicine.setBarcode(dto.getBarcode());
            medicine.setMinStock(dto.getMinStock());
            medicine.setStatus(dto.getStatus());
            this.updateById(medicine);
        }
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    public List<InventoryWarningVO> getLowStockMedicines() {
        List<InventoryWarningVO> warningList = new ArrayList<>();
        
        // 查询所有药品及其库存
        List<Medicine> medicines = this.list();
        
        for (Medicine medicine : medicines) {
            // 查询该药品的总库存
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Inventory::getMedicineId, medicine.getId())
                   .gt(Inventory::getQuantity, 0)
                   .ge(Inventory::getExpiryDate, LocalDate.now());
            
            List<Inventory> inventories = inventoryMapper.selectList(wrapper);
            int totalStock = inventories.stream().mapToInt(Inventory::getQuantity).sum();
            
            // 检查是否低于最小库存
            if (medicine.getMinStock() != null && totalStock < medicine.getMinStock()) {
                InventoryWarningVO vo = new InventoryWarningVO();
                vo.setMedicineId(medicine.getId());
                vo.setMedicineName(medicine.getName());
                vo.setQuantity(totalStock);
                vo.setMinStock(medicine.getMinStock());
                vo.setWarningType("STOCK_LOW");
                vo.setWarningLevel(totalStock < medicine.getMinStock() / 2 ? "HIGH" : "MEDIUM");
                vo.setWarningMessage(String.format("库存不足：当前 %d，最低 %d", totalStock, medicine.getMinStock()));
                warningList.add(vo);
            }
        }
        
        return warningList;
    }
}
