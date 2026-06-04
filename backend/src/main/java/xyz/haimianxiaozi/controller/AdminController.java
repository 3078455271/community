package xyz.haimianxiaozi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.ReportHandleDTO;
import xyz.haimianxiaozi.dto.SensitiveWordDTO;
import xyz.haimianxiaozi.dto.UserMuteDTO;
import xyz.haimianxiaozi.dto.UserRoleDTO;
import xyz.haimianxiaozi.entity.AuditLog;
import xyz.haimianxiaozi.entity.Category;
import xyz.haimianxiaozi.entity.Report;
import xyz.haimianxiaozi.entity.SensitiveWord;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.AuditLogMapper;
import xyz.haimianxiaozi.mapper.ReportMapper;
import xyz.haimianxiaozi.mapper.SensitiveWordMapper;
import xyz.haimianxiaozi.service.AdminService;
import xyz.haimianxiaozi.service.CategoryService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.vo.ReportVO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Set<String> ROLES = Set.of("USER", "MODERATOR", "ADMIN");

    private final AdminService adminService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ReportMapper reportMapper;
    private final SensitiveWordMapper sensitiveWordMapper;
    private final AuditLogMapper auditLogMapper;

    @GetMapping("/users")
    public R<Page<User>> users(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String keyword) {
        try {
            adminService.requireAdmin();
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            if (keyword != null && !keyword.isBlank()) {
                wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
            }
            wrapper.orderByDesc(User::getCreatedAt);
            Page<User> result = userService.page(new Page<>(page, size), wrapper);
            result.getRecords().forEach(user -> user.setPassword(null));
            return R.ok(result);
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @PutMapping("/users/{id}/role")
    public R<String> updateRole(@PathVariable Long id, @Valid @RequestBody UserRoleDTO dto) {
        try {
            User operator = adminService.requireAdmin();
            if (!ROLES.contains(dto.getRole())) {
                return R.fail("角色不合法");
            }
            User user = userService.getById(id);
            if (user == null) {
                return R.fail("用户不存在");
            }
            user.setRole(dto.getRole());
            userService.updateById(user);
            adminService.audit(operator, "UPDATE_ROLE", "USER", id, "role=" + dto.getRole());
            return R.ok("更新成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @PutMapping("/users/{id}/ban")
    public R<String> banUser(@PathVariable Long id) {
        return updateUserStatus(id, 0, "BAN_USER");
    }

    @DeleteMapping("/users/{id}/ban")
    public R<String> unbanUser(@PathVariable Long id) {
        return updateUserStatus(id, 1, "UNBAN_USER");
    }

    @PutMapping("/users/{id}/mute")
    public R<String> muteUser(@PathVariable Long id, @Valid @RequestBody UserMuteDTO dto) {
        try {
            User operator = adminService.requireAdmin();
            User user = userService.getById(id);
            if (user == null) {
                return R.fail("用户不存在");
            }
            int days = Math.min(Math.max(dto.getDays(), 1), 365);
            user.setMutedUntil(LocalDateTime.now().plusDays(days));
            userService.updateById(user);
            adminService.audit(operator, "MUTE_USER", "USER", id, "days=" + days);
            return R.ok("禁言成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}/mute")
    public R<String> unmuteUser(@PathVariable Long id) {
        try {
            User operator = adminService.requireAdmin();
            User user = userService.getById(id);
            if (user == null) {
                return R.fail("用户不存在");
            }
            userService.lambdaUpdate()
                    .eq(User::getId, id)
                    .set(User::getMutedUntil, null)
                    .update();
            adminService.audit(operator, "UNMUTE_USER", "USER", id, null);
            return R.ok("解除禁言成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @GetMapping("/reports")
    public R<Page<ReportVO>> reports(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) Integer status) {
        try {
            adminService.requireModerator();
            LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(Report::getStatus, status);
            }
            wrapper.orderByDesc(Report::getCreatedAt);
            Page<Report> reportPage = reportMapper.selectPage(new Page<>(page, size), wrapper);
            Map<Long, User> reporterMap = userService.listByIds(reportPage.getRecords().stream()
                            .map(Report::getReporterId)
                            .collect(Collectors.toSet()))
                    .stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            Page<ReportVO> voPage = new Page<>(page, size, reportPage.getTotal());
            voPage.setRecords(reportPage.getRecords().stream().map(report -> {
                ReportVO vo = new ReportVO();
                BeanUtils.copyProperties(report, vo);
                User reporter = reporterMap.get(report.getReporterId());
                if (reporter != null) {
                    vo.setReporterName(reporter.getNickname() != null ? reporter.getNickname() : reporter.getUsername());
                }
                return vo;
            }).toList());
            return R.ok(voPage);
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @PutMapping("/reports/{id}")
    public R<String> handleReport(@PathVariable Long id, @Valid @RequestBody ReportHandleDTO dto) {
        try {
            User operator = adminService.requireModerator();
            if (dto.getStatus() != 1 && dto.getStatus() != 2) {
                return R.fail("审核状态不合法");
            }
            Report report = reportMapper.selectById(id);
            if (report == null) {
                return R.fail("举报不存在");
            }
            report.setStatus(dto.getStatus());
            report.setHandledBy(operator.getId());
            report.setHandleRemark(dto.getRemark());
            report.setHandledAt(LocalDateTime.now());
            reportMapper.updateById(report);
            adminService.audit(operator, "HANDLE_REPORT", report.getTargetType(), report.getTargetId(),
                    "reportId=" + id + ", status=" + dto.getStatus());
            return R.ok("处理成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @GetMapping("/sensitive-words")
    public R<Page<SensitiveWord>> sensitiveWords(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        try {
            adminService.requireModerator();
            return R.ok(sensitiveWordMapper.selectPage(new Page<>(page, size),
                    new LambdaQueryWrapper<SensitiveWord>().orderByDesc(SensitiveWord::getCreatedAt)));
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @PostMapping("/sensitive-words")
    public R<Long> createSensitiveWord(@Valid @RequestBody SensitiveWordDTO dto) {
        try {
            User operator = adminService.requireModerator();
            SensitiveWord word = new SensitiveWord();
            word.setWord(dto.getWord().trim());
            word.setEnabled(1);
            word.setDeleted(0);
            sensitiveWordMapper.insert(word);
            adminService.audit(operator, "CREATE_SENSITIVE_WORD", "SENSITIVE_WORD", word.getId(), word.getWord());
            return R.ok(word.getId());
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @DeleteMapping("/sensitive-words/{id}")
    public R<String> deleteSensitiveWord(@PathVariable Long id) {
        try {
            User operator = adminService.requireModerator();
            sensitiveWordMapper.deleteById(id);
            adminService.audit(operator, "DELETE_SENSITIVE_WORD", "SENSITIVE_WORD", id, null);
            return R.ok("删除成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @GetMapping("/categories")
    public R<Page<Category>> categories(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        try {
            adminService.requireModerator();
            return R.ok(categoryService.page(new Page<>(page, size),
                    new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort)));
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @PostMapping("/categories")
    public R<Long> createCategory(@RequestBody Category category) {
        try {
            User operator = adminService.requireModerator();
            categoryService.save(category);
            adminService.audit(operator, "CREATE_CATEGORY", "CATEGORY", category.getId(), category.getName());
            return R.ok(category.getId());
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @PutMapping("/categories/{id}")
    public R<String> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
            User operator = adminService.requireModerator();
            category.setId(id);
            categoryService.updateById(category);
            adminService.audit(operator, "UPDATE_CATEGORY", "CATEGORY", id, category.getName());
            return R.ok("更新成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @DeleteMapping("/categories/{id}")
    public R<String> deleteCategory(@PathVariable Long id) {
        try {
            User operator = adminService.requireModerator();
            categoryService.removeById(id);
            adminService.audit(operator, "DELETE_CATEGORY", "CATEGORY", id, null);
            return R.ok("删除成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    @GetMapping("/audit-logs")
    public R<Page<AuditLog>> auditLogs(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        try {
            adminService.requireAdmin();
            return R.ok(auditLogMapper.selectPage(new Page<>(page, size),
                    new LambdaQueryWrapper<AuditLog>().orderByDesc(AuditLog::getCreatedAt)));
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }

    private R<String> updateUserStatus(Long id, Integer status, String action) {
        try {
            User operator = adminService.requireAdmin();
            User user = userService.getById(id);
            if (user == null) {
                return R.fail("用户不存在");
            }
            user.setStatus(status);
            userService.updateById(user);
            adminService.audit(operator, action, "USER", id, "status=" + status);
            return R.ok("操作成功");
        } catch (IllegalStateException e) {
            return R.fail(403, e.getMessage());
        }
    }
}
