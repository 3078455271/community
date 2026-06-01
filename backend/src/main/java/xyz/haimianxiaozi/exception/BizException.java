package xyz.haimianxiaozi.exception;

import lombok.Getter;
import xyz.haimianxiaozi.enums.ErrorCode;

/**
 * 业务异常，用于在 Service / Controller 中以受控方式抛出可预期的错误。
 * 由 {@link GlobalExceptionHandler} 统一捕获并转换为标准响应。
 *
 * @author haimianxiaozi
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
