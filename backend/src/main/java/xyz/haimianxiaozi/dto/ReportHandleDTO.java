package xyz.haimianxiaozi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportHandleDTO {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    private String remark;
}
