package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.Post;
import xyz.haimianxiaozi.mapper.PostMapper;
import xyz.haimianxiaozi.service.PostService;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
}