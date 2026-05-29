package xyz.haimianxiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.haimianxiaozi.entity.Post;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}