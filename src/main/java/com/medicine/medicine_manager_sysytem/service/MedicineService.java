package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medicine.medicine_manager_sysytem.DTO.MedicineDTO;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.VO.InventoryWarningVO;

import java.util.List;

public interface MedicineService extends IService<Medicine> {

    Page<Medicine> page(Page<Medicine> page, String keyword);

    Medicine getById(Long id);

    Long create(MedicineDTO dto);

    void update(Long id, MedicineDTO dto);

    void delete(Long id);

    List<InventoryWarningVO> getLowStockMedicines();
}
