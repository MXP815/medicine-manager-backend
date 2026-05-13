package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medicine.medicine_manager_sysytem.entity.User;
import com.medicine.medicine_manager_sysytem.entity.UserNotice;
import com.medicine.medicine_manager_sysytem.mapper.UserMapper;
import com.medicine.medicine_manager_sysytem.mapper.UserNoticeMapper;
import com.medicine.medicine_manager_sysytem.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<UserNoticeMapper, UserNotice> implements NoticeService {

    private final UserMapper userMapper;

    @Override
    public Page<UserNotice> page(Page<UserNotice> page, Integer type, Boolean isRead) {
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            // 需要根据关联的 Notice 表查询类型
            // 这里简化处理，如果需要可以添加子查询
        }
        if (isRead != null) {
            wrapper.eq(UserNotice::getIsRead, isRead);
        }
        wrapper.orderByDesc(UserNotice::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public UserNotice getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotice::getUserId, userId)
                .eq(UserNotice::getIsRead, false);
        return Math.toIntExact(this.count(wrapper));
    }

    @Override
    public void markAsRead(Long noticeId, Long userId) {
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotice::getNoticeId, noticeId)
                .eq(UserNotice::getUserId, userId);

        UserNotice userNotice = this.getOne(wrapper);
        if (userNotice != null) {
            userNotice.setIsRead(true);
            this.updateById(userNotice);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotice::getUserId, userId)
                .eq(UserNotice::getIsRead, false);

        List<UserNotice> list = this.list(wrapper);
        for (UserNotice un : list) {
            un.setIsRead(true);
        }
        this.updateBatchById(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long noticeId, Long userId) {
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotice::getNoticeId, noticeId)
                .eq(UserNotice::getUserId, userId);
        this.remove(wrapper);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public int getTotalCount(Long userId) {
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotice::getUserId, userId);
        return Math.toIntExact(this.count(wrapper));
    }
}
