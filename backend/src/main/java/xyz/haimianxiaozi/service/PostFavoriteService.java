package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.PostFavorite;
import xyz.haimianxiaozi.vo.PostVO;

public interface PostFavoriteService extends IService<PostFavorite> {

    boolean favorite(Long userId, Long postId, Long folderId);

    boolean unfavorite(Long userId, Long postId);

    boolean isFavorited(Long userId, Long postId);

    Page<PostVO> getFavoritePosts(Long userId, Long folderId, int page, int size);
}
