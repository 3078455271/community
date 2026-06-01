package xyz.haimianxiaozi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公共枚举类
 * 用于定义项目中通用的状态码、类型等常量集合
 *
 * @author haimianxiaozi
 */
public enum CommonEnums {

    ;

    /**
     * 点赞目标类型
     */
    @Getter
    @AllArgsConstructor
    public enum LikeTargetType {

        POST("POST", "帖子"),
        COMMENT("COMMENT", "评论");

        private final String code;
        private final String desc;
    }

    /**
     * 用户状态
     */
    @Getter
    @AllArgsConstructor
    public enum UserStatus {

        DISABLED(0, "禁用"),
        ENABLED(1, "正常");

        private final int code;
        private final String desc;
    }
}