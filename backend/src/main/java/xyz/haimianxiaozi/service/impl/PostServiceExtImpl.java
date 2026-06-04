package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.Category;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.entity.PostTag;
import xyz.haimianxiaozi.entity.PostViewHistory;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.PostMapper;
import xyz.haimianxiaozi.mapper.PostTagMapper;
import xyz.haimianxiaozi.mapper.PostViewHistoryMapper;
import xyz.haimianxiaozi.service.CategoryService;
import xyz.haimianxiaozi.service.PostServiceExt;
import xyz.haimianxiaozi.service.TagService;
import xyz.haimianxiaozi.service.UserFollowService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.vo.PostVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceExtImpl extends ServiceImpl<PostMapper, Post> implements PostServiceExt {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String VISIBILITY_FOLLOWERS = "FOLLOWERS";

    private final UserService userService;
    private final CategoryService categoryService;
    private final UserFollowService userFollowService;
    private final TagService tagService;
    private final PostTagMapper postTagMapper;
    private final PostViewHistoryMapper postViewHistoryMapper;

    @Override
    public Page<PostVO> getPostPage(int page, int size, Long categoryId, Long currentUserId) {
        LambdaQueryWrapper<Post> wrapper = visiblePublishedWrapper(currentUserId);
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Post::getStatus).orderByDesc(Post::getCreatedAt);

        Page<Post> postPage = page(new Page<>(page, size), wrapper);
        if (postPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }
        return convertToVOPage(postPage, page, size);
    }

    @Override
    public Page<PostVO> getPostPageByTag(int page, int size, Long tagId, Long currentUserId) {
        List<Long> postIds = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .select(PostTag::getPostId)
                .eq(PostTag::getTagId, tagId))
                .stream()
                .map(PostTag::getPostId)
                .toList();
        if (postIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        Page<Post> postPage = page(new Page<>(page, size), visiblePublishedWrapper(currentUserId)
                .in(Post::getId, postIds)
                .orderByDesc(Post::getStatus)
                .orderByDesc(Post::getCreatedAt));
        if (postPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }
        return convertToVOPage(postPage, page, size);
    }

    @Override
    public PostVO getPostDetail(Long id, Long currentUserId) {
        Post post = getById(id);
        if (post == null || !isPublished(post) || !canViewPost(post, currentUserId)) {
            return null;
        }

        post.setViewCount(post.getViewCount() + 1);
        updateById(post);
        return convertToVO(post);
    }

    @Override
    public Page<PostVO> getFollowingPostPage(int page, int size, Long userId) {
        List<Long> followingUserIds = userFollowService.listFollowingUserIds(userId);
        if (followingUserIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        Page<Post> postPage = page(new Page<>(page, size), visiblePublishedWrapper(userId)
                .in(Post::getUserId, followingUserIds)
                .orderByDesc(Post::getStatus)
                .orderByDesc(Post::getCreatedAt));
        if (postPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }
        return convertToVOPage(postPage, page, size);
    }

    @Override
    public Page<PostVO> getDraftPage(int page, int size, Long userId) {
        Page<Post> postPage = page(new Page<>(page, size), new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, 0)
                .orderByDesc(Post::getUpdatedAt));
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
        return convertToVO(post);
    }

    @Override
    public Page<PostVO> searchPosts(int page, int size, String keyword, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        String cleanKeyword = keyword.trim();
        long offset = (long) Math.max(page - 1, 0) * size;
        List<Long> followingUserIds = visibleFollowingUserIds(currentUserId);
        List<Post> records = baseMapper.searchPublished(cleanKeyword, offset, size, currentUserId, followingUserIds);
        long total = baseMapper.countSearchPublished(cleanKeyword, currentUserId, followingUserIds);
        Page<Post> postPage = new Page<>(page, size, total);
        postPage.setRecords(records);
        if (records.isEmpty()) {
            return new Page<>(page, size, 0);
        }
        return convertToVOPage(postPage, page, size);
    }

    @Override
    public List<PostVO> getRelatedPosts(Long id, int limit, Long currentUserId) {
        Post current = getById(id);
        if (current == null || !isPublished(current) || !canViewPost(current, currentUserId)) {
            return List.of();
        }

        Set<Long> relatedIds = new LinkedHashSet<>();
        List<Long> tagIds = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .select(PostTag::getTagId)
                .eq(PostTag::getPostId, id))
                .stream()
                .map(PostTag::getTagId)
                .toList();
        if (!tagIds.isEmpty()) {
            relatedIds.addAll(postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                    .select(PostTag::getPostId)
                    .in(PostTag::getTagId, tagIds)
                    .ne(PostTag::getPostId, id))
                    .stream()
                    .map(PostTag::getPostId)
                    .toList());
        }

        List<Post> related = new ArrayList<>();
        if (!relatedIds.isEmpty()) {
            related.addAll(list(visiblePublishedWrapper(currentUserId)
                    .in(Post::getId, relatedIds)
                    .orderByDesc(Post::getStatus)
                    .orderByDesc(Post::getViewCount)
                    .last("LIMIT " + limit)));
        }

        if (related.size() < limit && current.getCategoryId() != null) {
            int remaining = limit - related.size();
            List<Long> existingIds = related.stream().map(Post::getId).collect(Collectors.toList());
            related.addAll(list(visiblePublishedWrapper(currentUserId)
                    .eq(Post::getCategoryId, current.getCategoryId())
                    .ne(Post::getId, id)
                    .notIn(!existingIds.isEmpty(), Post::getId, existingIds)
                    .orderByDesc(Post::getStatus)
                    .orderByDesc(Post::getCreatedAt)
                    .last("LIMIT " + remaining)));
        }

        if (related.isEmpty()) {
            return List.of();
        }
        Page<Post> postPage = new Page<>(1, related.size(), related.size());
        postPage.setRecords(related);
        return convertToVOPage(postPage, 1, related.size()).getRecords();
    }

    @Override
    public void recordViewHistory(Long userId, Long postId) {
        if (userId == null || postId == null) {
            return;
        }
        PostViewHistory history = postViewHistoryMapper.selectOne(new LambdaQueryWrapper<PostViewHistory>()
                .eq(PostViewHistory::getUserId, userId)
                .eq(PostViewHistory::getPostId, postId), false);
        if (history == null) {
            history = new PostViewHistory();
            history.setUserId(userId);
            history.setPostId(postId);
            history.setViewedAt(LocalDateTime.now());
            postViewHistoryMapper.insert(history);
            return;
        }
        history.setViewedAt(LocalDateTime.now());
        postViewHistoryMapper.updateById(history);
    }

    @Override
    public Page<PostVO> getViewHistory(int page, int size, Long userId) {
        Page<PostViewHistory> historyPage = postViewHistoryMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PostViewHistory>()
                        .eq(PostViewHistory::getUserId, userId)
                        .orderByDesc(PostViewHistory::getViewedAt));
        List<PostViewHistory> histories = historyPage.getRecords();
        if (histories.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> postIds = histories.stream().map(PostViewHistory::getPostId).toList();
        Map<Long, Post> postMap = listByIds(postIds).stream()
                .filter(this::isPublished)
                .filter(post -> canViewPost(post, userId))
                .collect(Collectors.toMap(Post::getId, p -> p));
        List<Post> orderedPosts = postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();
        if (orderedPosts.isEmpty()) {
            return new Page<>(page, size, historyPage.getTotal());
        }

        Page<Post> postPage = new Page<>(page, size, historyPage.getTotal());
        postPage.setRecords(orderedPosts);
        return convertToVOPage(postPage, page, size);
    }

    private Page<PostVO> convertToVOPage(Page<Post> postPage, int page, int size) {
        List<Long> userIds = postPage.getRecords().stream()
                .map(Post::getUserId)
                .distinct()
                .collect(Collectors.toList());
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
            vo.setTags(tagService.listPostTags(post.getId()));
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    private PostVO convertToVO(Post post) {
        Page<Post> page = new Page<>(1, 1, 1);
        page.setRecords(List.of(post));
        return convertToVOPage(page, 1, 1).getRecords().getFirst();
    }

    private LambdaQueryWrapper<Post> visiblePublishedWrapper(Long currentUserId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>().in(Post::getStatus, 1, 2);
        applyVisibilityFilter(wrapper, currentUserId);
        return wrapper;
    }

    private void applyVisibilityFilter(LambdaQueryWrapper<Post> wrapper, Long currentUserId) {
        List<Long> followingUserIds = visibleFollowingUserIds(currentUserId);
        wrapper.and(query -> {
            query.eq(Post::getVisibility, VISIBILITY_PUBLIC).or().isNull(Post::getVisibility);
            if (currentUserId != null) {
                query.or().eq(Post::getUserId, currentUserId);
                if (!followingUserIds.isEmpty()) {
                    query.or(nested -> nested.eq(Post::getVisibility, VISIBILITY_FOLLOWERS)
                            .in(Post::getUserId, followingUserIds));
                }
            }
        });
    }

    private List<Long> visibleFollowingUserIds(Long currentUserId) {
        return currentUserId == null ? List.of() : userFollowService.listFollowingUserIds(currentUserId);
    }

    private boolean isPublished(Post post) {
        return Integer.valueOf(1).equals(post.getStatus()) || Integer.valueOf(2).equals(post.getStatus());
    }

    @Override
    public boolean canViewPost(Post post, Long currentUserId) {
        if (post == null || !isPublished(post)) {
            return false;
        }
        String visibility = post.getVisibility();
        if (visibility == null || VISIBILITY_PUBLIC.equals(visibility)) {
            return true;
        }
        if (!VISIBILITY_FOLLOWERS.equals(visibility) || currentUserId == null) {
            return false;
        }
        return post.getUserId().equals(currentUserId) || userFollowService.isFollowing(currentUserId, post.getUserId());
    }
}
