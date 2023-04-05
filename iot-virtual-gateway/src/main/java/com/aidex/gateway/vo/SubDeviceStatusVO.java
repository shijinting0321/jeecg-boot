package com.aidex.gateway.vo;

import com.aidex.gateway.util.DeviceStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class SubDeviceStatusVO {
    /**
     * 设备唯一标识
     */
    private String serial;
    /**
     * 设备在离线状态
     */
    private DeviceStatusEnum status;
}
