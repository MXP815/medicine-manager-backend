package com.medicine.medicine_manager_sysytem.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medicine.medicine_manager_sysytem.DTO.PurchaseOrderDTO;
import com.medicine.medicine_manager_sysytem.DTO.PurchaseOrderItemDTO;
import com.medicine.medicine_manager_sysytem.VO.PurchaseOrderVO;
import com.medicine.medicine_manager_sysytem.common.PageResult;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.entity.*;
import com.medicine.medicine_manager_sysytem.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchase-orders")
@Tag(name = "采购订单管理")
@Slf4j
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private MedicineMapper medicineMapper;

    @Autowired
    private AiSuggestionMapper aiSuggestionMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @GetMapping("/page")
    @Operation(summary = "获取采购订单列表")
    public Result<PageResult<PurchaseOrderVO>> list(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            log.info("=== 开始查询采购订单列表，参数：pageNum={}, pageSize={}, status={} ===", 
                    pageNum, pageSize, status);
            
            QueryWrapper<PurchaseOrder> wrapper = Wrappers.query();
            if (orderNo != null && !orderNo.isEmpty()) {
                wrapper.like("order_no", orderNo);
            }
            if (supplierId != null) {
                wrapper.eq("supplier_id", supplierId);
            }
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("create_time");
            
            log.info("执行数据库查询...");
            Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
            Page<PurchaseOrder> result = purchaseOrderMapper.selectPage(page, wrapper);
            
            log.info("查询成功，总数: {}, 当前页数据量: {}", 
                    result.getTotal(), result.getRecords().size());
            
            List<PurchaseOrderVO> voList = result.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            PageResult<PurchaseOrderVO> pageResult = PageResult.of(
                    voList,
                    result.getTotal(),
                    result.getSize(),
                    result.getCurrent()
            );
            
            log.info("返回数据成功，已转换{}条VO记录", voList.size());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("❌ 查询采购订单列表失败，错误类型: {}, 错误信息: {}", 
                    e.getClass().getName(), e.getMessage(), e);
            return Result.error("系统繁忙，获取数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取采购订单详情")
    public Result<PurchaseOrder> detail(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping("/items/{orderId}")
    @Operation(summary = "获取采购订单项列表")
    public Result<List<PurchaseOrderItem>> items(@PathVariable Long orderId) {
        QueryWrapper<PurchaseOrderItem> wrapper = Wrappers.query();
        wrapper.eq("order_id", orderId);
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(wrapper);
        return Result.success(items);
    }

    @PostMapping
    @Operation(summary = "创建采购订单")
    public Result<Long> create(@RequestBody PurchaseOrderDTO dto, Authentication authentication) {
        try {
            log.info("=== 开始创建采购订单 ===");
            
            Long creatorId = resolveCreatorId(authentication);
            log.info("👤 创建人ID: {}", creatorId);
            
            Long supplierId = resolveSupplierId(dto.getSupplierId());
            if (supplierId == null) {
                return Result.error("供应商不存在，请先在供应商管理中创建供应商");
            }
            
            PurchaseOrder order = new PurchaseOrder();
            order.setOrderNo(generateOrderNo());
            order.setSupplierId(supplierId);
            order.setStatus(0);
            order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
            order.setContractPath(dto.getContractPath());
            order.setRemark(dto.getRemark());
            order.setCreatorId(creatorId);
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalCount = 0;
            
            purchaseOrderMapper.insert(order);
            log.info("✅ 订单主表创建成功，订单ID: {}", order.getId());
            
            if (dto.getItems() != null && !dto.getItems().isEmpty()) {
                for (PurchaseOrderItemDTO itemDTO : dto.getItems()) {
                    Medicine medicine = medicineMapper.selectById(itemDTO.getMedicineId());
                    if (medicine == null) {
                        log.error("❌ 药品不存在，药品ID: {}", itemDTO.getMedicineId());
                        return Result.error("药品不存在，ID: " + itemDTO.getMedicineId());
                    }
                    
                    PurchaseOrderItem item = new PurchaseOrderItem();
                    item.setOrderId(order.getId());
                    item.setMedicineId(itemDTO.getMedicineId());
                    item.setQuantity(itemDTO.getQuantity());
                    item.setUnitPrice(BigDecimal.valueOf(itemDTO.getUnitPrice()));
                    item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
                    
                    String batchNumber = itemDTO.getBatchNumber();
                    if (batchNumber == null || batchNumber.isEmpty()) {
                        batchNumber = "BATCH" + System.currentTimeMillis();
                    }
                    item.setBatchNumber(batchNumber);
                    
                    item.setProductionDate(itemDTO.getProductionDate());
                    
                    LocalDate expiryDate = itemDTO.getExpiryDate();
                    if (expiryDate == null) {
                        expiryDate = LocalDate.now().plusMonths(12);
                    }
                    item.setExpiryDate(expiryDate);
                    
                    purchaseOrderItemMapper.insert(item);
                    
                    totalAmount = totalAmount.add(item.getTotalPrice());
                    totalCount += itemDTO.getQuantity();
                    
                    log.info("📦 添加订单项 - 药品: {}, 数量: {}, 单价: {}, 总价: {}", 
                            medicine.getName(), itemDTO.getQuantity(), item.getUnitPrice(), item.getTotalPrice());
                }
                
                order.setTotalAmount(totalAmount);
                order.setTotalCount(totalCount);
                purchaseOrderMapper.updateById(order);
                
                log.info("✅ 采购订单创建完成 - 订单ID: {}, 创建人ID: {}, 明细数: {}, 总金额: {}, 总数量: {}", 
                        order.getId(), creatorId, dto.getItems().size(), totalAmount, totalCount);
            } else {
                log.info("⚠️ 订单明细为空，创建空订单");
            }
            
            return Result.success(order.getId());
        } catch (Exception e) {
            log.error("❌ 创建采购订单失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新采购订单")
    public Result<Void> update(@PathVariable Long id, @RequestBody PurchaseOrder order) {
        try {
            order.setId(id);
            purchaseOrderMapper.updateById(order);
            log.info("更新采购订单成功，订单ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("更新采购订单失败，订单ID: {}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除采购订单")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            log.info("=== 开始删除订单，ID: {} ===", id);
            
            PurchaseOrder order = purchaseOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (order.getStatus() >= 2) {
                return Result.error("已审批的订单不能删除");
            }
            
            purchaseOrderMapper.deleteById(id);
            log.info("✅ 采购订单已删除，订单ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("❌ 删除采购订单失败，订单ID: {}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过采购订单")
    public Result<Void> approve(@PathVariable Long id) {
        try {
            log.info("=== 开始审批通过订单，ID: {} ===", id);
            
            PurchaseOrder order = purchaseOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (order.getStatus() != 0 && order.getStatus() != 1) {
                return Result.error("订单状态不允许审批");
            }
            
            order.setStatus(2);
            purchaseOrderMapper.updateById(order);
            
            log.info("✅ 采购订单审批通过成功，订单ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("❌ 审批采购订单失败，订单ID: {}", id, e);
            return Result.error("审批失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝采购订单")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        try {
            log.info("=== 开始拒绝订单，ID: {} ===", id);
            
            PurchaseOrder order = purchaseOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (order.getStatus() != 0 && order.getStatus() != 1) {
                return Result.error("订单状态不允许拒绝");
            }
            
            String reason = "未说明原因";
            if (request != null && request.containsKey("reason")) {
                reason = request.get("reason");
            }
            
            order.setStatus(3);
            order.setRejectReason(reason);
            purchaseOrderMapper.updateById(order);
            
            log.info("✅ 采购订单拒绝成功，订单ID: {}, 原因: {}", id, reason);
            return Result.success();
        } catch (Exception e) {
            log.error("❌ 拒绝采购订单失败，订单ID: {}", id, e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消采购订单")
    public Result<Void> cancel(@PathVariable Long id) {
        try {
            PurchaseOrder order = purchaseOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (order.getStatus() >= 2) {
                return Result.error("订单已审批，无法取消");
            }
            
            order.setStatus(5);
            purchaseOrderMapper.updateById(order);
            
            log.info("采购订单已取消，订单ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("取消采购订单失败，订单ID: {}", id, e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-inbound")
    @Operation(summary = "将通过的采购订单一键入库")
    public Result<Map<String, Object>> batchInbound() {
        try {
            log.info("=== 开始批量入库已通过订单 ===");
            
            QueryWrapper<PurchaseOrder> wrapper = Wrappers.query();
            wrapper.eq("status", 2);
            List<PurchaseOrder> approvedOrders = purchaseOrderMapper.selectList(wrapper);
            
            if (approvedOrders.isEmpty()) {
                return Result.error("没有待入库的订单");
            }
            
            int successCount = 0;
            int failCount = 0;
            int totalItemsCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            for (PurchaseOrder order : approvedOrders) {
                try {
                    QueryWrapper<PurchaseOrderItem> itemWrapper = Wrappers.query();
                    itemWrapper.eq("order_id", order.getId());
                    List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(itemWrapper);
                    
                    if (items.isEmpty()) {
                        log.warn("⚠️ 订单 {} 没有明细，跳过", order.getOrderNo());
                        continue;
                    }
                    
                    for (PurchaseOrderItem item : items) {
                        Medicine medicine = medicineMapper.selectById(item.getMedicineId());
                        if (medicine == null) {
                            log.warn("⚠️ 药品不存在，跳过，药品ID: {}", item.getMedicineId());
                            continue;
                        }
                        
                        QueryWrapper<Inventory> invWrapper = Wrappers.query();
                        invWrapper.eq("medicine_id", item.getMedicineId())
                                  .eq("batch_number", item.getBatchNumber())
                                  .eq("deleted", 0);
                        Inventory existingInventory = inventoryMapper.selectOne(invWrapper);
                        
                        if (existingInventory != null) {
                            existingInventory.setQuantity(existingInventory.getQuantity() + item.getQuantity());
                            existingInventory.setUpdateTime(LocalDateTime.now());
                            inventoryMapper.updateById(existingInventory);
                            log.info("✅ 更新库存 - 药品: {}, 批次: {}, 新增: {}, 当前总量: {}", 
                                    medicine.getName(), item.getBatchNumber(), item.getQuantity(), existingInventory.getQuantity());
                        } else {
                            Inventory newInventory = new Inventory();
                            newInventory.setMedicineId(item.getMedicineId());
                            newInventory.setBatchNumber(item.getBatchNumber());
                            newInventory.setQuantity(item.getQuantity());
                            newInventory.setExpiryDate(item.getExpiryDate());
                            newInventory.setWarehouseLocation("默认仓库");
                            newInventory.setMinStock(10);
                            newInventory.setStatus(1);
                            newInventory.setWarningStatus(0);
                            inventoryMapper.insert(newInventory);
                            log.info("✅ 新建库存 - 药品: {}, 批次: {}, 数量: {}", 
                                    medicine.getName(), item.getBatchNumber(), item.getQuantity());
                        }
                        
                        totalItemsCount++;
                    }
                    
                    order.setStatus(4);
                    purchaseOrderMapper.updateById(order);
                    successCount++;
                    
                    log.info("✅ 订单 {} 入库完成，共{}项", order.getOrderNo(), items.size());
                } catch (Exception e) {
                    failCount++;
                    String errorMsg = String.format("订单 %s 入库失败: %s", order.getOrderNo(), e.getMessage());
                    errorMessages.add(errorMsg);
                    log.error("❌ {}", errorMsg, e);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalOrders", approvedOrders.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalItemsCount", totalItemsCount);
            result.put("errorMessages", errorMessages);
            
            log.info("✅ 批量入库完成 - 总订单数: {}, 成功: {}, 失败: {}, 总明细数: {}", 
                    approvedOrders.size(), successCount, failCount, totalItemsCount);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("❌ 批量入库失败", e);
            return Result.error("批量入库失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取采购订单详情")
    public Result<PurchaseOrder> getById(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取采购订单统计数据")
    public Result<Map<String, Object>> getStatistics() {
        try {
            log.info("=== 开始获取采购订单统计数据 ===");
            
            Map<String, Object> statistics = new HashMap<>();
            
            long totalOrders = purchaseOrderMapper.selectCount(null);
            long pendingOrders = purchaseOrderMapper.selectCount(
                new QueryWrapper<PurchaseOrder>().eq("status", 0)
            );
            long submittedOrders = purchaseOrderMapper.selectCount(
                new QueryWrapper<PurchaseOrder>().eq("status", 1)
            );
            long approvedOrders = purchaseOrderMapper.selectCount(
                new QueryWrapper<PurchaseOrder>().eq("status", 2)
            );
            long rejectedOrders = purchaseOrderMapper.selectCount(
                new QueryWrapper<PurchaseOrder>().eq("status", 3)
            );
            long completedOrders = purchaseOrderMapper.selectCount(
                new QueryWrapper<PurchaseOrder>().eq("status", 4)
            );
            
            statistics.put("totalOrders", totalOrders);
            statistics.put("pendingOrders", pendingOrders);
            statistics.put("submittedOrders", submittedOrders);
            statistics.put("approvedOrders", approvedOrders);
            statistics.put("rejectedOrders", rejectedOrders);
            statistics.put("completedOrders", completedOrders);
            
            log.info("统计数据获取成功: {}", statistics);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("❌ 获取采购订单统计数据失败", e);
            // 返回默认值，避免前端报错
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalOrders", 0);
            defaultStats.put("pendingOrders", 0);
            defaultStats.put("submittedOrders", 0);
            defaultStats.put("approvedOrders", 0);
            defaultStats.put("rejectedOrders", 0);
            defaultStats.put("completedOrders", 0);
            return Result.success(defaultStats);
        }
    }

    @PostMapping("/from-suggestion/{suggestionId}")
    @Operation(summary = "根据AI建议创建采购订单")
    public Result<Long> createFromSuggestion(@PathVariable Long suggestionId, Authentication authentication) {
        try {
            log.info("=== 开始从AI建议创建订单，建议ID: {} ===", suggestionId);
            
            if (suggestionId == null || suggestionId <= 0) {
                return Result.error("无效的建议ID");
            }
            
            AiSuggestion suggestion = aiSuggestionMapper.selectById(suggestionId);
            if (suggestion == null) {
                log.error("建议不存在，ID: {}", suggestionId);
                return Result.error("建议不存在");
            }
            
            log.info("建议信息: 标题={}, 类别={}, medicineIds={}, supplierId={}", 
                    suggestion.getTitle(), suggestion.getCategory(), suggestion.getMedicineIds(), suggestion.getSupplierId());
            
            if (suggestion.getMedicineIds() == null || suggestion.getMedicineIds().isEmpty()) {
                log.warn("该建议不包含药品信息，无法创建订单");
                return Result.error("该建议不包含具体的药品信息，无法自动创建订单。请选择'智能补货建议'类型的建议。");
            }
            
            Long creatorId = resolveCreatorId(authentication);
            log.info("👤 解析后的创建人ID: {}", creatorId);
            
            PurchaseOrder order = new PurchaseOrder();
            order.setOrderNo(generateOrderNo());
            
            Long supplierId = resolveSupplierId(suggestion.getSupplierId());
            if (supplierId == null) {
                return Result.error("系统中没有找到任何供应商，请先在供应商管理中创建供应商");
            }
            order.setSupplierId(supplierId);
            
            order.setStatus(0);
            order.setCreatorId(creatorId);
            order.setRemark("AI建议自动生成 (建议ID: " + suggestionId + ") - " + suggestion.getTitle());
            
            log.info("📝 订单基本信息 - 订单号: {}, 供应商ID: {}, 创建人ID: {}", 
                    order.getOrderNo(), order.getSupplierId(), order.getCreatorId());
            
            if (order.getCreatorId() == null) {
                log.error("❌ 创建人ID为null，拒绝创建订单");
                return Result.error("系统错误：无法获取创建人信息，请重新登录");
            }

            String[] medicineIdArray = suggestion.getMedicineIds().split(",");
            String[] quantityArray = suggestion.getQuantities() != null ? suggestion.getQuantities().split(",") : null;
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalCount = 0;
            int successCount = 0;
            
            purchaseOrderMapper.insert(order);
            log.info("✅ 订单主表创建成功，订单ID: {}", order.getId());
            
            for (int i = 0; i < medicineIdArray.length; i++) {
                try {
                    Long medicineId = Long.parseLong(medicineIdArray[i].trim());
                    Integer quantity = quantityArray != null && i < quantityArray.length 
                            ? Integer.parseInt(quantityArray[i].trim()) 
                            : 10;
                    
                    Medicine medicine = medicineMapper.selectById(medicineId);
                    if (medicine != null) {
                        PurchaseOrderItem item = new PurchaseOrderItem();
                        item.setOrderId(order.getId());
                        item.setMedicineId(medicineId);
                        item.setQuantity(quantity);
                        item.setUnitPrice(medicine.getPurchasePrice() != null ? BigDecimal.valueOf(medicine.getPurchasePrice()) : BigDecimal.ZERO);
                        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                        item.setBatchNumber("BATCH" + System.currentTimeMillis() + i);
                        item.setExpiryDate(java.time.LocalDate.now().plusMonths(12));
                        
                        purchaseOrderItemMapper.insert(item);
                        
                        totalAmount = totalAmount.add(item.getTotalPrice());
                        totalCount += quantity;
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("处理药品ID时出错: {}", medicineIdArray[i], e);
                }
            }
            
            order.setTotalAmount(totalAmount);
            order.setTotalCount(totalCount);
            purchaseOrderMapper.updateById(order);
            
            log.info("✅ 订单创建完成 - 订单ID: {}, 创建人ID: {}, 药品数: {}, 总金额: {}", 
                    order.getId(), creatorId, successCount, totalAmount);
            
            if (successCount == 0) {
                return Result.error("未能添加任何药品到订单中，请检查药品是否存在");
            }
            
            return Result.success(order.getId());
        } catch (Exception e) {
            log.error("❌ 创建订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    private Long resolveCreatorId(Authentication authentication) {
        try {
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                log.info("✅ 从认证信息获取用户: ID={}, 用户名={}", user.getId(), user.getUsername());
                return user.getId();
            }
        } catch (Exception e) {
            log.warn("从认证信息获取用户失败", e);
        }
        
        User adminUser = userMapper.selectOne(
            Wrappers.<User>query()
                .eq("role", 1)
                .eq("status", 1)
                .eq("deleted", 0)
                .orderByAsc("id")
                .last("LIMIT 1")
        );
        
        if (adminUser != null) {
            log.info("🔄 使用默认管理员: ID={}, 用户名={}", adminUser.getId(), adminUser.getUsername());
            return adminUser.getId();
        }
        
        throw new RuntimeException("系统中没有可用用户");
    }

    private Long resolveSupplierId(Long suggestedSupplierId) {
        if (suggestedSupplierId != null && suggestedSupplierId > 0) {
            Supplier supplier = supplierMapper.selectById(suggestedSupplierId);
            if (supplier != null) {
                log.info("✅ 使用建议的供应商: ID={}, 名称={}", supplier.getId(), supplier.getName());
                return supplier.getId();
            }
        }
        
        Supplier defaultSupplier = supplierMapper.selectOne(
            Wrappers.<Supplier>query()
                .eq("deleted", 0)
                .orderByAsc("id")
                .last("LIMIT 1")
        );
        
        if (defaultSupplier != null) {
            log.info("🔄 使用默认供应商: ID={}, 名称={}", defaultSupplier.getId(), defaultSupplier.getName());
            return defaultSupplier.getId();
        }
        
        return null;
    }

    private String generateOrderNo() {
        return "PO" + System.currentTimeMillis();
    }

    private PurchaseOrderVO convertToVO(PurchaseOrder order) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        BeanUtils.copyProperties(order, vo);
        
        if (order.getSupplierId() != null && order.getSupplierId() > 0) {
            Supplier supplier = supplierMapper.selectById(order.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getName());
            } else {
                vo.setSupplierName("未知供应商");
            }
        } else {
            vo.setSupplierName("未设置供应商");
        }
        
        if (order.getCreatorId() != null && order.getCreatorId() > 0) {
            User creator = userMapper.selectById(order.getCreatorId());
            if (creator != null) {
                String creatorName = creator.getRealName() != null && !creator.getRealName().isEmpty() 
                    ? creator.getRealName() 
                    : creator.getUsername();
                vo.setCreatorName(creatorName);
            } else {
                vo.setCreatorName("未知用户");
            }
        } else {
            vo.setCreatorName("未设置创建人");
        }
        
        if (order.getApproverId() != null && order.getApproverId() > 0) {
            User approver = userMapper.selectById(order.getApproverId());
            if (approver != null) {
                String approverName = approver.getRealName() != null && !approver.getRealName().isEmpty() 
                    ? approver.getRealName() 
                    : approver.getUsername();
                vo.setApproverName(approverName);
            }
        }
        
        return vo;
    }
}

