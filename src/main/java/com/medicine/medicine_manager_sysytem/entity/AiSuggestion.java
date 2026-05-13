package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_ai_suggestion")
public class AiSuggestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String category;

    private String title;

    private String content;

    private Integer priority;

    private LocalDateTime createTime;

    private String medicineIds;

    private String quantities;

    private Long supplierId;

    private Double estimatedAmount;
}
