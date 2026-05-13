package com.medicine.medicine_manager_sysytem.common;

import lombok.Data;

@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String sortBy;
    private String sortOrder;
}
