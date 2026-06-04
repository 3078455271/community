package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.FavoriteFolder;
import xyz.haimianxiaozi.mapper.FavoriteFolderMapper;
import xyz.haimianxiaozi.service.FavoriteFolderService;

@Service
@RequiredArgsConstructor
public class FavoriteFolderServiceImpl extends ServiceImpl<FavoriteFolderMapper, FavoriteFolder>
        implements FavoriteFolderService {

    private static final String DEFAULT_FOLDER_NAME = "默认收藏夹";

    @Override
    public FavoriteFolder getOrCreateDefaultFolder(Long userId) {
        FavoriteFolder folder = getOne(new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getUserId, userId)
                .eq(FavoriteFolder::getName, DEFAULT_FOLDER_NAME), false);
        if (folder != null) {
            return folder;
        }

        FavoriteFolder defaultFolder = new FavoriteFolder();
        defaultFolder.setUserId(userId);
        defaultFolder.setName(DEFAULT_FOLDER_NAME);
        defaultFolder.setSort(0);
        defaultFolder.setDeleted(0);
        save(defaultFolder);
        return defaultFolder;
    }
}
