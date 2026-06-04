package xyz.haimianxiaozi.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<TagVO> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private Boolean essence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
