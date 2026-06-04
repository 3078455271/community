package xyz.haimianxiaozi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SensitiveWordDTO {

    @NotBlank(message = "敏感词不能为空")
    private String word;
}
