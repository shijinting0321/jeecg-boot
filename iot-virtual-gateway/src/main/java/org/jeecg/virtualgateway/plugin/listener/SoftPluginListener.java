package org.jeecg.virtualgateway.plugin.listener;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.extra.spring.SpringUtil;
import org.jeecg.virtualgateway.config.SoftGatewayProperties;
import org.jeecg.virtualgateway.plugin.handler.IPluginHandler;
import org.jeecg.virtualgateway.service.IDeviceService;
import org.jeecg.virtualgateway.util.ConcurrentTask;
import com.gitee.starblues.core.PluginInfo;
import com.gitee.starblues.integration.listener.PluginListener;
import com.gitee.starblues.integration.user.PluginUser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jtcl
 * @date 2022/6/13
 */
public class SoftPluginListener implements PluginListener {
    private static final String DEVICE_COLLECT_TASK_PREFIX = "-device-collect-";
    private static final String DEVICE_STATUS_TASK_PREFIX = "-device-status-";
    private static final String LIST_DEVICE_DATA_TASK_PREFIX = "-list-device-data-";
    private static final String SYNC_DEVICE_DATA_TASK = "sync-device-data-task";

    /**
     * 缓存插件定时任务id
     */
    private final Map<String, List<String>> task_map = new ConcurrentHashMap<>(4);
    /**
     * 缓存插件支持品牌型号id
     */
    private final Map<String, Plugin> brand_plugin_map = new ConcurrentHashMap<>(4);

    private IDeviceService deviceService;

    @Autowired
    private PluginUser pluginUser;
    @Autowired
    private SoftGatewayProperties properties;

    @Override
    public void startSuccess(PluginInfo pluginInfo) {
        String pluginId = pluginInfo.getPluginId();
        List<String> taskIds = pluginUser.getBeanByInterface(pluginId, IPluginHandler.class)
                .stream()
                .flatMap(handler -> {
                    Plugin plugin = new Plugin()
                            .setPluginId(pluginId)
                            .setHandler(handler);
                    Optional.ofNullable(handler.supportBrandModelId())
                            .orElseGet(ListUtil::empty)
                            .forEach(id -> brand_plugin_map.put(id, plugin));

                    String deviceId = createSyncDeviceTask(pluginId, handler);
                    String statusId = createSyncDeviceStatusTask(pluginId, handler);
                    String listDataId = createListDeviceDataTask(pluginId, handler);

                    return Stream.of(deviceId, statusId, listDataId);
                })
                .collect(Collectors.toList());

        task_map.put(pluginId, taskIds);
        PluginListener.super.startSuccess(pluginInfo);
    }

    @Override
    public void stopSuccess(PluginInfo pluginInfo) {
        String pluginId = pluginInfo.getPluginId();

        brand_plugin_map.forEach((id, plugin) -> {
            if (StrUtil.equals(pluginId, plugin.getPluginId())) {
                brand_plugin_map.remove(id);
            }
        });

        List<String> taskIds = task_map.remove(pluginId);
        Optional.ofNullable(taskIds)
                .ifPresent(ids -> ids.forEach(CronUtil::remove));

        PluginListener.super.stopSuccess(pluginInfo);
    }

    public Plugin getByBrandModeId(String brandModeId) {
        return brand_plugin_map.getOrDefault(brandModeId, null);
    }

    public void executeSyncDeviceTask(String pluginId, IPluginHandler handler) {
        String deviceId = handler.name() + DEVICE_COLLECT_TASK_PREFIX + pluginId;
        Optional.ofNullable(CronUtil.getScheduler().getTask(deviceId))
                .ifPresent(Task::execute);
    }

    public void executeSyncDeviceStatusTask(String pluginId, IPluginHandler handler) {
        String statusId = handler.name() + DEVICE_STATUS_TASK_PREFIX + pluginId;
        Optional.ofNullable(CronUtil.getScheduler().getTask(statusId))
                .ifPresent(Task::execute);
    }

    public void executeListDeviceDataTask(String pluginId, IPluginHandler handler) {
        String dataId = handler.name() + LIST_DEVICE_DATA_TASK_PREFIX + pluginId;
        Optional.ofNullable(CronUtil.getScheduler().getTask(dataId))
                .ifPresent(Task::execute);
    }


    public String createSyncDeviceTask(String pluginId, IPluginHandler handler) {
        String deviceId = handler.name() + DEVICE_COLLECT_TASK_PREFIX + pluginId;
        String cron = properties.getCollectDeviceCron();

        return CronUtil.schedule(deviceId, cron, new ConcurrentTask() {
            @Override
            public void execute1() {
                getDeviceService().syncDevice(pluginId, handler);
            }
        });
    }

    public String createSyncDeviceStatusTask(String pluginId, IPluginHandler handler) {
        String statusId = handler.name() + DEVICE_STATUS_TASK_PREFIX + pluginId;
        String cron = handler.listDeviceStatusCron();

        return CronUtil.schedule(statusId, cron, new ConcurrentTask() {
            @Override
            public void execute1() {
                getDeviceService().syncDeviceStatus(pluginId, handler);
            }
        });
    }

    public String createListDeviceDataTask(String pluginId, IPluginHandler handler) {
        String dataId = handler.name() + LIST_DEVICE_DATA_TASK_PREFIX + pluginId;
        String cron = handler.listDeviceDataCron();

        return CronUtil.schedule(dataId, cron, new ConcurrentTask() {
            @Override
            public void execute1() {
                getDeviceService().listDeviceData(pluginId, handler);
            }
        });
    }

    public String createSyncDeviceDataTask() {
        String cron = properties.getUploadDataCron();

        return CronUtil.schedule(SYNC_DEVICE_DATA_TASK, cron, new ConcurrentTask() {
            @Override
            public void execute1() {
                getDeviceService().syncDeviceData();
            }
        });
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

    @Setter
    @Getter
    @ToString
    @Accessors(chain = true)
    public static class Plugin {
        private String pluginId;
        private IPluginHandler handler;
    }
}
