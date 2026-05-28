package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.Comment;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.CommentMapper;
import xyz.haimianxiaozi.service.CommentService;
import xyz.haimianxiaozi.service.UserService;
import xyz.haimianxiaozi.vo.CommentVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserService userService;

    @Override
    public List<CommentVO> getCommentsByPostId(Long postId) {
        // 查询所有评论
        List<Comment> comments = list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .orderByAsc(Comment::getCreatedAt));

        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有用户ID
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 转换为VO
        List<CommentVO> voList = comments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());

        // 构建树形结构
        Map<Long, List<CommentVO>> parentMap = voList.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        voList.forEach(vo -> vo.setChildren(parentMap.getOrDefault(vo.getId(), new ArrayList<>())));

        return voList.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
    }
}