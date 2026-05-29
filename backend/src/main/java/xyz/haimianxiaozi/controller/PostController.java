package xyz.haimianxiaozi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.PostDTO;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.PostVO;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceExt postServiceExt;
    private final UserContext userContext;

    @GetMapping
    public R<Page<PostVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {
        return R.ok(postServiceExt.getPostPage(page, size, categoryId));
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
}