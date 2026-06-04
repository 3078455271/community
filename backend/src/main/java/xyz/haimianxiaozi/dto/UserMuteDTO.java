package xyz.haimianxiaozi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserMuteDTO {

    @NotNull(message = "禁言天数不能为空")
    private Integer days;
}
