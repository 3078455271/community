package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.Comment;
import xyz.haimianxiaozi.vo.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<CommentVO> getCommentsByPostId(Long postId);
}