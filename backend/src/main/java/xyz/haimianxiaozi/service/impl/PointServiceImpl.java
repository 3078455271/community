package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.haimianxiaozi.entity.PointLog;
import xyz.haimianxiaozi.entity.UserPoint;
import xyz.haimianxiaozi.mapper.PointLogMapper;
import xyz.haimianxiaozi.mapper.UserPointMapper;
import xyz.haimianxiaozi.service.PointService;
import xyz.haimianxiaozi.vo.UserPointVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointServiceImpl extends ServiceImpl<UserPointMapper, UserPoint> implements PointService {

    private static final int SIGN_IN_POINTS = 5;
    private static final int POST_POINTS = 10;
    private static final int COMMENT_POINTS = 2;

    private final PointLogMapper pointLogMapper;

    @Override
    public UserPointVO getPointInfo(Long userId) {
        return toVO(getOrCreate(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPointVO signIn(Long userId) {
        UserPoint point = getOrCreate(userId);
        if (isSignedInToday(point)) {
            return toVO(point);
        }

        point.setPoints(point.getPoints() + SIGN_IN_POINTS);
        point.setLevel(calculateLevel(point.getPoints()));
        point.setSignInDays(point.getSignInDays() + 1);
        point.setLastSignInAt(LocalDateTime.now());
        updateById(point);
        saveLog(userId, "SIGN_IN", SIGN_IN_POINTS, "每日签到");
        return toVO(point);
    }

    @Override
    public void rewardPost(Long userId) {
        addPoints(userId, "POST", POST_POINTS, "发布帖子");
    }

    @Override
    public void rewardComment(Long userId) {
        addPoints(userId, "COMMENT", COMMENT_POINTS, "发表评论");
    }

    private void addPoints(Long userId, String action, int points, String description) {
        UserPoint userPoint = getOrCreate(userId);
        userPoint.setPoints(userPoint.getPoints() + points);
        userPoint.setLevel(calculateLevel(userPoint.getPoints()));
        updateById(userPoint);
        saveLog(userId, action, points, description);
    }

    private UserPoint getOrCreate(Long userId) {
        UserPoint point = getOne(new LambdaQueryWrapper<UserPoint>().eq(UserPoint::getUserId, userId), false);
        if (point != null) {
            return point;
        }

        UserPoint newPoint = new UserPoint();
        newPoint.setUserId(userId);
        newPoint.setPoints(0);
        newPoint.setLevel(1);
        newPoint.setSignInDays(0);
        save(newPoint);
        return newPoint;
    }

    private void saveLog(Long userId, String action, int points, String description) {
        PointLog log = new PointLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setPoints(points);
        log.setDescription(description);
        pointLogMapper.insert(log);
    }

    private int calculateLevel(int points) {
        return points / 100 + 1;
    }

    private boolean isSignedInToday(UserPoint point) {
        if (point.getLastSignInAt() == null) {
            return false;
        }
        return point.getLastSignInAt().toLocalDate().equals(LocalDate.now());
    }

    private UserPointVO toVO(UserPoint point) {
        UserPointVO vo = new UserPointVO();
        vo.setUserId(point.getUserId());
        vo.setPoints(point.getPoints());
        vo.setLevel(point.getLevel());
        vo.setSignInDays(point.getSignInDays());
        vo.setLastSignInAt(point.getLastSignInAt());
        vo.setSignedInToday(isSignedInToday(point));
        return vo;
    }
}
