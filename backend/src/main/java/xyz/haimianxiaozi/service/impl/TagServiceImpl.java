package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.haimianxiaozi.entity.PostTag;
import xyz.haimianxiaozi.entity.Tag;
import xyz.haimianxiaozi.mapper.TagMapper;
import xyz.haimianxiaozi.service.TagService;
import xyz.haimianxiaozi.mapper.PostTagMapper;
import xyz.haimianxiaozi.vo.TagVO;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private static final int MAX_TAG_COUNT = 5;
    private static final int MAX_TAG_LENGTH = 20;

    private final PostTagMapper postTagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPostTags(Long postId, List<String> tagNames, boolean newPublished, boolean oldPublished) {
        List<PostTag> oldRelations = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getPostId, postId));
        if (oldPublished && !oldRelations.isEmpty()) {
            decreasePostCount(oldRelations.stream().map(PostTag::getTagId).toList());
        }
        oldRelations.forEach(relation -> postTagMapper.deleteById(relation.getId()));

        List<String> normalizedNames = normalizeTagNames(tagNames);
        if (normalizedNames.isEmpty()) {
            return;
        }

        List<Tag> tags = normalizedNames.stream().map(this::getOrCreateTag).toList();
        for (Tag tag : tags) {
            PostTag relation = new PostTag();
            relation.setPostId(postId);
            relation.setTagId(tag.getId());
            postTagMapper.insert(relation);
        }
        if (newPublished) {
            increasePostCount(tags.stream().map(Tag::getId).toList());
        }
    }

    @Override
    public List<TagVO> listPostTags(Long postId) {
        List<PostTag> relations = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getPostId, postId));
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tagIds = relations.stream().map(PostTag::getTagId).toList();
        Map<Long, Tag> tagMap = listByIds(tagIds).stream().collect(Collectors.toMap(Tag::getId, t -> t));
        return relations.stream()
                .map(relation -> tagMap.get(relation.getTagId()))
                .filter(tag -> tag != null)
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<TagVO> listHotTags(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return list(new LambdaQueryWrapper<Tag>()
                .orderByDesc(Tag::getPostCount)
                .orderByDesc(Tag::getUpdatedAt)
                .last("LIMIT " + safeLimit))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private Tag getOrCreateTag(String name) {
        Tag tag = getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name), false);
        if (tag != null) {
            return tag;
        }

        Tag newTag = new Tag();
        newTag.setName(name);
        newTag.setPostCount(0);
        newTag.setDeleted(0);
        save(newTag);
        return newTag;
    }

    private List<String> normalizeTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> names = new LinkedHashSet<>();
        for (String tagName : tagNames) {
            if (tagName == null) {
                continue;
            }
            String name = tagName.trim();
            if (name.startsWith("#")) {
                name = name.substring(1).trim();
            }
            if (!name.isBlank() && name.length() <= MAX_TAG_LENGTH) {
                names.add(name);
            }
            if (names.size() >= MAX_TAG_COUNT) {
                break;
            }
        }
        return new ArrayList<>(names);
    }

    private void increasePostCount(List<Long> tagIds) {
        tagIds.forEach(tagId -> update(new LambdaUpdateWrapper<Tag>()
                .eq(Tag::getId, tagId)
                .setSql("post_count = post_count + 1")));
    }

    private void decreasePostCount(List<Long> tagIds) {
        tagIds.forEach(tagId -> update(new LambdaUpdateWrapper<Tag>()
                .eq(Tag::getId, tagId)
                .setSql("post_count = GREATEST(post_count - 1, 0)")));
    }

    private TagVO toVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }
}
