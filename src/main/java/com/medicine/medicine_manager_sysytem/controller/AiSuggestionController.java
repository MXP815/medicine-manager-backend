package com.medicine.medicine_manager_sysytem.controller;

import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.AiSuggestion;
import com.medicine.medicine_manager_sysytem.service.AiSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI智能建议", description = "AI智能分析与建议接口")
@Slf4j
public class AiSuggestionController {

    private final AiSuggestionService aiSuggestionService;

    @GetMapping("/dashboard-suggestions")
    @Operation(summary = "获取仪表盘智能建议")
    public Result<List<AiSuggestion>> getDashboardSuggestions() {
        try {
            List<AiSuggestion> suggestions = aiSuggestionService.getDashboardSuggestions();
            return Result.success(suggestions != null ? suggestions : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取仪表盘建议失败", e);
            return Result.success(new ArrayList<>());
        }
    }

    @GetMapping("/inventory-warnings")
    @Operation(summary = "获取库存预警建议")
    public Result<List<AiSuggestion>> getInventoryWarnings() {
        try {
            List<AiSuggestion> suggestions = aiSuggestionService.getInventoryWarnings();
            return Result.success(suggestions != null ? suggestions : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取库存预警失败", e);
            return Result.success(new ArrayList<>());
        }
    }

    @GetMapping("/purchase-suggestions")
    @Operation(summary = "获取采购建议")
    public Result<List<AiSuggestion>> getPurchaseSuggestions() {
        try {
            List<AiSuggestion> suggestions = aiSuggestionService.getPurchaseSuggestions();
            return Result.success(suggestions != null ? suggestions : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取采购建议失败", e);
            return Result.success(new ArrayList<>());
        }
    }

    @GetMapping("/smart-purchase-suggestions")
    @Operation(summary = "获取智能采购建议")
    public Result<List<AiSuggestion>> getSmartPurchaseSuggestions() {
        try {
            List<AiSuggestion> suggestions = aiSuggestionService.getSmartPurchaseSuggestions();
            return Result.success(suggestions != null ? suggestions : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取智能采购建议失败", e);
            return Result.success(new ArrayList<>());
        }
    }

    @GetMapping("/sales-suggestions")
    @Operation(summary = "获取销售建议")
    public Result<List<AiSuggestion>> getSalesSuggestions() {
        try {
            List<AiSuggestion> suggestions = aiSuggestionService.getSalesSuggestions();
            return Result.success(suggestions != null ? suggestions : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取销售建议失败", e);
            return Result.success(new ArrayList<>());
        }
    }
}
