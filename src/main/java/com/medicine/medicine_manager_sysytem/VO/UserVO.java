package com.medicine.medicine_manager_sysytem.VO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer role;
    private String department;
    private LocalDateTime createTime;
}
