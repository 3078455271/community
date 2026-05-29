package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.Like;
import xyz.haimianxiaozi.enums.CommonEnums.LikeTargetType;

public interface LikeService extends IService<Like> {

    /**
     * 点赞
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 是否成功
     */
    boolean like(Long userId, Long targetId, LikeTargetType targetType);

    /**
     * 取消点赞
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 是否成功
     */
    boolean unlike(Long userId, Long targetId, LikeTargetType targetType);

    /**
     * 检查是否已点赞
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 是否已点赞
     */
    boolean isLiked(Long userId, Long targetId, LikeTargetType targetType);
}