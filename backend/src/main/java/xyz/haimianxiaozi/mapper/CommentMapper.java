package xyz.haimianxiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.haimianxiaozi.entity.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}