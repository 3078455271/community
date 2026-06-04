package xyz.haimianxiaozi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.ReportDTO;
import xyz.haimianxiaozi.entity.Report;
import xyz.haimianxiaozi.mapper.ReportMapper;
import xyz.haimianxiaozi.util.UserContext;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportMapper reportMapper;
    private final UserContext userContext;

    @PostMapping
    public R<Long> create(@Valid @RequestBody ReportDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setStatus(0);
        reportMapper.insert(report);
        return R.ok(report.getId());
    }
}
