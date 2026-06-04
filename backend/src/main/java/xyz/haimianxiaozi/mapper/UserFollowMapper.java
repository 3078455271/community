package xyz.haimianxiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.haimianxiaozi.entity.UserFollow;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    @Select("""
            SELECT *
            FROM user_follow
            WHERE follower_id = #{followerId}
              AND following_id = #{followingId}
            LIMIT 1
            """)
    UserFollow selectAnyByPair(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
