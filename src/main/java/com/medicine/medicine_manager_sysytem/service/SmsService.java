package com.medicine.medicine_manager_sysytem.service;

public interface SmsService {

    void sendVerificationCode(String phone, String code);
}
