package xyz.haimianxiaozi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.service.TagService;
import xyz.haimianxiaozi.vo.TagVO;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/hot")
    public R<List<TagVO>> hotTags(@RequestParam(defaultValue = "20") int limit) {
        return R.ok(tagService.listHotTags(limit));
    }
}
