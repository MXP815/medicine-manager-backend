package com.medicine.medicine_manager_sysytem.DTO;

import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "客户名称不能为空")
    private String name;

    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String contactEmail;

    private String deliveryAddress;

    @DecimalMin(value = "0", message = "信用额度不能为负数")
    private BigDecimal creditLimit;

    private Integer customerLevel;

    private String remark;
}
