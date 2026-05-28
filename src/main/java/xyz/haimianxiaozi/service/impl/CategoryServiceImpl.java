package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.Category;
import xyz.haimianxiaozi.mapper.CategoryMapper;
import xyz.haimianxiaozi.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> listAll() {
        return list();
    }
}