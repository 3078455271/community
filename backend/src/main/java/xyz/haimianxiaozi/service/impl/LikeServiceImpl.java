package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.haimianxiaozi.entity.Like;
import xyz.haimianxiaozi.enums.CommonEnums.LikeTargetType;
import xyz.haimianxiaozi.mapper.LikeMapper;
import xyz.haimianxiaozi.service.LikeService;

/**
 * 点赞服务实现
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean like(Long userId, Long targetId, LikeTargetType targetType) {
        // 检查是否已点赞
        if (isLiked(userId, targetId, targetType)) {
            return false;
        }

        Like like = new Like();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setTargetType(targetType.getCode());

        return save(like);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlike(Long userId, Long targetId, LikeTargetType targetType) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, targetId)
                .eq(Like::getTargetType, targetType.getCode());

        return remove(wrapper);
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeTargetType targetType) {
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, targetId)
                .eq(Like::getTargetType, targetType.getCode());

        return count(wrapper) > 0;
    }
}