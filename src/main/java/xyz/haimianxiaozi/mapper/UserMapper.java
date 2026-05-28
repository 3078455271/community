package xyz.haimianxiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.haimianxiaozi.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}