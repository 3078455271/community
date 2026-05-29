package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> listAll();
}