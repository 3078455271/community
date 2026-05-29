package xyz.haimianxiaozi.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long categoryId;
    private String categoryName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
}