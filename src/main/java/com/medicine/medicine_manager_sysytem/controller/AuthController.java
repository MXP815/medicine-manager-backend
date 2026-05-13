package com.medicine.medicine_manager_sysytem.controller;

import com.medicine.medicine_manager_sysytem.DTO.RegisterRequest;
import com.medicine.medicine_manager_sysytem.common.Result;
import com.medicine.medicine_manager_sysytem.DTO.LoginRequest;
import com.medicine.medicine_manager_sysytem.entity.User;
import com.medicine.medicine_manager_sysytem.mapper.UserMapper;
import com.medicine.medicine_manager_sysytem.service.EmailService;
import com.medicine.medicine_manager_sysytem.service.SmsService;
import com.medicine.medicine_manager_sysytem.service.VerificationCodeService;
import com.medicine.medicine_manager_sysytem.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户认证接口")
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
    private final SmsService smsService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        
        if (user == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        
        if (!matches) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BadCredentialsException("用户已被禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", getRoleCode(user.getRole()));
        
        String token = jwtUtil.generateTokenWithClaims(user.getId(), user.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("refreshToken", refreshToken);
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("avatar", user.getAvatar());
        data.put("role", getRoleCode(user.getRole()));

        return Result.success(data);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BadCredentialsException("用户名已存在");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            User existingEmail = userMapper.selectByEmail(request.getEmail());
            if (existingEmail != null) {
                throw new BadCredentialsException("邮箱已被注册");
            }
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            User existingPhone = userMapper.selectByPhone(request.getPhone());
            if (existingPhone != null) {
                throw new BadCredentialsException("手机号已被注册");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        
        // 注册时只能分配普通角色，不能直接注册为管理员
        Integer role = request.getRole();
        if (role == null || role == 1) {
            role = 2; // 默认角色：药师
        }
        // 限制可注册的角色范围（排除管理员）
        if (role < 2 || role > 7) {
            role = 2;
        }
        user.setRole(role);
        user.setStatus(1);

        userMapper.insert(user);

        return Result.success(user.getId());
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/currentUser")
    @Operation(summary = "获取当前用户信息")
    public Result<Map<String, Object>> getCurrentUser(Principal principal) {
        if (principal == null) {
            log.warn("⚠️ Principal 为 null，用户未登录");
            return Result.error("未登录或登录已过期");
        }
        
        String username = principal.getName();
        log.debug("📋 获取当前用户信息 - principal类型: {}, username: {}", 
                principal.getClass().getName(), username);
        
        User user = null;
        
        if (principal instanceof org.springframework.security.core.Authentication) {
            Object principalObj = ((org.springframework.security.core.Authentication) principal).getPrincipal();
            if (principalObj instanceof User) {
                user = (User) principalObj;
                log.debug("✅ 从 Authentication 中直接获取 User 对象 - ID: {}, 用户名: {}", 
                        user.getId(), user.getUsername());
            }
        }
        
        if (user == null) {
            user = userMapper.selectByUsername(username);
            if (user != null) {
                log.debug("✅ 通过 username 查询到用户 - ID: {}, 用户名: {}", 
                        user.getId(), user.getUsername());
            }
        }
        
        if (user == null) {
            log.error("❌ 用户不存在 - username: {}", username);
            throw new BadCredentialsException("用户不存在: " + username);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("department", user.getDepartment());
        data.put("role", user.getRole());
        data.put("avatar", user.getAvatar());
        
        log.info("✅ 返回用户信息 - ID: {}, 用户名: {}", user.getId(), user.getUsername());
        return Result.success(data);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token")
    public Result<Map<String, String>> refreshToken(@RequestParam String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadCredentialsException("Refresh Token 不能为空");
        }
        
        try {
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            
            User user = userMapper.selectById(userId);
            if (user == null || (user.getStatus() != null && user.getStatus() != 1)) {
                throw new BadCredentialsException("用户不存在或已被禁用");
            }
            
            String newToken = jwtUtil.generateToken(userId, username);
            
            Map<String, String> data = new HashMap<>();
            data.put("token", newToken);
            
            return Result.success(data);
        } catch (Exception e) {
            throw new BadCredentialsException("Refresh Token 已过期或无效");
        }
    }

    @PostMapping("/changePassword")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword,
                                       Principal principal) {
        String username = principal.getName();
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new BadCredentialsException("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        return Result.success();
    }

    @PostMapping("/forgotPassword/email/sendCode")
    @Operation(summary = "忘记密码-发送邮件验证码")
    public Result<Void> sendEmailCode(@RequestParam String username, @RequestParam String email) {
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new BadCredentialsException("用户不存在");
        }
        
        if (!email.equals(user.getEmail())) {
            throw new BadCredentialsException("邮箱与注册邮箱不匹配");
        }
        
        String code = verificationCodeService.generateCode("EMAIL_RESET", email);
        emailService.sendPasswordResetEmail(email, code);
        
        return Result.success();
    }

    @PostMapping("/forgotPassword/sms/sendCode")
    @Operation(summary = "忘记密码-发送短信验证码")
    public Result<Void> sendSmsCode(@RequestParam String username, @RequestParam String phone) {
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new BadCredentialsException("用户不存在");
        }
        
        if (!phone.equals(user.getPhone())) {
            throw new BadCredentialsException("手机号与注册手机号不匹配");
        }
        
        String code = verificationCodeService.generateCode("SMS_RESET", phone);
        smsService.sendVerificationCode(phone, code);
        
        return Result.success();
    }

    @PostMapping("/forgotPassword/reset")
    @Operation(summary = "忘记密码-重置密码")
    public Result<Void> resetPassword(@RequestParam String type,
                                      @RequestParam String target,
                                      @RequestParam String code,
                                      @RequestParam String newPassword) {
        boolean verified = verificationCodeService.verifyCode(type, target, code);
        
        if (!verified) {
            throw new BadCredentialsException("验证码错误或已过期");
        }
        
        String username = null;
        if ("EMAIL_RESET".equals(type)) {
            User user = userMapper.selectByEmail(target);
            if (user != null) {
                username = user.getUsername();
            }
        } else if ("SMS_RESET".equals(type)) {
            User user = userMapper.selectByPhone(target);
            if (user != null) {
                username = user.getUsername();
            }
        }
        
        if (username == null) {
            throw new BadCredentialsException("用户不存在");
        }
        
        User user = userMapper.selectByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        return Result.success();
    }

    @PostMapping("/updateProfile")
    @Operation(summary = "更新个人资料")
    public Result<Void> updateProfile(@RequestParam(required = false) String realName,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phone,
                                      @RequestParam(required = false) String department,
                                      Principal principal) {
        String username = principal.getName();
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new BadCredentialsException("用户不存在");
        }
        
        if (realName != null) {
            user.setRealName(realName);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (department != null) {
            user.setDepartment(department);
        }
        
        userMapper.updateById(user);
        return Result.success();
    }

    @GetMapping("/menus")
    @Operation(summary = "获取用户菜单权限")
    public Result<List<Map<String, Object>>> getMenus(Principal principal) {
        if (principal == null) {
            log.warn("⚠️ Principal 为 null，用户未登录");
            return Result.error("未登录或登录已过期");
        }
        
        String username = principal.getName();
        log.info("📋 获取菜单权限 - principal类型: {}, username: {}", 
                principal.getClass().getName(), username);
        
        User user = null;
        
        if (principal instanceof org.springframework.security.core.Authentication) {
            Object principalObj = ((org.springframework.security.core.Authentication) principal).getPrincipal();
            if (principalObj instanceof User) {
                user = (User) principalObj;
                log.info("✅ 从 Authentication 中直接获取 User 对象 - ID: {}, 用户名: {}", 
                        user.getId(), user.getUsername());
            }
        }
        
        if (user == null) {
            user = userMapper.selectByUsername(username);
            if (user != null) {
                log.info("✅ 通过 username 查询到用户 - ID: {}, 用户名: {}", 
                        user.getId(), user.getUsername());
            }
        }
        
        if (user == null) {
            log.error("❌ 用户不存在 - username: {}", username);
            return Result.error("用户不存在: " + username);
        }
        
        log.info("✅ 找到用户 - ID: {}, 用户名: {}, 角色: {}", user.getId(), user.getUsername(), user.getRole());
        
        if (user.getRole() == null) {
            log.error("❌ 用户角色未配置 - username: {}", username);
            return Result.error("用户角色未配置: " + username);
        }
        
        String roleCode = getRoleCode(user.getRole());
        log.info("🎭 角色代码: {}", roleCode);
        
        List<Map<String, Object>> menus = switch (roleCode) {
            case "ADMIN" -> List.of(
                createMenu("Dashboard", "首页", "/dashboard"),
                createMenu("BasicData", "基础数据", "/basic-data"),
                createMenu("Purchase", "采购管理", "/purchase"),
                createMenu("Warehouse", "仓储管理", "/warehouse"),
                createMenu("Sales", "销售管理", "/sales"),
                createMenu("Quality", "质量管理", "/quality"),
                createMenu("Finance", "财务管理", "/finance"),
                createMenu("System", "系统管理", "/system")
            );
            case "PHARMACIST" -> List.of(
                createMenu("Dashboard", "首页", "/dashboard"),
                createMenu("BasicData", "基础数据", "/basic-data")
            );
            case "PURCHASER" -> List.of(
                createMenu("Dashboard", "首页", "/dashboard"),
                createMenu("Purchase", "采购管理", "/purchase")
            );
            case "SALES" -> List.of(
                createMenu("Dashboard", "首页", "/dashboard"),
                createMenu("Sales", "销售管理", "/sales"),
                createMenu("Customer", "客户管理", "/customer")
            );
            case "WAREHOUSE" -> List.of(
                createMenu("Dashboard", "首页", "/dashboard"),
                createMenu("Warehouse", "仓储管理", "/warehouse")
            );
            case "QUALITY" -> List.of(
                createMenu("Dashboard", "首页", "/dashboard"),
                createMenu("Quality", "质量管理", "/quality")
            );
            case "FINANCE" -> List.of(
                createMenu("Dashboard", "首页", "/dashboard"),
                createMenu("Finance", "财务管理", "/finance")
            );
            default -> List.of();
        };
        
        log.info("✅ 返回菜单数量: {}", menus.size());
        return Result.success(menus);
    }
    
    private String getRoleCode(Integer role) {
        if (role == null) {
            return "USER";
        }
        return switch (role) {
            case 1 -> "ADMIN";
            case 2 -> "PHARMACIST";
            case 3 -> "PURCHASER";
            case 4 -> "SALES";
            case 5 -> "WAREHOUSE";
            case 6 -> "QUALITY";
            case 7 -> "FINANCE";
            default -> "USER";
        };
    }
    
    private Map<String, Object> createMenu(String key, String title, String path) {
        Map<String, Object> menu = new HashMap<>();
        menu.put("key", key);
        menu.put("title", title);
        menu.put("path", path);
        return menu;
    }
}
