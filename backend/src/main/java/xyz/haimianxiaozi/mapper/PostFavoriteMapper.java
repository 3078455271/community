package xyz.haimianxiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import xyz.haimianxiaozi.entity.PostFavorite;

@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {

    @Select("""
            SELECT *
            FROM post_favorite
            WHERE user_id = #{userId}
              AND post_id = #{postId}
            LIMIT 1
            """)
    PostFavorite selectAnyByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    @Update("""
            UPDATE post_favorite
            SET folder_id = #{folderId},
                deleted = 0,
                updated_at = NOW()
            WHERE id = #{id}
            """)
    int restoreById(@Param("id") Long id, @Param("folderId") Long folderId);
}
