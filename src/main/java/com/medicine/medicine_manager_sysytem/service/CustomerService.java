package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.CustomerDTO;
import com.medicine.medicine_manager_sysytem.entity.Customer;

import java.util.List;
import java.util.Map;

public interface CustomerService {

    Page<Customer> page(Page<Customer> page, String keyword);

    List<Map<String, Object>> getPurchaseHistory(Long customerId);
    Integer getCustomerPoints(Long customerId);

    Customer getById(Long id);

    Long create(CustomerDTO dto);

    void update(Long id, CustomerDTO dto);

    void delete(Long id);

    List<Customer> listByIds(List<Long> ids);
}
