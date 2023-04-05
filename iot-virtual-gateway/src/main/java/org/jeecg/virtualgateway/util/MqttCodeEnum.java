package org.jeecg.virtualgateway.util;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Getter;

/**
 * @author jtcl
 * @date 2022/6/16
 */
@JSONType(serializeEnumAsJavaBean = true)
public enum MqttCodeEnum implements CodeMessage {
    SUCCESS("00000", "成功"),
    ERROR("00002", "失败"),
    TIMEOUT("00003", "指令执行超时"),
    COMMAND_ERROR("00005", "指令执行失败,属性被高等级指令占用"),
    DEVICE_ERROR("00007", "指令执行失败,设备离线"),
    GATEWAY_ERROR("00008", "解锁指令执行失败,属性被网关本地锁占用");

    @Getter
    private final String code;
    @Getter
    private final String message;

    MqttCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
