package com.aidex.gateway.plugin.handler;

import cn.hutool.core.util.ClassUtil;
import com.aidex.gateway.vo.SubDeviceDataVO;
import com.aidex.gateway.vo.SubDeviceStatusVO;
import com.aidex.gateway.vo.SubDeviceVO;
import com.aidex.gateway.vo.SyncDeviceVO;

import java.util.List;

/**
 * @author jtcl
 * @date 2022/6/8
 */
public interface IPluginHandler {
    String DEFAULT_TASK_CRON = "0 */1 * * * ?";

    /**
     * 驱动操作名称
     *
     * @return 名称
     */
    default String name() {
        return ClassUtil.getClassName(this, true);
    }

    /**
     * 返回支持的品牌型号
     *
     * @return 品牌型号id列表
     */
    List<String> supportBrandModelId();

    /**
     * 查询设备列表
     *
     * @return 设备列表
     */
    List<SubDeviceVO> listDevice();

    /**
     * 查询设备在离线状态
     *
     * @return 设备在离线状态
     */
    List<SubDeviceStatusVO> listDeviceStatus();

    /**
     * 查询设备在离线状态定时表达式
     * 默认: "0 /1 * * * ?"
     *
     * @return cron表达式
     */
    default String listDeviceStatusCron() {
        return DEFAULT_TASK_CRON;
    }

    /**
     * 查询设备数据
     *
     * @return 设备数据
     */
    List<SubDeviceDataVO> listDeviceData();

    /**
     * 查询设备数据定时表达式
     * 默认: "0 /1 * * * ?"
     *
     * @return cron表达式
     */
    default String listDeviceDataCron() {
        return DEFAULT_TASK_CRON;
    }

    /**
     * 设备控制
     *
     * @param serial 设备唯一标识
     * @param code   功能码
     * @param value  下发值
     * @return 控制结果
     */
    boolean operate(String serial, String code, String value);

    /**
     * 设备重启
     *
     * @param serials 设备唯一标识
     * @return 重启结果
     */
    boolean reboot(List<String> serials);

    /**
     * 添加设备
     *
     * @param vo 设备
     * @return 添加结果
     */
    default boolean add(SyncDeviceVO vo) {
        return false;
    }

    /**
     * 更新设备
     *
     * @param vo 设备
     * @return 更新结果
     */
    default boolean update(SyncDeviceVO vo) {
        return false;
    }

    /**
     * 删除设备
     *
     * @param serials 设备唯一标识
     * @return 删除结果
     */
    default boolean delete(List<String> serials) {
        return true;
    }
}
