package com.medicine.medicine_manager_sysytem.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "供应商名称不能为空")
    private String name;

    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String contactEmail;

    private String address;

    private String businessScope;

    @NotBlank(message = "许可证号不能为空")
    private String licenseNumber;

    private String licensePath;

    @NotNull(message = "资质有效期不能为空")
    private LocalDate licenseExpiryDate;

    private String cooperationLevel;

    private String remark;
}
