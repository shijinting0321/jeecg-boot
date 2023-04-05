package org.jeecg.virtualgateway.service;

import org.jeecg.virtualgateway.entity.Device;
import org.jeecg.virtualgateway.entity.Gateway;
import org.jeecg.virtualgateway.plugin.handler.IPluginHandler;
import org.jeecg.virtualgateway.vo.DeviceSyncVO;
import org.jeecg.virtualgateway.vo.DeviceVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jtcl
 * @date 2022/6/7
 */
public interface IDeviceService extends IService<Device> {
    /**
     * 添加设备
     *
     * @param vo 设备信息
     */
    void saveDevice(DeviceVO vo);

    /**
     * 更新设备
     *
     * @param vo 设备信息
     */
    void updateDeviceById(DeviceVO vo);

    /**
     * 同步设备
     */
    void syncDevice();

    /**
     * 同步设备
     *
     * @param pluginId 驱动id
     * @param handler  驱动实现类
     */
    void syncDevice(String pluginId, IPluginHandler handler);

    /**
     * 申请同步
     *
     * @param pluginId 驱动id
     * @param handler  驱动实现类
     * @param info     网关设备信息
     * @return 同步响应
     */
    DeviceSyncVO applySync(String pluginId, IPluginHandler handler, Gateway info);

    /**
     * 推送同步响应处理结果
     *
     * @param info 网关设备信息
     * @param vo   同步响应
     */
    void pushSyncResult(Gateway info, DeviceSyncVO vo);

    /**
     * 同步设备状态
     *
     * @param clearCache 是否清除状态缓存
     */
    void syncDeviceStatus(boolean clearCache);

    /**
     * 同步设备状态
     *
     * @param pluginId 驱动id
     * @param handler  驱动实现类
     */
    void syncDeviceStatus(String pluginId, IPluginHandler handler);

    /**
     * 采集设备数据
     *
     * @param clearCache 是否清除状态缓存
     */
    void listDeviceData(boolean clearCache);

    /**
     * 采集设备数据
     *
     * @param pluginId 驱动id
     * @param handler  驱动实现类
     */
    void listDeviceData(String pluginId, IPluginHandler handler);

    /**
     * 上传设备数据
     */
    void syncDeviceData();

    /**
     * 控制设备
     *
     * @param deviceId 上位机设备id
     * @param code     功能码
     * @param value    下发值
     */
    void operateDevice(String deviceId, String code, String value);

    /**
     * 重启设备
     *
     * @param deviceId 上位机设备id
     */
    void rebootDevice(String deviceId);
}
