package com.aidex.gateway.service;

import com.aidex.gateway.util.DeviceStatusEnum;
import com.aidex.gateway.util.MqttCodeEnum;
import com.aidex.gateway.vo.MqttDataVO;
import com.hivemq.client.mqtt.datatypes.MqttQos;

import java.util.List;
import java.util.Map;

/**
 * @author jtcl
 * @date 2022/6/7
 */
public interface IMqttService {
    /**
     * 初始化客户端
     */
    void initClient();

    /**
     * 推送设备状态
     *
     * @param deviceId 上位机设备id
     * @param status   设备状态
     */
    void publishStatus(String deviceId, DeviceStatusEnum status);

    /**
     * 推送设备状态
     *
     * @param status 上位机设备id-设备状态
     */
    void publishStatus(Map<String, DeviceStatusEnum> status);

    /**
     * 推送采集数据
     *
     * @param data 采集数据
     */
    void publishData(List<MqttDataVO> data);

    /**
     * 推送基础控制反馈
     *
     * @param messageId 指令唯一标识
     * @param code      反馈码
     */
    void publishBaseControlResult(String messageId, MqttCodeEnum code);

    /**
     * 推送控制反馈
     *
     * @param messageId 指令唯一标识
     * @param code      反馈码
     */
    void publishControlResult(String messageId, MqttCodeEnum code);

    /**
     * 推送数据
     *
     * @param topic  主题
     * @param qos    qos
     * @param retain retain
     * @param data   推送数据
     */
    void publish(String topic, MqttQos qos, boolean retain, Object data);
}
