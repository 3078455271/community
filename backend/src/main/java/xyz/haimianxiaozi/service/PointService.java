package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.UserPoint;
import xyz.haimianxiaozi.vo.UserPointVO;

public interface PointService extends IService<UserPoint> {

    UserPointVO getPointInfo(Long userId);

    UserPointVO signIn(Long userId);

    void rewardPost(Long userId);

    void rewardComment(Long userId);
}
