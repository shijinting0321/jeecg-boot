package org.jeecg.virtualgateway;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.cron.CronUtil;
import org.jeecg.virtualgateway.config.SoftGatewayProperties;
import org.jeecg.virtualgateway.plugin.listener.SoftPluginListener;
import org.jeecg.virtualgateway.service.IDeviceService;
import org.jeecg.virtualgateway.service.IMqttService;
import org.jeecg.virtualgateway.service.IRedisService;
import org.jeecg.virtualgateway.service.ISoftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author jtcl
 * @date 2022/6/7
 */
@Slf4j
public class SoftGatewayRunner implements ApplicationRunner {
    @Autowired
    public SoftGatewayProperties properties;
    @Autowired
    public ISoftService softService;
    @Autowired
    public IMqttService mqttService;
    @Autowired
    public IDeviceService deviceService;
    @Autowired
    public IRedisService redisService;
    @Autowired
    public SoftPluginListener pluginListener;

    static {
//        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
    }

    @Override
    public void run(ApplicationArguments args) {

        while (true) {
            try {
                initInfo();
                break;
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("初始化设备失败,稍后重试", e);
                }

                ThreadUtil.sleep(1, TimeUnit.MINUTES);
            }
        }
        initToken();
        initMqttClient();
        initTask();
    }


    private void initInfo() {
        if (log.isInfoEnabled()) {
            log.info("初始化设备: {}, v{}", properties.getCode(), properties.getVersion());
        }

        softService.initInfo();
    }

    private void initToken() {
        if (log.isInfoEnabled()) {
            log.info("初始化授权");
        }

        softService.initToken();
    }

    private void initMqttClient() {
        if (log.isInfoEnabled()) {
            log.info("初始化Mqtt客户端");
        }

        mqttService.initClient();
    }

    private void initTask() {
        if (log.isInfoEnabled()) {
            log.info("启动定时任务");
        }

        pluginListener.createSyncDeviceDataTask();
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
}
