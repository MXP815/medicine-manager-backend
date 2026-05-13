package com.medicine.medicine_manager_sysytem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_verification_code")
public class VerificationCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String target;

    private String code;

    private LocalDateTime expireTime;

    private Boolean used;

    private LocalDateTime createTime;
}
