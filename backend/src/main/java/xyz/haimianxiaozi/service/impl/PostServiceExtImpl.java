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
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.vo.PostVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceExtImpl extends ServiceImpl<PostMapper, Post> implements PostServiceExt {

    private final UserService userService;
    private final CategoryService categoryService;

    @Override
    public Page<PostVO> getPostPage(int page, int size, Long categoryId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
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
        if (post == null) {
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
                .map(Post::getCategoryId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryService.listByIds(categoryIds).stream()
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