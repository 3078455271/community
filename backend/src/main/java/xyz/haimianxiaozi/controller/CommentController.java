package xyz.haimianxiaozi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.CommentDTO;
import xyz.haimianxiaozi.entity.Comment;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.service.CommentService;
import xyz.haimianxiaozi.service.PostService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.CommentVO;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
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
}