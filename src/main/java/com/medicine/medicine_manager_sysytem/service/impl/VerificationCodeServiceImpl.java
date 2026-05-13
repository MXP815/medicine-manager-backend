package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medicine.medicine_manager_sysytem.entity.VerificationCode;
import com.medicine.medicine_manager_sysytem.mapper.VerificationCodeMapper;
import com.medicine.medicine_manager_sysytem.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;

    @Override
    public String generateCode(String type, String target) {
        String code = String.format("%06d", new Random().nextInt(999999));

        VerificationCode vc = new VerificationCode();
        vc.setType(type);
        vc.setTarget(target);
        vc.setCode(code);
        vc.setExpireTime(LocalDateTime.now().plusMinutes(5));
        vc.setUsed(false);
        vc.setCreateTime(LocalDateTime.now());

        verificationCodeMapper.insert(vc);

        return code;
    }

    @Override
    public boolean verifyCode(String type, String target, String code) {
        LambdaQueryWrapper<VerificationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationCode::getType, type)
                .eq(VerificationCode::getTarget, target)
                .eq(VerificationCode::getCode, code)
                .eq(VerificationCode::getUsed, false)
                .gt(VerificationCode::getExpireTime, LocalDateTime.now())
                .orderByDesc(VerificationCode::getCreateTime)
                .last("LIMIT 1");

        VerificationCode vc = verificationCodeMapper.selectOne(wrapper);

        if (vc != null) {
            vc.setUsed(true);
            verificationCodeMapper.updateById(vc);
            return true;
        }

        return false;
    }

    @Override
    public void invalidateCode(String type, String target) {
        LambdaQueryWrapper<VerificationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationCode::getType, type)
                .eq(VerificationCode::getTarget, target)
                .eq(VerificationCode::getUsed, false);

        VerificationCode vc = verificationCodeMapper.selectOne(wrapper);
        if (vc != null) {
            vc.setUsed(true);
            verificationCodeMapper.updateById(vc);
        }
    }
}
