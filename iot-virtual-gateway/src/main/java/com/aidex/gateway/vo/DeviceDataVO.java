package com.aidex.gateway.vo;

import cn.hutool.core.bean.BeanUtil;
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
public class DeviceDataVO extends SubDeviceDataVO {
    /**
     * 设备id
     */
    private String id;
    /**
     * 上位机设备id
     */
    private String deviceId;

    public DeviceDataVO(SubDeviceDataVO vo) {
        BeanUtil.copyProperties(vo, this);
    }
}
