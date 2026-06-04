package xyz.haimianxiaozi.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostDraftDTO {

    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    private String content;

    private Long categoryId;
}
