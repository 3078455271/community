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
            SELECT p.*
            FROM post p
            LEFT JOIN user u ON p.user_id = u.id AND u.deleted = 0
            WHERE p.deleted = 0
              AND p.status IN (1, 2)
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
            """)
    List<Post> searchPublished(@Param("keyword") String keyword,
                               @Param("offset") long offset,
                               @Param("size") long size);

    @Select("""
            SELECT COUNT(1)
            FROM post p
            LEFT JOIN user u ON p.user_id = u.id AND u.deleted = 0
            WHERE p.deleted = 0
              AND p.status IN (1, 2)
              AND (
                  MATCH(p.title, p.content) AGAINST (#{keyword} IN NATURAL LANGUAGE MODE)
                  OR p.title LIKE CONCAT('%', #{keyword}, '%')
                  OR p.content LIKE CONCAT('%', #{keyword}, '%')
                  OR u.username LIKE CONCAT('%', #{keyword}, '%')
                  OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
              )
            """)
    long countSearchPublished(@Param("keyword") String keyword);
}
