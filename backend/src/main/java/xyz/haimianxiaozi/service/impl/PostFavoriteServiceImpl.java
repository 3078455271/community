package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.Category;
import xyz.haimianxiaozi.entity.FavoriteFolder;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.entity.PostFavorite;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.PostFavoriteMapper;
import xyz.haimianxiaozi.service.CategoryService;
import xyz.haimianxiaozi.service.FavoriteFolderService;
import xyz.haimianxiaozi.service.PostFavoriteService;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.vo.PostVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostFavoriteServiceImpl extends ServiceImpl<PostFavoriteMapper, PostFavorite>
        implements PostFavoriteService {

    private final FavoriteFolderService favoriteFolderService;
    private final PostServiceExt postServiceExt;
    private final UserService userService;
    private final CategoryService categoryService;

    @Override
    public boolean favorite(Long userId, Long postId, Long folderId) {
        FavoriteFolder folder = folderId == null
                ? favoriteFolderService.getOrCreateDefaultFolder(userId)
                : favoriteFolderService.getById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            return false;
        }

        PostFavorite existing = baseMapper.selectAnyByUserAndPost(userId, postId);
        if (existing != null) {
            if (Integer.valueOf(0).equals(existing.getDeleted())) {
                return false;
            }
            return baseMapper.restoreById(existing.getId(), folder.getId()) > 0;
        }

        PostFavorite favorite = new PostFavorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);
        favorite.setFolderId(folder.getId());
        favorite.setDeleted(0);
        return save(favorite);
    }

    @Override
    public boolean unfavorite(Long userId, Long postId) {
        PostFavorite favorite = getOne(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId), false);
        if (favorite == null) {
            return false;
        }
        return removeById(favorite.getId());
    }

    @Override
    public boolean isFavorited(Long userId, Long postId) {
        return count(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId)) > 0;
    }

    @Override
    public Page<PostVO> getFavoritePosts(Long userId, Long folderId, int page, int size) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .orderByDesc(PostFavorite::getCreatedAt);
        if (folderId != null) {
            wrapper.eq(PostFavorite::getFolderId, folderId);
        }

        Page<PostFavorite> favoritePage = page(new Page<>(page, size), wrapper);
        if (favoritePage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> postIds = favoritePage.getRecords().stream()
                .map(PostFavorite::getPostId)
                .toList();
        List<Post> posts = postServiceExt.listByIds(postIds);
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));

        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        List<Long> categoryIds = posts.stream().map(Post::getCategoryId).distinct().toList();
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<PostVO> voList = favoritePage.getRecords().stream()
                .map(favorite -> postMap.get(favorite.getPostId()))
                .filter(post -> post != null)
                .map(post -> toPostVO(post, userMap, categoryMap))
                .toList();

        Page<PostVO> voPage = new Page<>(page, size, favoritePage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private PostVO toPostVO(Post post, Map<Long, User> userMap, Map<Long, Category> categoryMap) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        User user = userMap.get(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        Category category = categoryMap.get(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }
}
