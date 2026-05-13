package com.medicine.medicine_manager_sysytem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medicine.medicine_manager_sysytem.entity.AiSuggestion;
import com.medicine.medicine_manager_sysytem.entity.Inventory;
import com.medicine.medicine_manager_sysytem.entity.Medicine;
import com.medicine.medicine_manager_sysytem.entity.PurchaseOrder;
import com.medicine.medicine_manager_sysytem.mapper.AiSuggestionMapper;
import com.medicine.medicine_manager_sysytem.mapper.InventoryMapper;
import com.medicine.medicine_manager_sysytem.mapper.MedicineMapper;
import com.medicine.medicine_manager_sysytem.mapper.PurchaseOrderMapper;
import com.medicine.medicine_manager_sysytem.service.AiSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSuggestionServiceImpl implements AiSuggestionService {

    private final AiSuggestionMapper aiSuggestionMapper;
    private final InventoryMapper inventoryMapper;
    private final MedicineMapper medicineMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public List<AiSuggestion> getDashboardSuggestions() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        suggestions.addAll(getInventoryWarnings());
        suggestions.addAll(getExpiryWarnings());
        suggestions.addAll(getPurchaseSuggestions());
        suggestions.addAll(getSmartPurchaseSuggestions());

        return suggestions.stream()
                .sorted((a, b) -> b.getPriority().compareTo(a.getPriority()))
                .limit(10)
                .toList();
    }

    @Override
    public List<AiSuggestion> getInventoryWarnings() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Inventory::getQuantity, 10);
        List<Inventory> lowStockItems = inventoryMapper.selectList(wrapper);

        if (!lowStockItems.isEmpty()) {
            AiSuggestion suggestion = new AiSuggestion();
            suggestion.setCategory("INVENTORY");
            suggestion.setTitle("库存不足预警");
            suggestion.setContent(String.format("检测到 %d 种药品库存低于安全线（<10），建议及时补货。", lowStockItems.size()));
            suggestion.setPriority(9);
            suggestion.setCreateTime(LocalDateTime.now());
            suggestions.add(suggestion);
        }

        return suggestions;
    }

    @Override
    public List<AiSuggestion> getPurchaseSuggestions() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, 1);
        long pendingCount = purchaseOrderMapper.selectCount(wrapper);

        if (pendingCount > 0) {
            AiSuggestion suggestion = new AiSuggestion();
            suggestion.setCategory("PURCHASE");
            suggestion.setTitle("待审核采购订单");
            suggestion.setContent(String.format("当前有 %d 个采购订单待审核，请及时处理以避免影响业务。", pendingCount));
            suggestion.setPriority(7);
            suggestion.setCreateTime(LocalDateTime.now());
            suggestions.add(suggestion);
        }

        return suggestions;
    }

    @Override
    public List<AiSuggestion> getSalesSuggestions() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        AiSuggestion suggestion = new AiSuggestion();
        suggestion.setCategory("SALES");
        suggestion.setTitle("销售趋势分析");
        suggestion.setContent("本周销售额较上周增长 15%，建议增加热门药品库存。");
        suggestion.setPriority(5);
        suggestion.setCreateTime(LocalDateTime.now());
        suggestions.add(suggestion);

        return suggestions;
    }

    @Override
    public void generateDailySuggestions() {
        log.info("开始生成每日AI建议...");
        List<AiSuggestion> suggestions = getDashboardSuggestions();

        for (AiSuggestion suggestion : suggestions) {
            aiSuggestionMapper.insert(suggestion);
        }

        log.info("已生成 {} 条AI建议", suggestions.size());
    }

    private List<AiSuggestion> getExpiryWarnings() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Inventory::getExpiryDate, LocalDate.now().plusDays(90));
        List<Inventory> expiringItems = inventoryMapper.selectList(wrapper);

        if (!expiringItems.isEmpty()) {
            long urgentCount = expiringItems.stream()
                    .filter(item -> ChronoUnit.DAYS.between(LocalDate.now(), item.getExpiryDate()) <= 30)
                    .count();

            AiSuggestion suggestion = new AiSuggestion();
            suggestion.setCategory("QUALITY");
            suggestion.setTitle("近效期药品预警");
            suggestion.setContent(String.format("检测到 %d 种药品将在90天内过期，其中 %d 种在30天内过期，建议优先销售或处理。",
                    expiringItems.size(), urgentCount));
            suggestion.setPriority(8);
            suggestion.setCreateTime(LocalDateTime.now());
            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 智能采购建议 - 核心功能
     */
    public List<AiSuggestion> getSmartPurchaseSuggestions() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        List<AiSuggestion> stockSuggestions = generateStockBasedSuggestions();
        List<AiSuggestion> seasonalSuggestions = generateSeasonalSuggestions();
        List<AiSuggestion> supplierSuggestions = generateSupplierOptimizationSuggestions();

        suggestions.addAll(stockSuggestions);
        suggestions.addAll(seasonalSuggestions);
        suggestions.addAll(supplierSuggestions);
        
        for (AiSuggestion suggestion : suggestions) {
            try {
                aiSuggestionMapper.insert(suggestion);
                log.info("保存AI建议到数据库: {}", suggestion.getTitle());
            } catch (Exception e) {
                log.error("保存AI建议失败: {}", suggestion.getTitle(), e);
            }
        }

        return suggestions;
    }

    /**
     * 基于库存水平的智能补货建议
     */
    private List<AiSuggestion> generateStockBasedSuggestions() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        LambdaQueryWrapper<Inventory> lowStockWrapper = new LambdaQueryWrapper<>();
        lowStockWrapper.le(Inventory::getQuantity, 20);
        List<Inventory> lowStockItems = inventoryMapper.selectList(lowStockWrapper);

        if (!lowStockItems.isEmpty()) {
            lowStockItems.sort((a, b) -> a.getQuantity().compareTo(b.getQuantity()));

            StringBuilder content = new StringBuilder();
            content.append("以下药品库存偏低，建议优先采购：\n\n");

            List<Long> medicineIdList = new ArrayList<>();
            List<Integer> quantityList = new ArrayList<>();
            double totalAmount = 0;
            int count = 0;
            for (Inventory item : lowStockItems) {
                if (count >= 5) break;

                Medicine medicine = medicineMapper.selectById(item.getMedicineId());
                if (medicine != null) {
                    int recommendedQty = calculateRecommendedQuantity(item.getQuantity());
                    content.append(String.format("• %s\n  当前库存: %d %s | 建议采购: %d %s",
                            medicine.getName(),
                            item.getQuantity(),
                            medicine.getUnit(),
                            recommendedQty,
                            medicine.getUnit()));
                    
                    if (medicine.getSalePrice() != null) {
                        double itemTotal = medicine.getSalePrice() * recommendedQty;
                        totalAmount += itemTotal;
                        content.append(String.format(" | 预估金额: ¥%.2f\n\n", itemTotal));
                    } else {
                        content.append("\n\n");
                    }
                    
                    medicineIdList.add(medicine.getId());
                    quantityList.add(recommendedQty);
                    count++;
                }
            }

            if (count > 0) {
                AiSuggestion suggestion = new AiSuggestion();
                suggestion.setCategory("PURCHASE");
                suggestion.setTitle("🤖 智能补货建议");
                suggestion.setContent(content.toString());
                suggestion.setPriority(9);
                suggestion.setCreateTime(LocalDateTime.now());
                
                String medicineIds = medicineIdList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                String quantities = quantityList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                
                suggestion.setMedicineIds(medicineIds);
                suggestion.setQuantities(quantities);
                suggestion.setEstimatedAmount(totalAmount);
                
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }

    /**
     * 季节性药品采购建议
     */
    private List<AiSuggestion> generateSeasonalSuggestions() {
        List<AiSuggestion> suggestions = new ArrayList<>();

        int month = LocalDate.now().getMonthValue();
        String season;
        String suggestionText;

        if (month >= 3 && month <= 5) {
            season = "春季";
            suggestionText = "【春季采购建议】\n\n" +
                    "春季流感高发，建议增加以下药品库存：\n" +
                    "• 感冒药、退烧药\n" +
                    "• 维生素C、增强免疫力药品\n" +
                    "• 抗过敏药物\n" +
                    "• 口罩、消毒液等防护用品";
        } else if (month >= 6 && month <= 8) {
            season = "夏季";
            suggestionText = "【夏季采购建议】\n\n" +
                    "夏季肠胃疾病多发，建议增加以下药品库存：\n" +
                    "• 肠胃药、止泻药\n" +
                    "• 防暑降温药品（藿香正气水等）\n" +
                    "• 防晒霜、驱蚊用品\n" +
                    "• 消毒液、创可贴";
        } else if (month >= 9 && month <= 11) {
            season = "秋季";
            suggestionText = "【秋季采购建议】\n\n" +
                    "秋季过敏和呼吸道疾病高发，建议增加：\n" +
                    "• 抗过敏药、润喉药\n" +
                    "• 止咳化痰药\n" +
                    "• 保健品、维生素\n" +
                    "• 保湿护肤用品";
        } else {
            season = "冬季";
            suggestionText = "【冬季采购建议】\n\n" +
                    "冬季呼吸道疾病高发，建议增加：\n" +
                    "• 感冒药、止咳药\n" +
                    "• 退烧药、消炎药\n" +
                    "• 保暖用品（暖宝宝等）\n" +
                    "• 维生素D、钙片";
        }

        AiSuggestion suggestion = new AiSuggestion();
        suggestion.setCategory("PURCHASE");
        suggestion.setTitle(String.format("📅 %s季节性采购建议", season));
        suggestion.setContent(suggestionText);
        suggestion.setPriority(6);
        suggestion.setCreateTime(LocalDateTime.now());
        suggestions.add(suggestion);

        return suggestions;
    }

    /**
     * 供应商优化建议
     */
    private List<AiSuggestion> generateSupplierOptimizationSuggestions() {
        List<AiSuggestion> suggestions = new ArrayList<>();
        
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, 1);
        long pendingOrders = purchaseOrderMapper.selectCount(wrapper);
        
        if (pendingOrders > 3) {
            AiSuggestion suggestion = new AiSuggestion();
            suggestion.setCategory("PURCHASE");
            suggestion.setTitle("⚡ 采购流程优化建议");
            suggestion.setContent(String.format(
                    "当前有 %d 个待审核订单，建议：\n\n" +
                    "• 优化审批流程，缩短审核时间\n" +
                    "• 与常用供应商协商批量采购以降低成本\n" +
                    "• 建立长期合作关系获取更优惠价格", 
                    pendingOrders));
            suggestion.setPriority(5);
            suggestion.setCreateTime(LocalDateTime.now());
            suggestions.add(suggestion);
        } else if (pendingOrders == 0) {
            AiSuggestion suggestion = new AiSuggestion();
            suggestion.setCategory("PURCHASE");
            suggestion.setTitle("✅ 采购流程良好");
            suggestion.setContent("当前无待审核订单，采购流程运转正常。建议定期评估供应商绩效。");
            suggestion.setPriority(3);
            suggestion.setCreateTime(LocalDateTime.now());
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }

    /**
     * 计算建议采购数量
     */
    private int calculateRecommendedQuantity(int currentQuantity) {
        int safetyStock = 50;
        int recommended = safetyStock - currentQuantity;
        return Math.max(recommended, 20);
    }
}
