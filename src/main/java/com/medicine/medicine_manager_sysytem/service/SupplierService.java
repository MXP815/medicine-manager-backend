package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.SupplierDTO;
import com.medicine.medicine_manager_sysytem.entity.Supplier;

import java.util.List;

public interface SupplierService {

    Page<Supplier> page(Page<Supplier> page, String keyword);

    Supplier getById(Long id);

    Long create(SupplierDTO dto);

    void update(Long id, SupplierDTO dto);

    void delete(Long id);

    List<Supplier> listByIds(List<Long> ids);

    List<Supplier> getExpiringSuppliers(int days);

    void addToBlacklist(Long id);

    void removeFromBlacklist(Long id);
}
