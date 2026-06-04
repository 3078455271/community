package xyz.haimianxiaozi.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPointVO {

    private Long userId;
    private Integer points;
    private Integer level;
    private Integer signInDays;
    private Boolean signedInToday;
    private LocalDateTime lastSignInAt;
}
