package com.hzq.web.exception;

import com.hzq.core.result.ResultEnum;
import lombok.Getter;

import java.io.Serial;

/**
 * @author hua
 * @className com.hua.common.exception SystemError
 * @date 2024/8/24 12:14
 * @description 自定义系统业务异常
 */
@Getter
public class SystemException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private int code;
    private String msg;

    public SystemException() {}

    public SystemException(ResultEnum resultEnum) {
        this(resultEnum.getCode(), resultEnum.getMessage());
    }

    public SystemException(String msg) {
        this(ResultEnum.CUSTOM_ERROR.getCode(), msg);
    }

    public SystemException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
