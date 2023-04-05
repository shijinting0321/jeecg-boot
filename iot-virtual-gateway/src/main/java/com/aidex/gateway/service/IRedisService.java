package com.aidex.gateway.service;

import java.util.List;
import java.util.Map;

/**
 * @author jtcl
 * @date 2022/6/7
 */
public interface IRedisService {
    /**
     * 缓存设备信息
     *
     * @param serial  设备序列号
     * @param version 设备版本
     * @param info    设备信息
     */
    void saveInfo(String serial, String version, String info);

    /**
     * 获取缓存设备信息
     *
     * @param serial  设备序列号
     * @param version 设备版本
     */
    String getInfo(String serial, String version);

    /**
     * 缓存设备授权
     *
     * @param deviceKey   deviceKey
     * @param accessToken 授权token
     * @param expire      过期时间
     */
    void saveToken(String deviceKey, String accessToken, int expire);

    /**
     * 获取缓存设备授权
     *
     * @param deviceKey deviceKey
     */
    String getToken(String deviceKey);

    /**
     * 缓存设备状态
     *
     * @param deviceKey deviceKey
     * @param map       设备状态
     */
    void saveDeviceStatus(String deviceKey, Map<String, String> map);

    /**
     * 获取缓存设备状态
     *
     * @param deviceKey deviceKey
     * @return 设备状态
     */
    Map<Object, Object> getDeviceStatus(String deviceKey);

    /**
     * 清除缓存设备状态
     *
     * @param deviceKey deviceKey
     */
    void removeDeviceStatus(String deviceKey);

    /**
     * 清除缓存设备状态
     *
     * @param deviceKey deviceKey
     * @param deviceIds 设备唯一标识
     */
    void removeDeviceStatus(String deviceKey, List<String> deviceIds);

    /**
     * 缓存设备数据
     *
     * @param deviceKey deviceKey
     * @param map       设备数据
     */
    void saveDeviceData(String deviceKey, Map<String, String> map);

    /**
     * 获取缓存设备数据
     *
     * @param deviceKey deviceKey
     * @return 设备数据
     */
    Map<Object, Object> getDeviceData(String deviceKey);

    /**
     * 清除缓存设备数据
     *
     * @param deviceKey deviceKey
     */
    void removeDeviceData(String deviceKey);

    /**
     * 清除缓存设备数据
     *
     * @param deviceKey deviceKey
     * @param deviceIds 设备唯一标识
     */
    void removeDeviceData(String deviceKey, List<String> deviceIds);
}
