package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.vo.PostVO;

public interface PostServiceExt extends IService<Post> {

    Page<PostVO> getPostPage(int page, int size, Long categoryId);

    PostVO getPostDetail(Long id);
}
