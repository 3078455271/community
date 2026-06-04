package xyz.haimianxiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.haimianxiaozi.entity.Post;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("""
            <script>
            SELECT p.*
            FROM post p
            LEFT JOIN user u ON p.user_id = u.id AND u.deleted = 0
            WHERE p.deleted = 0
              AND p.status IN (1, 2)
              AND (
                  p.visibility IS NULL
                  OR p.visibility = 'PUBLIC'
                  <if test="currentUserId != null">
                  OR p.user_id = #{currentUserId}
                  </if>
                  <if test="followingUserIds != null and followingUserIds.size() > 0">
                  OR (p.visibility = 'FOLLOWERS' AND p.user_id IN
                      <foreach collection="followingUserIds" item="id" open="(" separator="," close=")">
                          #{id}
                      </foreach>
                  )
                  </if>
              )
              AND (
                  MATCH(p.title, p.content) AGAINST (#{keyword} IN NATURAL LANGUAGE MODE)
                  OR p.title LIKE CONCAT('%', #{keyword}, '%')
                  OR p.content LIKE CONCAT('%', #{keyword}, '%')
                  OR u.username LIKE CONCAT('%', #{keyword}, '%')
                  OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
              )
            ORDER BY p.status DESC,
                     MATCH(p.title, p.content) AGAINST (#{keyword} IN NATURAL LANGUAGE MODE) DESC,
                     p.created_at DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Post> searchPublished(@Param("keyword") String keyword,
                               @Param("offset") long offset,
                               @Param("size") long size,
                               @Param("currentUserId") Long currentUserId,
                               @Param("followingUserIds") List<Long> followingUserIds);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM post p
            LEFT JOIN user u ON p.user_id = u.id AND u.deleted = 0
            WHERE p.deleted = 0
              AND p.status IN (1, 2)
              AND (
                  p.visibility IS NULL
                  OR p.visibility = 'PUBLIC'
                  <if test="currentUserId != null">
                  OR p.user_id = #{currentUserId}
                  </if>
                  <if test="followingUserIds != null and followingUserIds.size() > 0">
                  OR (p.visibility = 'FOLLOWERS' AND p.user_id IN
                      <foreach collection="followingUserIds" item="id" open="(" separator="," close=")">
                          #{id}
                      </foreach>
                  )
                  </if>
              )
              AND (
                  MATCH(p.title, p.content) AGAINST (#{keyword} IN NATURAL LANGUAGE MODE)
                  OR p.title LIKE CONCAT('%', #{keyword}, '%')
                  OR p.content LIKE CONCAT('%', #{keyword}, '%')
                  OR u.username LIKE CONCAT('%', #{keyword}, '%')
                  OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
              )
            </script>
            """)
    long countSearchPublished(@Param("keyword") String keyword,
                              @Param("currentUserId") Long currentUserId,
                              @Param("followingUserIds") List<Long> followingUserIds);
}
