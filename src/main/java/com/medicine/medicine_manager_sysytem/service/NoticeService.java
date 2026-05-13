package com.medicine.medicine_manager_sysytem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.entity.User;
import com.medicine.medicine_manager_sysytem.entity.UserNotice;
import java.util.List;

public interface NoticeService {

    Page<UserNotice> page(Page<UserNotice> page, Integer type, Boolean isRead);

    UserNotice getById(Long id);

    int getUnreadCount(Long userId);

    void markAsRead(Long noticeId, Long userId);

    void markAllAsRead(Long userId);

    void delete(Long noticeId, Long userId);

    User getUserByUsername(String username);
    int getTotalCount(Long userId);

}
