package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.CustomerDTO;
import com.medicine.medicine_manager_sysytem.entity.Customer;
import com.medicine.medicine_manager_sysytem.exception.BusinessException;
import com.medicine.medicine_manager_sysytem.mapper.CustomerMapper;
import com.medicine.medicine_manager_sysytem.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;

    @Override
    public Page<Customer> page(Page<Customer> page, String keyword) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Customer::getName, keyword)
                    .or()
                    .like(Customer::getContactPhone, keyword)
            );
        }
        return customerMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Map<String, Object>> getPurchaseHistory(Long customerId) {
        // 查询客户的购买历史（从销售订单表）
        // 这里需要注入 SalesOrderMapper，为简化暂时返回空列表
        // 实际应该关联查询 t_sales_order 和 t_sales_order_item
        return List.of();
    }

    @Override
    public Integer getCustomerPoints(Long customerId) {
        Customer customer = getById(customerId);
        if (customer == null) {
            return 0;
        }
        // 假设 Customer 实体有 points 字段
        // 如果没有，需要从消费记录计算
        return customer.getPoints() != null ? customer.getPoints() : 0;
    }

    @Override
    public Customer getById(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CustomerDTO dto) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto, customer);
        customer.setPoints(0); // 初始积分为 0
        customer.setStatus(1); // 默认启用
        customerMapper.insert(customer);
        return customer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CustomerDTO dto) {
        Customer customer = getById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        BeanUtils.copyProperties(dto, customer);
        customerMapper.updateById(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        customerMapper.deleteById(id);
    }

    @Override
    public List<Customer> listByIds(List<Long> ids) {
        return customerMapper.selectBatchIds(ids);
    }
}
