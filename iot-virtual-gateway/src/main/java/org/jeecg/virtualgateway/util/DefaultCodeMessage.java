package org.jeecg.virtualgateway.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DefaultCodeMessage implements CodeMessage {

    private static final long serialVersionUID = 1L;

    public static final DefaultCodeMessage SUCCESS = new DefaultCodeMessage("00000", "一切 ok");

    /**
     * 错误码
     */
    private @NonNull String code;
    /**
     * 提示信息
     */
    private String message;

    public DefaultCodeMessage() {
    }

    public DefaultCodeMessage(@NonNull String code, String message) {
        this.code = code;
        this.message = message;
    }
}
