package org.jeecg.virtualgateway.service.impl;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.jeecg.virtualgateway.plugin.handler.IPluginHandler;
import org.jeecg.virtualgateway.plugin.listener.SoftPluginListener;
import org.jeecg.virtualgateway.service.IAsyncService;
import org.jeecg.virtualgateway.service.IDeviceService;
import org.jeecg.virtualgateway.service.IMqttService;
import org.jeecg.virtualgateway.util.MqttCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jtcl
 * @date 2022/6/17
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {
    private IMqttService mqttService;
    private IDeviceService deviceService;
    private SoftPluginListener pluginListener;

    @Override
    public void syncDevice(String messageId) {
        MqttCodeEnum result = MqttCodeEnum.SUCCESS;
        try {
            syncDevice();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("设备同步失败: {}", e.getMessage(), e);
            }

            result = MqttCodeEnum.ERROR;
        }

        getMqttService().publishBaseControlResult(messageId, result);
    }

    @Override
    public void syncDevice() {
        getDeviceService().syncDevice();
    }

    @Override
    public void syncDevice(String pluginId, IPluginHandler handler) {
        getPluginListener().executeSyncDeviceTask(pluginId, handler);
    }

    @Override
    public void syncDeviceStatus() {
        getDeviceService().syncDeviceStatus(true);
    }

    @Override
    public void syncDeviceStatus(String pluginId, IPluginHandler handler) {
        getPluginListener().executeSyncDeviceStatusTask(pluginId, handler);
    }

    @Override
    public void listDeviceData() {
        getDeviceService().listDeviceData(true);
    }

    @Override
    public void listDeviceData(String pluginId, IPluginHandler handler) {
        getPluginListener().executeListDeviceDataTask(pluginId, handler);
    }

    @Override
    public void reboot(String messageId, List<String> deviceIds) {
        if (log.isDebugEnabled()) {
            log.debug("设备重启: {},{}", messageId, deviceIds);
        }

        MqttCodeEnum result = MqttCodeEnum.SUCCESS;

        deviceIds = Optional.ofNullable(deviceIds)
                .orElseGet(ListUtil::empty)
                .stream()
                .filter(deviceId -> {
                    try {
                        getDeviceService().rebootDevice(deviceId);
                        return false;
                    } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                            log.error("设备重启失败: {}", e.getMessage(), e);
                        }

                        return true;
                    }
                })
                .collect(Collectors.toList());

        if (!deviceIds.isEmpty()) {
            result = MqttCodeEnum.ERROR;
        }

        getMqttService().publishBaseControlResult(messageId, result);
    }

    @Override
    public void operate(String messageId, String deviceId, String code, String value, long timestamp) {
        if (log.isDebugEnabled()) {
            log.debug("设备控制: {},{},{},{},{}", messageId, deviceId, code, value, timestamp);
        }

        MqttCodeEnum result = MqttCodeEnum.SUCCESS;
        try {
            getDeviceService().operateDevice(deviceId, code, value);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("设备控制失败: {}", e.getMessage(), e);
            }

            result = MqttCodeEnum.ERROR;
        }

        getMqttService().publishControlResult(messageId, result);
    }

    private IMqttService getMqttService() {
        if (Objects.isNull(mqttService)) {
            synchronized (this) {
                if (Objects.isNull(mqttService)) {
                    mqttService = SpringUtil.getBean(IMqttService.class);
                }
            }
        }
        return mqttService;
    }

    private IDeviceService getDeviceService() {
        if (Objects.isNull(deviceService)) {
            synchronized (this) {
                if (Objects.isNull(deviceService)) {
                    deviceService = SpringUtil.getBean(IDeviceService.class);
                }
            }
        }
        return deviceService;
    }

    private SoftPluginListener getPluginListener() {
        if (Objects.isNull(pluginListener)) {
            synchronized (this) {
                if (Objects.isNull(pluginListener)) {
                    pluginListener = SpringUtil.getBean(SoftPluginListener.class);
                }
            }
        }
        return pluginListener;
    }
}
