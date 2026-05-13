package com.medicine.medicine_manager_sysytem.service;

public interface EmailService {

    void sendVerificationCode(String to, String code);

    void sendPasswordResetEmail(String to, String code);
}
