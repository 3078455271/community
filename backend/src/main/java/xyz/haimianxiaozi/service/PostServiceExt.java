package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.vo.PostVO;

import java.util.List;

public interface PostServiceExt extends IService<Post> {

    Page<PostVO> getPostPage(int page, int size, Long categoryId);

    Page<PostVO> getPostPageByTag(int page, int size, Long tagId);

    PostVO getPostDetail(Long id);

    Page<PostVO> getFollowingPostPage(int page, int size, Long userId);

    Page<PostVO> getDraftPage(int page, int size, Long userId);

    PostVO getDraftDetail(Long id, Long userId);

    /**
     * 搜索帖子
     *
     * @param page      页码
     * @param size      每页大小
     * @param keyword   搜索关键词
     * @return 帖子分页
     */
    Page<PostVO> searchPosts(int page, int size, String keyword);

    List<PostVO> getRelatedPosts(Long id, int limit);

    void recordViewHistory(Long userId, Long postId);

    Page<PostVO> getViewHistory(int page, int size, Long userId);
}
