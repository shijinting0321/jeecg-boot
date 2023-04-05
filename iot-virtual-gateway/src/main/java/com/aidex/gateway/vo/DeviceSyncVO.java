package com.aidex.gateway.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
public class DeviceSyncVO {
    private String messageId;
    private List<SyncResultVO> syncResult;
    private List<PlatformDeviceVO> add;
    private List<PlatformDeviceVO> update;
    private List<String> delete;
}
