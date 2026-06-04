package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.UserFollow;

import java.util.List;

public interface UserFollowService extends IService<UserFollow> {

    boolean follow(Long followerId, Long followingId);

    boolean unfollow(Long followerId, Long followingId);

    boolean isFollowing(Long followerId, Long followingId);

    long countFollowing(Long userId);

    long countFollowers(Long userId);

    List<Long> listFollowingUserIds(Long userId);
}
