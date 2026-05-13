package com.medicine.medicine_manager_sysytem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.common.PageQuery;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.UserNotice;
import com.medicine.medicine_manager_sysytem.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "系统通知管理接口")
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    @Operation(summary = "通知列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Result<PageResult<UserNotice>> list(PageQuery query, 
                                               @RequestParam(required = false) Integer type,
                                               @RequestParam(required = false) Boolean isRead) {
        Page<UserNotice> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<UserNotice> result = noticeService.page(page, type, isRead);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通知详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Result<Object> getById(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读通知数量")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Result<Map<String, Integer>> unreadCount(Principal principal) {
        try {
            if (principal == null) {
                Map<String, Integer> data = new HashMap<>();
                data.put("count", 0);
                return Result.success(data);
            }
            
            String username = principal.getName();
            var user = noticeService.getUserByUsername(username);
            Long userId = user != null ? user.getId() : 0L;
            Map<String, Integer> data = new HashMap<>();
            data.put("count", noticeService.getUnreadCount(userId));
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            Map<String, Integer> data = new HashMap<>();
            data.put("count", 0);
            return Result.success(data);
        }
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记为已读")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Result<Void> markAsRead(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        var user = noticeService.getUserByUsername(username);
        Long userId = user != null ? user.getId() : 0L;
        noticeService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记为已读")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Result<Void> markAllAsRead(Principal principal) {
        String username = principal.getName();
        var user = noticeService.getUserByUsername(username);
        Long userId = user != null ? user.getId() : 0L;
        noticeService.markAllAsRead(userId);
        return Result.success();
    }

    @PostMapping("/{id}/delete")
    @Operation(summary = "删除通知")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Result<Void> delete(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        var user = noticeService.getUserByUsername(username);
        Long userId = user != null ? user.getId() : 0L;
        noticeService.delete(id, userId);
        return Result.success();
    }

    @GetMapping("/statistics")
    @Operation(summary = "通知统计")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Result<Map<String, Integer>> statistics(Principal principal) {
        String username = principal.getName();
        var user = noticeService.getUserByUsername(username);
        Long userId = user != null ? user.getId() : 0L;
        
        int unreadCount = noticeService.getUnreadCount(userId);
        int totalCount = noticeService.getTotalCount(userId);
        int readCount = totalCount - unreadCount;
        
        Map<String, Integer> data = new HashMap<>();
        data.put("totalCount", totalCount);
        data.put("unreadCount", unreadCount);
        data.put("readCount", readCount);
        
        return Result.success(data);
    }
}
