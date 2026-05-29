package xyz.haimianxiaozi.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息 DTO
 */
@Data
public class UpdateUserDTO {

    @Size(min = 2, max = 20, message = "昵称长度 2-20 个字符")
    private String nickname;

    private String avatar;

    private String email;
}