package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.User;

public interface UserService extends IService<User> {

    User getByUsername(String username);
}