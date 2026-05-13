package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.SupplierDTO;
import com.medicine.medicine_manager_sysytem.entity.Supplier;
import com.medicine.medicine_manager_sysytem.exception.BusinessException;
import com.medicine.medicine_manager_sysytem.mapper.SupplierMapper;
import com.medicine.medicine_manager_sysytem.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

    @Override
    public Page<Supplier> page(Page<Supplier> page, String keyword) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Supplier::getName, keyword)
                    .or()
                    .like(Supplier::getContactPhone, keyword)
            );
        }
        return supplierMapper.selectPage(page, wrapper);
    }

    @Override
    public Supplier getById(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SupplierDTO dto) {
        Supplier supplier = getById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }
        BeanUtils.copyProperties(dto, supplier);
        supplierMapper.updateById(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        supplierMapper.deleteById(id);
    }

    @Override
    public List<Supplier> listByIds(List<Long> ids) {
        return supplierMapper.selectBatchIds(ids);
    }

    @Override
    public List<Supplier> getExpiringSuppliers(int days) {
        LocalDate now = LocalDate.now();
        LocalDate futureDate = now.plusDays(days);

        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Supplier::getLicenseExpiryDate, now, futureDate);
        return supplierMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToBlacklist(Long id) {
        Supplier supplier = getById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }
        supplier.setBlacklisted(true);
        supplierMapper.updateById(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklist(Long id) {
        Supplier supplier = getById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }
        supplier.setBlacklisted(false);
        supplierMapper.updateById(supplier);
    }
}
