package org.jeecg.virtualgateway.config;

import org.jeecg.virtualgateway.SoftGatewayRunner;
import org.jeecg.virtualgateway.plugin.listener.SoftPluginListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jtcl
 */
@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(SoftGatewayProperties.class)
public class SoftGatewayConfiguration {
    @Bean
    public SoftGatewayRunner softGatewayRunner() {
        return new SoftGatewayRunner();
    }

    @Bean
    public SoftPluginListener pluginListener() {
        return new SoftPluginListener();
    }

    @Bean
    public Executor asyncMqttExecutor() {
        if (log.isInfoEnabled()) {
            log.info("初始化Mqtt订阅消息异步处理线程池...");
        }

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(10);
        //配置最大线程数
        executor.setMaxPoolSize(10);
        //配置队列大小
        executor.setQueueCapacity(99999);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("Mqtt订阅消息处理-");

        // 设置拒绝策略：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        return executor;
    }
}
