package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.FavoriteFolder;

public interface FavoriteFolderService extends IService<FavoriteFolder> {

    FavoriteFolder getOrCreateDefaultFolder(Long userId);
}
