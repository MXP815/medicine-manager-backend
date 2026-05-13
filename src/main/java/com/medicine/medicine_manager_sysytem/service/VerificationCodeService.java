package com.medicine.medicine_manager_sysytem.service;

import com.medicine.medicine_manager_sysytem.entity.VerificationCode;

public interface VerificationCodeService {

    String generateCode(String type, String target);

    boolean verifyCode(String type, String target, String code);

    void invalidateCode(String type, String target);
}
