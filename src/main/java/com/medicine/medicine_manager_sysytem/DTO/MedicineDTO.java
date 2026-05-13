package com.medicine.medicine_manager_sysytem.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class MedicineDTO {

    private Long id;

    @NotBlank(message = "药品编码不能为空")
    private String code;

    @NotBlank(message = "药品名称不能为空")
    private String name;

    @NotBlank(message = "规格不能为空")
    private String specification;

    @NotBlank(message = "生产厂家不能为空")
    private String manufacturer;

    @NotBlank(message = "单位不能为空")
    private String unit;

    @NotNull(message = "有效期不能为空")
    @Positive(message = "有效期必须大于 0")
    private Integer shelfLife;

    @NotBlank(message = "存储条件不能为空")
    private String storageCondition;

    private String category;

    private String form;

    private String approvalNumber;

    @NotNull(message = "采购价格不能为空")
    @Positive(message = "采购价格必须大于 0")
    private Double purchasePrice;

    @NotNull(message = "销售价格不能为空")
    @Positive(message = "销售价格必须大于 0")
    private Double salePrice;

    private Integer minStock;

    private Integer status;

    private String barcode;

    private String description;
}
