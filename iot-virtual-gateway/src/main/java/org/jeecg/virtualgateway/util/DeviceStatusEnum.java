package org.jeecg.virtualgateway.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * @author jtcl
 * @date 2022/6/16
 */
@JSONType(serializeEnumAsJavaBean = true)
public enum DeviceStatusEnum implements CodeMessage {
    ONLINE("0", "正常上线"),
    OFFLINE("1", "正常下线"),
    EXCEPTION("-1", "异常下线");

    @Getter
    private final String code;
    @Getter
    private final String message;

    DeviceStatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Nullable
    public static DeviceStatusEnum fromCode(final String code) {
        for (DeviceStatusEnum value : values()) {
            if (StrUtil.equals(value.getCode(), code)) {
                return value;
            }
        }

        return null;
    }
}
