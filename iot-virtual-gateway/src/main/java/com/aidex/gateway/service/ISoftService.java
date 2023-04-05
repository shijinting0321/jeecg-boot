package com.aidex.gateway.service;

import com.aidex.gateway.entity.Gateway;

/**
 * @author jtcl
 * @date 2022/6/7
 */
public interface ISoftService {
    /**
     * 初始化设备信息
     */
    String initInfo();

    /**
     * 设备授权
     */
    String initToken();

    /**
     * 获取设备信息
     *
     * @return 设备信息
     */
    Gateway getInfo();

    /**
     * 获取设备授权
     *
     * @return 授权token
     */
    String getToken();
}
