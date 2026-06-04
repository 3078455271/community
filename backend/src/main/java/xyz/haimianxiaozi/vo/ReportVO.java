package xyz.haimianxiaozi.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private String targetType;
    private Long targetId;
    private String reason;
    private Integer status;
    private Long handledBy;
    private String handleRemark;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
