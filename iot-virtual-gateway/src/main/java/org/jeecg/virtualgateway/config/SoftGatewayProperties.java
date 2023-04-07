package org.jeecg.virtualgateway.config;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author jtcl
 * @date 2022/6/7
 */
@Setter
@Getter
@ToString
@Validated
@ConfigurationProperties(prefix = "iot.gateway")
public class SoftGatewayProperties {
    /**
     * 平台接口地址
     */
    @NotBlank(message = "平台接口地址不能为空")
    private String domain;
    /**
     * 软网关序列号
     */
    @NotBlank(message = "软网关序列号不能为空")
    private String code;
    /**
     * 软网关版本号
     */
    private String version;

    /**
     * 设备采集频率cron表达式
     * 默认每天同步一次
     */
    private String collectDeviceCron = "0 0 0 */1 * ?";

    /**
     * 设备采集数据上传频率cron表达式
     * 默认每3秒上传一次
     */
    private String uploadDataCron = "0/3 * * * * ?";

    /**
     * Mqtt地址
     */
    @NotBlank(message = "Mqtt地址不能为空")
    private String mqttHost;
    /**
     * Mqtt端口
     */
    @NotNull(message = "Mqtt端口不能为空")
    private Integer mqttPort;

    @NotNull(message = "Mqtt客户端id不能为空")
    private String clientId;

    @NotNull(message = "Mqtt客户端密码不能为空")
    private String clientPwd;

    /**
     * Mqtt订阅主题
     */
    @Valid
    private List<Topic> topics;

    @Setter
    @Getter
    @ToString
    public static class Topic {
        /**
         * 主题名称
         * 支持设备信息变量，如: {id}即设备id
         */
        @NotBlank(message = "队列名称不能为空")
        private String name;

        /**
         * 主题qos
         */
        @NotNull(message = "队列qos不能为空")
        private MqttQos qos = MqttQos.EXACTLY_ONCE;
    }
}
