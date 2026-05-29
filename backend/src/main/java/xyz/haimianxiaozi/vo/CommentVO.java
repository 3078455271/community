package xyz.haimianxiaozi.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long parentId;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private List<CommentVO> children;
}