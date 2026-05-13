package com.medicine.medicine_manager_sysytem.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records;
    private Long total;
    private Long size;
    private Long current;
    private Long pages;

    public PageResult(List<T> records, Long total, Long size, Long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (total + size - 1) / size;
    }

    public static <T> PageResult<T> of(List<T> records, Long total, Long size, Long current) {
        return new PageResult<>(records, total, size, current);
    }
}
