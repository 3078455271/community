package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.Tag;
import xyz.haimianxiaozi.vo.TagVO;

import java.util.List;

public interface TagService extends IService<Tag> {

    void syncPostTags(Long postId, List<String> tagNames, boolean newPublished, boolean oldPublished);

    List<TagVO> listPostTags(Long postId);

    List<TagVO> listHotTags(int limit);
}
