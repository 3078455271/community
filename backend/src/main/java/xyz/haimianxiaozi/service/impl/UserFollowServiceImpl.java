package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.UserFollow;
import xyz.haimianxiaozi.mapper.UserFollowMapper;
import xyz.haimianxiaozi.service.UserFollowService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    @Override
    public boolean follow(Long followerId, Long followingId) {
        UserFollow existing = baseMapper.selectAnyByPair(followerId, followingId);
        if (existing != null) {
            if (Integer.valueOf(0).equals(existing.getDeleted())) {
                return false;
            }
            existing.setDeleted(0);
            return updateById(existing);
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        follow.setDeleted(0);
        return save(follow);
    }

    @Override
    public boolean unfollow(Long followerId, Long followingId) {
        UserFollow existing = getActiveFollow(followerId, followingId);
        if (existing == null) {
            return false;
        }
        existing.setDeleted(1);
        return updateById(existing);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return getActiveFollow(followerId, followingId) != null;
    }

    @Override
    public long countFollowing(Long userId) {
        return count(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getDeleted, 0));
    }

    @Override
    public long countFollowers(Long userId) {
        return count(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, userId)
                .eq(UserFollow::getDeleted, 0));
    }

    @Override
    public List<Long> listFollowingUserIds(Long userId) {
        return list(new LambdaQueryWrapper<UserFollow>()
                .select(UserFollow::getFollowingId)
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getDeleted, 0))
                .stream()
                .map(UserFollow::getFollowingId)
                .toList();
    }

    private UserFollow getActiveFollow(Long followerId, Long followingId) {
        return getOne(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId)
                .eq(UserFollow::getDeleted, 0), false);
    }
}
