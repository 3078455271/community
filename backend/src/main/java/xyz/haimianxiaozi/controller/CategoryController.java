package xyz.haimianxiaozi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.entity.Category;
import xyz.haimianxiaozi.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public R<List<Category>> list() {
        return R.ok(categoryService.listAll());
    }
}