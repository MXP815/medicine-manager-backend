package com.medicine.medicine_manager_sysytem.service.impl;

import com.medicine.medicine_manager_sysytem.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@medicine-system.com");
        message.setTo(to);
        message.setSubject("【医药管理系统】验证码");
        message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露给他人。");

        mailSender.send(message);
        log.info("验证码邮件已发送至: {}", to);
    }

    @Override
    public void sendPasswordResetEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@medicine-system.com");
        message.setTo(to);
        message.setSubject("【医药管理系统】密码重置");
        message.setText("您正在重置密码，验证码为：" + code + "，有效期5分钟。\n如非本人操作，请忽略此邮件。");

        mailSender.send(message);
        log.info("密码重置邮件已发送至: {}", to);
    }
}
