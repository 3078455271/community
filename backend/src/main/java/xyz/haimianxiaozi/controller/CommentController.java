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
import xyz.haimianxiaozi.service.ContentModerationService;
import xyz.haimianxiaozi.service.LikeService;
import xyz.haimianxiaozi.service.MentionService;
import xyz.haimianxiaozi.service.NotificationService;
import xyz.haimianxiaozi.service.PointService;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.CommentVO;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostServiceExt postServiceExt;
    private final LikeService likeService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final MentionService mentionService;
    private final PointService pointService;
    private final ContentModerationService contentModerationService;
    private final UserContext userContext;

    @GetMapping
    public R<List<CommentVO>> list(@PathVariable Long postId) {
        Post post = postServiceExt.getById(postId);
        if (!postServiceExt.canViewPost(post, userContext.getCurrentUserId())) {
            return R.fail("帖子不存在");
        }
        return R.ok(commentService.getCommentsByPostId(postId));
    }

    @PostMapping
    public R<Long> create(@PathVariable Long postId, @Valid @RequestBody CommentDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        User currentUser = userService.getById(userId);
        if (contentModerationService.isMuted(currentUser)) {
            return R.fail(403, contentModerationService.muteMessage(currentUser));
        }
        String sensitiveWord = contentModerationService.findSensitiveWord(dto.getContent());
        if (sensitiveWord != null) {
            return R.fail("评论包含敏感词：" + sensitiveWord);
        }

        Post post = postServiceExt.getById(postId);
        if (!postServiceExt.canViewPost(post, userId)) {
            return R.fail("帖子不存在");
        }
        if (Boolean.FALSE.equals(post.getCommentEnabled())) {
            return R.fail("该帖子已关闭评论");
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
        postServiceExt.updateById(post);

        String nickname = getDisplayName(currentUser);
        mentionService.notifyMentions(dto.getContent(), userId, nickname, postId);

        // 发送通知给帖子作者（不通知自己）
        if (!post.getUserId().equals(userId)) {
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
                notificationService.sendNotification(
                        parentComment.getUserId(),
                        "COMMENT",
                        nickname + " 回复了你的评论",
                        postId
                );
            }
        }

        pointService.rewardComment(userId);
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
        Post post = postServiceExt.getById(postId);
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postServiceExt.updateById(post);
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
                String nickname = getDisplayName(user);
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

    private String getDisplayName(User user) {
        if (user == null) {
            return "用户";
        }
        return user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername();
    }
}
