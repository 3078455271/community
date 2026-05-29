package xyz.haimianxiaozi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.PostDTO;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.enums.CommonEnums.LikeTargetType;
import xyz.haimianxiaozi.service.LikeService;
import xyz.haimianxiaozi.service.NotificationService;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.PostVO;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceExt postServiceExt;
    private final LikeService likeService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final UserContext userContext;

    @GetMapping
    public R<Page<PostVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {
        return R.ok(postServiceExt.getPostPage(page, size, categoryId));
    }

    @GetMapping("/search")
    public R<Page<PostVO>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String keyword) {
        return R.ok(postServiceExt.searchPosts(page, size, keyword));
    }

    @GetMapping("/{id}")
    public R<PostVO> detail(@PathVariable Long id) {
        PostVO vo = postServiceExt.getPostDetail(id);
        if (vo == null) {
            return R.fail("帖子不存在");
        }
        return R.ok(vo);
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody PostDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        postServiceExt.save(post);

        return R.ok(post.getId());
    }

    @PutMapping("/{id}")
    public R<String> update(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postServiceExt.getById(id);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return R.fail(403, "无权修改");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());
        postServiceExt.updateById(post);

        return R.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postServiceExt.getById(id);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return R.fail(403, "无权删除");
        }

        postServiceExt.removeById(id);
        return R.ok("删除成功");
    }

    @PostMapping("/{id}/like")
    public R<String> like(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postServiceExt.getById(id);
        if (post == null) {
            return R.fail("帖子不存在");
        }

        boolean success = likeService.like(userId, id, LikeTargetType.POST);
        if (success) {
            post.setLikeCount(post.getLikeCount() + 1);
            postServiceExt.updateById(post);

            // 发送通知（不通知自己）
            if (!post.getUserId().equals(userId)) {
                User user = userService.getById(userId);
                String nickname = user != null ? user.getNickname() : "用户";
                notificationService.sendNotification(
                        post.getUserId(),
                        "LIKE",
                        nickname + " 赞了你的帖子「" + post.getTitle() + "」",
                        id
                );
            }

            return R.ok("点赞成功");
        }
        return R.fail("已点赞");
    }

    @DeleteMapping("/{id}/like")
    public R<String> unlike(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postServiceExt.getById(id);
        if (post == null) {
            return R.fail("帖子不存在");
        }

        boolean success = likeService.unlike(userId, id, LikeTargetType.POST);
        if (success) {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postServiceExt.updateById(post);
            return R.ok("取消点赞成功");
        }
        return R.fail("未点赞");
    }
}