package com.aidex.gateway.service;

import com.aidex.gateway.plugin.handler.IPluginHandler;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * @author jtcl
 * @date 2022/6/7
 */
@Async("asyncMqttExecutor")
public interface IAsyncService {
    /**
     * 设备同步
     *
     * @param messageId 指令唯一标识
     */
    void syncDevice(String messageId);

    /**
     * 设备同步
     */
    void syncDevice();

    /**
     * 设备同步
     *
     * @param pluginId 驱动id
     * @param handler  驱动实现类
     */
    void syncDevice(String pluginId, IPluginHandler handler);

    /**
     * 同步设备状态
     */
    void syncDeviceStatus();

    /**
     * 同步设备状态
     *
     * @param pluginId 驱动id
     * @param handler  驱动实现类
     */
    void syncDeviceStatus(String pluginId, IPluginHandler handler);

    /**
     * 采集设备数据
     */
    void listDeviceData();

    /**
     * 采集设备数据
     *
     * @param pluginId 驱动id
     * @param handler  驱动实现类
     */
    void listDeviceData(String pluginId, IPluginHandler handler);

    /**
     * 设备重启
     *
     * @param messageId 指令唯一标识
     * @param deviceIds 上位机设备ids
     */
    void reboot(String messageId, List<String> deviceIds);

    /**
     * 设备控制
     *
     * @param messageId 指令唯一标识
     * @param deviceId  上位机设备id
     * @param code      功能码
     * @param value     下发值
     * @param timestamp 指令时间戳
     */
    void operate(String messageId, String deviceId, String code, String value, long timestamp);
}
