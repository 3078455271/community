package xyz.haimianxiaozi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.PostDraftDTO;
import xyz.haimianxiaozi.dto.PostDTO;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.enums.CommonEnums.LikeTargetType;
import xyz.haimianxiaozi.service.LikeService;
import xyz.haimianxiaozi.service.MentionService;
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
    private final MentionService mentionService;
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

    @GetMapping("/following")
    public R<Page<PostVO>> following(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(postServiceExt.getFollowingPostPage(page, size, userId));
    }

    @GetMapping("/drafts")
    public R<Page<PostVO>> drafts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(postServiceExt.getDraftPage(page, size, userId));
    }

    @GetMapping("/drafts/{id}")
    public R<PostVO> draftDetail(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        PostVO vo = postServiceExt.getDraftDetail(id, userId);
        if (vo == null) {
            return R.fail("草稿不存在");
        }
        return R.ok(vo);
    }

    @PostMapping("/drafts")
    public R<Long> createDraft(@RequestBody PostDraftDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = new Post();
        post.setUserId(userId);
        fillDraft(post, dto);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(0);
        postServiceExt.save(post);
        return R.ok(post.getId());
    }

    @PutMapping("/drafts/{id}")
    public R<String> updateDraft(@PathVariable Long id, @RequestBody PostDraftDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postServiceExt.getById(id);
        if (post == null || !Integer.valueOf(0).equals(post.getStatus())) {
            return R.fail("草稿不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return R.fail(403, "无权修改");
        }

        fillDraft(post, dto);
        postServiceExt.updateById(post);
        return R.ok("保存成功");
    }

    @PostMapping("/drafts/{id}/publish")
    public R<Long> publishDraft(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postServiceExt.getById(id);
        if (post == null || !Integer.valueOf(0).equals(post.getStatus())) {
            return R.fail("草稿不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return R.fail(403, "无权发布");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());
        post.setStatus(1);
        postServiceExt.updateById(post);

        User user = userService.getById(userId);
        mentionService.notifyMentions(dto.getContent(), userId, getDisplayName(user), post.getId());
        return R.ok(post.getId());
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

        User user = userService.getById(userId);
        String displayName = getDisplayName(user);
        mentionService.notifyMentions(dto.getContent(), userId, displayName, post.getId());

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
                String nickname = getDisplayName(user);
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

    private String getDisplayName(User user) {
        if (user == null) {
            return "用户";
        }
        return user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername();
    }

    private void fillDraft(Post post, PostDraftDTO dto) {
        String title = dto.getTitle();
        post.setTitle(title == null || title.isBlank() ? "未命名草稿" : title);
        post.setContent(dto.getContent() == null ? "" : dto.getContent());
        post.setCategoryId(dto.getCategoryId());
    }
}
