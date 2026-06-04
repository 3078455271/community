package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.Category;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.PostMapper;
import xyz.haimianxiaozi.service.CategoryService;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.service.UserFollowService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.vo.PostVO;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceExtImpl extends ServiceImpl<PostMapper, Post> implements PostServiceExt {

    private final UserService userService;
    private final CategoryService categoryService;
    private final UserFollowService userFollowService;

    @Override
    public Page<PostVO> getPostPage(int page, int size, Long categoryId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        wrapper.eq(Post::getStatus, 1);
        wrapper.orderByDesc(Post::getCreatedAt);

        Page<Post> postPage = page(new Page<>(page, size), wrapper);

        if (postPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        return convertToVOPage(postPage, page, size);
    }

    @Override
    public PostVO getPostDetail(Long id) {
        Post post = getById(id);
        if (post == null || !Integer.valueOf(1).equals(post.getStatus())) {
            return null;
        }

        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);

        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);

        User user = userService.getById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        Category category = categoryService.getById(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        return vo;
    }

    @Override
    public Page<PostVO> getFollowingPostPage(int page, int size, Long userId) {
        List<Long> followingUserIds = userFollowService.listFollowingUserIds(userId);
        if (followingUserIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Post::getUserId, followingUserIds)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt);

        Page<Post> postPage = page(new Page<>(page, size), wrapper);
        if (postPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        return convertToVOPage(postPage, page, size);
    }

    @Override
    public Page<PostVO> getDraftPage(int page, int size, Long userId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
                .eq(Post::getStatus, 0)
                .orderByDesc(Post::getUpdatedAt);

        Page<Post> postPage = page(new Page<>(page, size), wrapper);
        if (postPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        return convertToVOPage(postPage, page, size);
    }

    @Override
    public PostVO getDraftDetail(Long id, Long userId) {
        Post post = getOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, id)
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, 0), false);
        if (post == null) {
            return null;
        }

        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        if (post.getCategoryId() != null) {
            Category category = categoryService.getById(post.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }

    @Override
    public Page<PostVO> searchPosts(int page, int size, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .like(Post::getTitle, keyword)
                .or()
                .like(Post::getContent, keyword)
        );
        wrapper.eq(Post::getStatus, 1);
        wrapper.orderByDesc(Post::getCreatedAt);

        Page<Post> postPage = page(new Page<>(page, size), wrapper);

        if (postPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        return convertToVOPage(postPage, page, size);
    }

    /**
     * 将帖子分页转换为 VO 分页
     */
    private Page<PostVO> convertToVOPage(Page<Post> postPage, int page, int size) {
        // 批量查询用户和分类
        List<Long> userIds = postPage.getRecords().stream()
                .map(Post::getUserId).distinct().collect(Collectors.toList());
        List<Long> categoryIds = postPage.getRecords().stream()
                .map(Post::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryIds.isEmpty()
                ? Map.of()
                : categoryService.listByIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));

        Page<PostVO> voPage = new Page<>(page, size, postPage.getTotal());
        List<PostVO> voList = postPage.getRecords().stream().map(post -> {
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
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}
