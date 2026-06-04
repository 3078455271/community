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
import xyz.haimianxiaozi.service.PointService;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.service.ContentModerationService;
import xyz.haimianxiaozi.service.TagService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.PostVO;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceExt postServiceExt;
    private final LikeService likeService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final MentionService mentionService;
    private final TagService tagService;
    private final PointService pointService;
    private final ContentModerationService contentModerationService;
    private final UserContext userContext;

    @GetMapping
    public R<Page<PostVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        if (tagId != null) {
            return R.ok(postServiceExt.getPostPageByTag(page, size, tagId));
        }
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

    @GetMapping("/history")
    public R<Page<PostVO>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(postServiceExt.getViewHistory(page, size, userId));
    }

    @PostMapping("/drafts")
    public R<Long> createDraft(@RequestBody PostDraftDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        User currentUser = userService.getById(userId);
        R<String> moderationResult = checkWritable(currentUser, dto.getContent());
        if (moderationResult != null) {
            return R.fail(moderationResult.getCode(), moderationResult.getMessage());
        }

        Post post = new Post();
        post.setUserId(userId);
        fillDraft(post, dto);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(0);
        post.setEssence(false);
        postServiceExt.save(post);
        tagService.syncPostTags(post.getId(), dto.getTags(), false, false);
        return R.ok(post.getId());
    }

    @PutMapping("/drafts/{id}")
    public R<String> updateDraft(@PathVariable Long id, @RequestBody PostDraftDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        User currentUser = userService.getById(userId);
        R<String> moderationResult = checkWritable(currentUser, dto.getContent());
        if (moderationResult != null) {
            return R.fail(moderationResult.getCode(), moderationResult.getMessage());
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
        tagService.syncPostTags(post.getId(), dto.getTags(), false, false);
        return R.ok("保存成功");
    }

    @PostMapping("/drafts/{id}/publish")
    public R<Long> publishDraft(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        User currentUser = userService.getById(userId);
        R<String> moderationResult = checkWritable(currentUser, dto.getContent());
        if (moderationResult != null) {
            return R.fail(moderationResult.getCode(), moderationResult.getMessage());
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
        post.setEssence(false);
        postServiceExt.updateById(post);
        tagService.syncPostTags(post.getId(), dto.getTags(), true, false);

        User user = userService.getById(userId);
        mentionService.notifyMentions(dto.getContent(), userId, getDisplayName(user), post.getId());
        pointService.rewardPost(userId);
        return R.ok(post.getId());
    }

    @GetMapping("/{id}")
    public R<PostVO> detail(@PathVariable Long id) {
        PostVO vo = postServiceExt.getPostDetail(id);
        if (vo == null) {
            return R.fail("帖子不存在");
        }
        Long userId = userContext.getCurrentUserId();
        if (userId != null) {
            postServiceExt.recordViewHistory(userId, id);
        }
        return R.ok(vo);
    }

    @GetMapping("/{id}/related")
    public R<List<PostVO>> related(@PathVariable Long id,
                                   @RequestParam(defaultValue = "6") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 12);
        return R.ok(postServiceExt.getRelatedPosts(id, safeLimit));
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody PostDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        User currentUser = userService.getById(userId);
        R<String> moderationResult = checkWritable(currentUser, dto.getContent());
        if (moderationResult != null) {
            return R.fail(moderationResult.getCode(), moderationResult.getMessage());
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
        post.setEssence(false);
        postServiceExt.save(post);
        tagService.syncPostTags(post.getId(), dto.getTags(), true, false);

        User user = userService.getById(userId);
        String displayName = getDisplayName(user);
        mentionService.notifyMentions(dto.getContent(), userId, displayName, post.getId());
        pointService.rewardPost(userId);

        return R.ok(post.getId());
    }

    @PutMapping("/{id}")
    public R<String> update(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        User currentUser = userService.getById(userId);
        R<String> moderationResult = checkWritable(currentUser, dto.getContent());
        if (moderationResult != null) {
            return R.fail(moderationResult.getCode(), moderationResult.getMessage());
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
        tagService.syncPostTags(post.getId(), dto.getTags(), true, true);

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

        tagService.syncPostTags(id, List.of(), false, isPublished(post));
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

    @PutMapping("/{id}/top")
    public R<String> top(@PathVariable Long id) {
        return updatePostMark(id, true, null);
    }

    @DeleteMapping("/{id}/top")
    public R<String> cancelTop(@PathVariable Long id) {
        return updatePostMark(id, false, null);
    }

    @PutMapping("/{id}/essence")
    public R<String> essence(@PathVariable Long id) {
        return updatePostMark(id, null, true);
    }

    @DeleteMapping("/{id}/essence")
    public R<String> cancelEssence(@PathVariable Long id) {
        return updatePostMark(id, null, false);
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

    private boolean isPublished(Post post) {
        return Integer.valueOf(1).equals(post.getStatus()) || Integer.valueOf(2).equals(post.getStatus());
    }

    private R<String> checkWritable(User user, String content) {
        if (contentModerationService.isMuted(user)) {
            return R.fail(403, contentModerationService.muteMessage(user));
        }
        String sensitiveWord = contentModerationService.findSensitiveWord(content);
        if (sensitiveWord != null) {
            return R.fail(400, "内容包含敏感词：" + sensitiveWord);
        }
        return null;
    }

    private R<String> updatePostMark(Long id, Boolean top, Boolean essence) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        Post post = postServiceExt.getById(id);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return R.fail(403, "无权操作");
        }
        if (top != null) {
            post.setStatus(top ? 2 : 1);
        }
        if (essence != null) {
            post.setEssence(essence);
        }
        postServiceExt.updateById(post);
        return R.ok("操作成功");
    }
}
