package com.medicine.medicine_manager_sysytem.task;

import com.medicine.medicine_manager_sysytem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryWarningTask {

    private final InventoryService inventoryService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void updateInventoryWarning() {
        log.info("开始执行库存效期预警定时任务...");
        try {
            inventoryService.updateWarningStatus();
            log.info("库存效期预警更新完成");
        } catch (Exception e) {
            log.error("库存效期预警更新失败", e);
        }
    }
}
