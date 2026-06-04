package xyz.haimianxiaozi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.service.PointService;
import xyz.haimianxiaozi.util.UserContext;
import xyz.haimianxiaozi.vo.UserPointVO;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final UserContext userContext;

    @GetMapping("/me")
    public R<UserPointVO> me() {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(pointService.getPointInfo(userId));
    }

    @PostMapping("/sign-in")
    public R<UserPointVO> signIn() {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(pointService.signIn(userId));
    }
}
