package xyz.haimianxiaozi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.CommentDTO;
import xyz.haimianxiaozi.entity.Comment;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.enums.CommonEnums.LikeTargetType;
import xyz.haimianxiaozi.service.CommentService;
import xyz.haimianxiaozi.service.LikeService;
import xyz.haimianxiaozi.service.NotificationService;
import xyz.haimianxiaozi.service.PostService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.CommentVO;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final LikeService likeService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final UserContext userContext;

    @GetMapping
    public R<List<CommentVO>> list(@PathVariable Long postId) {
        return R.ok(commentService.getCommentsByPostId(postId));
    }

    @PostMapping
    public R<Long> create(@PathVariable Long postId, @Valid @RequestBody CommentDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postService.getById(postId);
        if (post == null) {
            return R.fail("帖子不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setLikeCount(0);
        commentService.save(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postService.updateById(post);

        // 发送通知给帖子作者（不通知自己）
        if (!post.getUserId().equals(userId)) {
            User user = userService.getById(userId);
            String nickname = user != null ? user.getNickname() : "用户";
            notificationService.sendNotification(
                    post.getUserId(),
                    "COMMENT",
                    nickname + " 评论了你的帖子「" + post.getTitle() + "」",
                    postId
            );
        }

        // 如果是回复评论，通知被回复的用户
        if (dto.getParentId() != null) {
            Comment parentComment = commentService.getById(dto.getParentId());
            if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                User user = userService.getById(userId);
                String nickname = user != null ? user.getNickname() : "用户";
                notificationService.sendNotification(
                        parentComment.getUserId(),
                        "COMMENT",
                        nickname + " 回复了你的评论",
                        postId
                );
            }
        }

        return R.ok(comment.getId());
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long postId, @PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return R.fail("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            return R.fail(403, "无权删除");
        }

        commentService.removeById(id);

        // 更新帖子评论数
        Post post = postService.getById(postId);
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postService.updateById(post);
        }

        return R.ok("删除成功");
    }

    @PostMapping("/{id}/like")
    public R<String> like(@PathVariable Long postId, @PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return R.fail("评论不存在");
        }

        boolean success = likeService.like(userId, id, LikeTargetType.COMMENT);
        if (success) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentService.updateById(comment);

            // 发送通知（不通知自己）
            if (!comment.getUserId().equals(userId)) {
                User user = userService.getById(userId);
                String nickname = user != null ? user.getNickname() : "用户";
                notificationService.sendNotification(
                        comment.getUserId(),
                        "LIKE",
                        nickname + " 赞了你的评论",
                        postId
                );
            }

            return R.ok("点赞成功");
        }
        return R.fail("已点赞");
    }

    @DeleteMapping("/{id}/like")
    public R<String> unlike(@PathVariable Long postId, @PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return R.fail("评论不存在");
        }

        boolean success = likeService.unlike(userId, id, LikeTargetType.COMMENT);
        if (success) {
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentService.updateById(comment);
            return R.ok("取消点赞成功");
        }
        return R.fail("未点赞");
    }
}