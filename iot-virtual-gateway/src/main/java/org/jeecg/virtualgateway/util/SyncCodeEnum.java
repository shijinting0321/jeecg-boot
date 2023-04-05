package org.jeecg.virtualgateway.util;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Getter;

/**
 * @author jtcl
 * @date 2022/6/16
 */
@JSONType(serializeEnumAsJavaBean = true)
public enum SyncCodeEnum implements CodeMessage {
    SUCCESS("00000", "设备同步成功"),
    DEVICE_SHADOW_ERROR("10000", "设备影子处理失败"),
    BRAND_MODEL_NOT_EXIST("10001", "品牌型号不存在"),
    FORMAT_ERROR("10002", "格式错误"),
    ERROR("10003", "异常错误"),
    KEYWORD_TYPE_ERROR("10004", "关键字类型错误"),
    DEVICE_EXCEPTION("10005", "上级设备异常,设备不存在"),
    CHANNEL_EXCEPTION("10006", "上级不存在该通道"),
    CONNECT_POINT_ERROR("10100", "新增设备失败,连接点位错误(缺少必要的连接属性,例如地址/通道)"),
    DUPLICATE_DEVICE_ID_ERROR("10101", "新增设备失败,重复的设备平台id"),
    INVALID_DEVICE_ERROR("10200", "更新设备失败,无效设备平台id"),
    SAVE_ERROR("10300", "平台新增失败,落库异常"),
    UPDATE_ERROR("10301", "平台修改失败,落库异常"),
    DUPLICATE_DEVICE_NAME_ERROR("10302", "平台同步失败,设备名称重复"),
    CONNECT_PROPERTY_NOT_EXIST("10400", "同步失败,连接属性不存在"),
    NULL_SERIAL_ERROR("10500", "序列号为空"),
    DUPLICATE_SERIAL_ERROR("10501", "序列号已经被占用"),
    DEVICE_ACTIVATED_ERROR("10502", "设备已经激活");

    @Getter
    private final String code;
    @Getter
    private final String message;

    SyncCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
