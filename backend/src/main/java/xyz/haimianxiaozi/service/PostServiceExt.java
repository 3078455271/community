package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.vo.PostVO;

public interface PostServiceExt {

    Page<PostVO> getPostPage(int page, int size, Long categoryId);

    PostVO getPostDetail(Long id);

    void save(Post post);

    Post getById(Long id);

    void updateById(Post post);

    void removeById(Long id);
}