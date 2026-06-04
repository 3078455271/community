package xyz.haimianxiaozi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.FavoriteFolderDTO;
import xyz.haimianxiaozi.dto.PostFavoriteDTO;
import xyz.haimianxiaozi.entity.FavoriteFolder;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.service.FavoriteFolderService;
import xyz.haimianxiaozi.service.PostFavoriteService;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.PostVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteFolderService favoriteFolderService;
    private final PostFavoriteService postFavoriteService;
    private final PostServiceExt postServiceExt;
    private final UserContext userContext;

    @GetMapping("/folders")
    public R<List<FavoriteFolder>> listFolders() {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        favoriteFolderService.getOrCreateDefaultFolder(userId);
        List<FavoriteFolder> folders = favoriteFolderService.list(new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getUserId, userId)
                .orderByAsc(FavoriteFolder::getSort)
                .orderByDesc(FavoriteFolder::getCreatedAt));
        return R.ok(folders);
    }

    @PostMapping("/folders")
    public R<Long> createFolder(@Valid @RequestBody FavoriteFolderDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName(dto.getName());
        folder.setSort(100);
        folder.setDeleted(0);
        favoriteFolderService.save(folder);
        return R.ok(folder.getId());
    }

    @GetMapping("/posts")
    public R<Page<PostVO>> listFavoritePosts(
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(postFavoriteService.getFavoritePosts(userId, folderId, page, size));
    }

    @GetMapping("/posts/{postId}/status")
    public R<Map<String, Object>> favoriteStatus(@PathVariable Long postId) {
        Long userId = userContext.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", userId != null && postFavoriteService.isFavorited(userId, postId));
        return R.ok(result);
    }

    @PostMapping("/posts/{postId}")
    public R<String> favorite(@PathVariable Long postId, @RequestBody(required = false) PostFavoriteDTO dto) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        Post post = postServiceExt.getById(postId);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        Long folderId = dto == null ? null : dto.getFolderId();
        boolean success = postFavoriteService.favorite(userId, postId, folderId);
        if (!success) {
            return R.fail("已收藏或收藏夹不存在");
        }
        return R.ok("收藏成功");
    }

    @DeleteMapping("/posts/{postId}")
    public R<String> unfavorite(@PathVariable Long postId) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        boolean success = postFavoriteService.unfavorite(userId, postId);
        if (!success) {
            return R.fail("未收藏");
        }
        return R.ok("取消收藏成功");
    }
}
