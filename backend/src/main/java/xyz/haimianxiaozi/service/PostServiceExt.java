package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.vo.PostVO;

public interface PostServiceExt extends IService<Post> {

    Page<PostVO> getPostPage(int page, int size, Long categoryId);

    PostVO getPostDetail(Long id);

    /**
     * 搜索帖子
     *
     * @param page      页码
     * @param size      每页大小
     * @param keyword   搜索关键词
     * @return 帖子分页
     */
    Page<PostVO> searchPosts(int page, int size, String keyword);
}