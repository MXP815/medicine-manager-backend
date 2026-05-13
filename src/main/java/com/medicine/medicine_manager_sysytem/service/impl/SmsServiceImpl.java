package com.medicine.medicine_manager_sysytem.service.impl;

import com.medicine.medicine_manager_sysytem.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendVerificationCode(String phone, String code) {
        log.info("【模拟短信发送】手机号: {}, 验证码: {}", phone, code);
        // TODO: 集成阿里云/腾讯云短信服务
        // 实际项目中需要调用第三方短信API
    }
}
