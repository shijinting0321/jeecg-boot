package org.jeecg.virtualgateway.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.jeecg.virtualgateway.config.SoftGatewayProperties;
import org.jeecg.virtualgateway.entity.Device;
import org.jeecg.virtualgateway.entity.DeviceData;
import org.jeecg.virtualgateway.entity.Gateway;
import org.jeecg.virtualgateway.mapper.DeviceMapper;
import org.jeecg.virtualgateway.plugin.handler.IPluginHandler;
import org.jeecg.virtualgateway.plugin.listener.SoftPluginListener;
import org.jeecg.virtualgateway.util.DeviceStatusEnum;
import org.jeecg.virtualgateway.util.SyncCodeEnum;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.integration.user.PluginUser;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.virtualgateway.service.*;
import org.jeecg.virtualgateway.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jtcl
 * @date 2022/6/13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    private IDeviceService deviceService;

    @Autowired
    private SoftGatewayProperties properties;
    @Autowired
    private ISoftService softService;
    @Autowired
    private IMqttService mqttService;
    @Autowired
    private IRedisService redisService;
    @Autowired
    private IDeviceDataService deviceDataService;
    @Autowired
    private IAsyncService asyncService;

    @Autowired
    private PluginUser pluginUser;
    @Autowired
    private SoftPluginListener pluginListener;

    @Override
    public void saveDevice(DeviceVO vo) {
        long count = count(new LambdaQueryWrapper<Device>()
                .eq(Device::getSerial, vo.getSerial()));
        if (count > 0) {
            throw new JeecgBootException("设备序列号已存在");
        }

        String parentId = vo.getParentId();
        if (StrUtil.isNotBlank(parentId)) {
            Device device = Optional.ofNullable(getById(parentId))
                    .orElseThrow(() -> new JeecgBootException("上级设备不存在"));
            vo.setParentDeviceId(device.getDeviceId());
        }

        Device device = BeanUtil.copyProperties(vo, Device.class);
        boolean save = save(device);

        Assert.isTrue(save, "设备保存失败");
        vo.setId(device.getId());
    }

    @Override
    public void updateDeviceById(DeviceVO vo) {
        Device device = Optional.ofNullable(getById(vo.getId()))
                .orElseThrow(() -> new JeecgBootException("设备不存在"));

        String parentId = vo.getParentId();
        if (!StrUtil.equals(device.getParentId(), parentId) && StrUtil.isNotBlank(parentId)) {
            device = Optional.ofNullable(getById(parentId))
                    .orElseThrow(() -> new JeecgBootException("上级设备不存在"));
            vo.setParentDeviceId(device.getDeviceId());
        }

        device = BeanUtil.copyProperties(vo, Device.class);
        // 不能修改序列号
        device.setSerial(null);
        boolean update = updateById(device);

        Assert.isTrue(update, "设备更新失败");
    }

    @Override
    @Transactional(readOnly = true)
    public void syncDevice() {
        pluginUser.getBeanByInterface(IPluginHandler.class, false)
                .getPluginBean()
                .forEach((pluginId, handlers) -> handlers.forEach(handler -> asyncService.syncDevice(pluginId, handler)));
    }

    @Override
    @Transactional(readOnly = true)
    public void syncDevice(String pluginId, IPluginHandler handler) {
        Gateway info = softService.getInfo();
        Assert.notNull(info, "未找到设备信息");

        IDeviceService service = getDeviceService();
        Optional.ofNullable(service.applySync(pluginId, handler, info))
                .ifPresent(vo -> service.pushSyncResult(info, vo));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DeviceSyncVO applySync(String pluginId, IPluginHandler handler, Gateway info) {
        pluginId = pluginId + ":" + handler.name();

        List<SubDeviceVO> vos;
        try {
            vos = handler.listDevice();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("驱动[{}]设备列表查询错误: {}", pluginId, e.getMessage(), e);
            }

            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("驱动[{}], 设备列表: {}", pluginId, JSON.toJSONString(vos));
        }

        Map<String, Device> deviceMap = list(new LambdaQueryWrapper<Device>().eq(Device::getPluginId, pluginId))
                .stream()
                .collect(Collectors.toMap(Device::getSerial, Function.identity(), (d1, d2) -> d2));

        List<DeviceVO> devices = saveOrUpdateBatchDevice(vos, deviceMap, null, pluginId);

        // 添加设备
        List<PlatformDeviceVO> add = devices
                .stream()
                .filter(device -> "add".equals(device.getOperate()))
                .map(device -> toPlatform(device, info.getId()))
                .collect(Collectors.toList());
        // 更新设备
        List<PlatformDeviceVO> update = devices
                .stream()
                .filter(device -> "update".equals(device.getOperate()))
                .map(device -> toPlatform(device, info.getId()))
                .collect(Collectors.toList());
        // 删除设备
        Collection<Device> delete = deviceMap.values();

        Map<String, Object> data = MapUtil.of("id", info.getId());
        data.put("messageId", IdUtil.fastSimpleUUID());
        data.put("add", add);
        data.put("update", update);
        data.put("delete", delete
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList()));

        if (log.isDebugEnabled()) {
            log.debug("设备同步参数: {}", JSON.toJSONString(data));
        }

        String url = properties.getDomain() +
                "/cec-saas-ac-platform/device/linkDeviceApi/deviceSync?accessToken=" +
                softService.getToken();
        HttpResponse response = HttpUtil.createPost(url)
                .contentType(ContentType.JSON.getValue())
                .body(JSON.toJSONString(MapUtil.of("data", data)))
                .execute();
        Assert.isTrue(response.isOk(), "设备同步失败: {}", response.getStatus());

        Result<?> result = JSON.parseObject(response.body(), Result.class);
        if (result.getCode() != Result.OK().getCode()) {
            throw new JeecgBootException(result.getMessage());
        }

        String json = JSON.toJSONString(result.getResult());
        if (log.isDebugEnabled()) {
            log.debug("设备同步结果: {}", json);
        }

        DeviceSyncVO sync = JSON.parseObject(json, DeviceSyncVO.class);
        Assert.notNull(sync, "设备同步服务错误");

        // 主动同步设备结果处理
        Map<String, SyncResultVO> idMap = Optional.ofNullable(sync.getSyncResult())
                .map(rs -> rs
                        .stream()
                        .collect(Collectors.toMap(SyncResultVO::getDeviceId, Function.identity(), (r1, r2) -> r2))
                )
                .orElseGet(MapUtil::empty);
        Map<String, SyncResultVO> deviceIdMap = Optional.ofNullable(sync.getSyncResult())
                .map(rs -> rs
                        .stream()
                        .collect(Collectors.toMap(SyncResultVO::getId, Function.identity(), (r1, r2) -> r2))
                )
                .orElseGet(MapUtil::empty);
        // 添加设备,添加成功则保存平台id,否则删除并记录code
        List<Device> ds = add
                .stream()
                .map(vo -> {
                    String id = vo.getDeviceId();
                    SyncResultVO sr = idMap.remove(id);

                    Device device = new Device();
                    device.setId(id);
                    device.setParentId(vo.getDeviceParentId());
                    if (Objects.isNull(sr)) {
                        device.setDeleted(true);
                        device.setSyncResult("无");
                    } else if (sr.isOk()) {
                        device.setDeviceId(sr.getId());
                        device.setSyncResult(SyncCodeEnum.SUCCESS.getCode());
                    } else {
                        device.setDeleted(true);
                        device.setSyncResult(sr.getCode());
                    }
                    return device;
                })
                .collect(Collectors.toList());
        // 更新设备,更新成功则更新设备,否则记录code
        ds.addAll(update
                .stream()
                .map(vo -> {
                    String id = vo.getId();
                    SyncResultVO sr = deviceIdMap.remove(id);

                    Device device = new Device();
                    device.setId(vo.getDeviceId());
                    device.setParentId(vo.getDeviceParentId());
                    if (Objects.isNull(sr)) {
                        device.setSyncResult("无");
                    } else if (sr.isOk()) {
                        device.setSerial(vo.getSn());
                        device.setBrandModelId(vo.getBrandModelId());
                        device.setDeviceId(id);
                        device.setLinkAttr(vo.getLinkAttr());
                        device.setName(vo.getDeviceName());
                        device.setParentId(vo.getDeviceParentId());
                        device.setSyncResult(SyncCodeEnum.SUCCESS.getCode());
                    } else {
                        device.setSyncResult(sr.getCode());
                    }
                    return device;
                })
                .collect(Collectors.toList()));
        // 删除设备,删除成功则删除设备,否则记录code
        ds.addAll(delete
                .stream()
                .map(vo -> {
                    String id = vo.getId();
                    SyncResultVO sr = deviceIdMap.remove(vo.getDeviceId());

                    Device device = new Device();
                    device.setId(id);
                    device.setParentId(vo.getParentId());
                    if (Objects.isNull(sr)) {
                        device.setSyncResult("无");
                    } else if (sr.isOk()) {
                        device.setDeleted(true);
                        device.setSyncResult(SyncCodeEnum.SUCCESS.getCode());
                    } else {
                        device.setSyncResult(sr.getCode());
                    }
                    return device;
                })
                .collect(Collectors.toList()));

        if (!ds.isEmpty()) {
            Assert.isTrue(updateBatchById(ds), "同步结果更新失败");

            List<String> ids = ds.stream()
                    .filter(d -> Objects.nonNull(d.getDeleted()) && d.getDeleted())
                    .map(Device::getId)
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                Assert.isTrue(removeByIds(ids), "同步结果更新失败");
            }
        }

        return sync;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pushSyncResult(Gateway info, DeviceSyncVO vo) {
        String id = info.getId();
        List<SyncResultVO> sr = new ArrayList<>(64);
        // 添加
        sr.addAll(batchSaveDevice(handleParentId(id, vo.getAdd())));
        // 更新
        sr.addAll(batchUpdateDevice(handleParentId(id, vo.getUpdate())));
        // 删除
        List<SyncResultVO> deletes = batchRemoveDeviceByDeviceIds(vo.getDelete());
        List<String> deviceIds = deletes.stream()
                .filter(SyncResultVO::isOk)
                .map(SyncResultVO::getId)
                .collect(Collectors.toList());
        redisService.removeDeviceStatus(info.getDeviceKey(), deviceIds);
        redisService.removeDeviceData(info.getDeviceKey(), deviceIds);
        sr.addAll(deletes);

        if (sr.isEmpty()) {
            // 无被动同步设备,不推送结果
            return;
        }

        Map<String, Object> data = MapUtil.of("id", id);
        data.put("messageId", Optional.ofNullable(vo.getMessageId()).orElse(IdUtil.simpleUUID()));
        data.put("success", true);
        data.put("syncResult", sr
                .stream()
                .collect(Collectors.toMap(SyncResultVO::getId, SyncResultVO::getCode, (c1, c2) -> c2)));

        if (log.isDebugEnabled()) {
            log.debug("设备同步结果反馈参数: {}", JSON.toJSONString(data));
        }

        String url = properties.getDomain() +
                "/cec-saas-ac-platform/device/linkDeviceApi/deviceSyncResult?accessToken=" +
                softService.getToken();
        HttpResponse response = HttpUtil.createPost(url)
                .contentType(ContentType.JSON.getValue())
                .body(JSON.toJSONString(MapUtil.of("data", data)))
                .execute();
        Assert.isTrue(response.isOk(), "设备同步结果反馈失败: {}", response.getStatus());

        Result<?> result = JSON.parseObject(response.body(), Result.class);
        if (result.getCode() != Result.OK().getCode()) {
            throw new JeecgBootException(result.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void syncDeviceStatus(boolean clearCache) {
        if (clearCache) {
            Gateway info = softService.getInfo();
            Assert.notNull(info, "未找到设备信息");

            redisService.removeDeviceStatus(info.getDeviceKey());
        }

        pluginUser.getBeanByInterface(IPluginHandler.class, false)
                .getPluginBean()
                .forEach((pluginId, handlers) -> handlers.forEach(handler -> asyncService.syncDeviceStatus(pluginId, handler)));
    }

    @Override
    @Transactional(readOnly = true)
    public void syncDeviceStatus(String pluginId, IPluginHandler handler) {
        pluginId = pluginId + ":" + handler.name();

        List<SubDeviceStatusVO> vos;
        try {
            vos = handler.listDeviceStatus();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("驱动[{}]设备状态查询错误: {}", pluginId, e.getMessage(), e);
            }

            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("驱动[{}], 设备状态: {}", pluginId, JSON.toJSONString(vos));
        }

        Gateway info = softService.getInfo();
        Assert.notNull(info, "未找到设备信息");

        String deviceKey = info.getDeviceKey();
        Map<String, String> deviceIdMap = list(new LambdaQueryWrapper<Device>()
                .select(Device::getSerial, Device::getDeviceId)
                .eq(Device::getPluginId, pluginId))
                .stream()
                .collect(Collectors.toMap(Device::getSerial, Device::getDeviceId, (d1, d2) -> d2));

        Map<Object, Object> map = Optional.ofNullable(redisService.getDeviceStatus(deviceKey))
                .orElseGet(MapUtil::empty);
        Optional.ofNullable(vos)
                .map(status -> {
                    Map<String, DeviceStatusEnum> ss = MapUtil.newHashMap(status.size());
                    status.forEach(s -> Optional.ofNullable(deviceIdMap.get(s.getSerial()))
                            .ifPresent(deviceId -> {
                                DeviceStatusEnum se = s.getStatus();
                                if (!StrUtil.equals(se.getCode(), (String) map.get(deviceId))) {
                                    ss.put(deviceId, se);
                                }
                            }));
                    mqttService.publishStatus(ss);
                    return ss.entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, en -> en.getValue().getCode()));
                })
                .ifPresent(status -> redisService.saveDeviceStatus(deviceKey, status));
    }

    @Override
    @Transactional(readOnly = true)
    public void listDeviceData(boolean clearCache) {
        if (clearCache) {
            Gateway info = softService.getInfo();
            Assert.notNull(info, "未找到设备信息");

            redisService.removeDeviceData(info.getDeviceKey());
        }

        pluginUser.getBeanByInterface(IPluginHandler.class, false)
                .getPluginBean()
                .forEach((pluginId, handlers) -> handlers.forEach(handler -> asyncService.listDeviceData(pluginId, handler)));
    }

    @Override
    public void listDeviceData(String pluginId, IPluginHandler handler) {
        Gateway info = softService.getInfo();
        Assert.notNull(info, "未找到设备信息");

        String deviceKey = info.getDeviceKey();
        pluginId = pluginId + ":" + handler.name();

        List<SubDeviceDataVO> vos;
        try {
            vos = handler.listDeviceData();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("驱动[{}]采集数据错误: {}", pluginId, e.getMessage(), e);
            }

            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("驱动[{}], 采集数据: {}", pluginId, JSON.toJSONString(vos));
        }

        Map<String, String> deviceIdMap = list(new LambdaQueryWrapper<Device>()
                .select(Device::getSerial, Device::getDeviceId)
                .eq(Device::getPluginId, pluginId))
                .stream()
                .collect(Collectors.toMap(Device::getSerial, Device::getDeviceId, (d1, d2) -> d2));

        Map<Object, Object> map = Optional.ofNullable(redisService.getDeviceData(deviceKey))
                .orElseGet(MapUtil::empty);

        // 过滤数据
        List<DeviceData> data = Optional.ofNullable(vos)
                .orElseGet(ListUtil::empty)
                .stream()
                .flatMap(d -> Optional.ofNullable(deviceIdMap.get(d.getSerial()))
                        .map(deviceId -> d.getData()
                                .entrySet()
                                .stream()
                                .map(en -> {
                                    String code = en.getKey();
                                    SubDeviceDataVO.AttrValue value = en.getValue();
                                    return new DeviceData()
                                            .setDeviceId(deviceId)
                                            .setCode(code)
                                            .setValue(value.getValue())
                                            .setTimestamp(value.getTimestamp());
                                })
                        )
                        .orElse(null)
                )
                .filter(d -> !StrUtil.equals(d.getValue(), (String) map.get(d.getDeviceId() + ":" + d.getCode())))
                .collect(Collectors.toList());

        if (data.isEmpty()) {
            return;
        }

        // 保存数据
        try {
            deviceDataService.saveBatch(data);
            Map<String, String> cache = data.stream()
                    .collect(Collectors.toMap(d -> d.getDeviceId() + ":" + d.getCode(), DeviceData::getValue, (v1, v2) -> v2));
            redisService.saveDeviceData(deviceKey, cache);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("驱动[{}]保存数据错误: {}", pluginId, e.getMessage(), e);
            }
        }
    }

    @Override
    public void syncDeviceData() {
        // 查询设备数据
        List<DeviceData> data = deviceDataService.listData(5000);

        if (log.isDebugEnabled()) {
            log.debug("上传数据: {}", JSON.toJSONString(data));
        }

        if (data.isEmpty()) {
            return;
        }

        List<MqttDataVO> mqttData = data.stream()
                .collect(Collectors.groupingBy(DeviceData::getDeviceId))
                .entrySet()
                .stream()
                .map(en -> new MqttDataVO()
                        .setDeviceId(en.getKey())
                        .setData(en.getValue())
                )
                .collect(Collectors.toList());

        try {
            // TODO: 2023/4/8 修改上报数据格式 
            // 推送设备数据
            mqttService.publishData(mqttData);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("上传数据错误: {}", e.getMessage(), e);
            }
        }

        // 删除数据
        List<String> ids = mqttData.stream()
                .flatMap(device -> device.getData()
                        .stream()
                        .map(DeviceData::getId)
                )
                .collect(Collectors.toList());
        Assert.isTrue(deviceDataService.removeByIds(ids), "设备数据删除失败");
    }

    @Override
    @Transactional(readOnly = true)
    public void operateDevice(String deviceId, String code, String value) {
        Device device = Optional.ofNullable(getOne(new LambdaQueryWrapper<Device>()
                        .eq(Device::getDeviceId, deviceId)))
                .orElseThrow(() -> new JeecgBootException("设备不存在: " + deviceId));

        SoftPluginListener.Plugin plugin = Optional.ofNullable(pluginListener.getByBrandModeId(device.getBrandModelId()))
                .orElseThrow(() -> new JeecgBootException("不支持的品牌类型设备"));
        boolean operate = plugin.getHandler().operate(device.getSerial(), code, value);

        Assert.isTrue(operate, "设备控制失败: {},{},{},{}", device.getName(), device.getSerial(), code, value);
    }

    @Override
    @Transactional(readOnly = true)
    public void rebootDevice(String deviceId) {
        Device device = Optional.ofNullable(getOne(new LambdaQueryWrapper<Device>()
                        .eq(Device::getDeviceId, deviceId)))
                .orElseThrow(() -> new JeecgBootException("设备不存在: " + deviceId));

        SoftPluginListener.Plugin plugin = Optional.ofNullable(pluginListener.getByBrandModeId(device.getBrandModelId()))
                .orElseThrow(() -> new JeecgBootException("不支持的品牌类型设备"));
        boolean operate = plugin.getHandler().reboot(ListUtil.toList(device.getSerial()));

        Assert.isTrue(operate, "设备重启失败: {},{}", device.getName(), device.getSerial());
    }

    private List<SyncResultVO> batchRemoveDeviceByDeviceIds(List<String> deviceIds) {
        if (CollectionUtil.isEmpty(deviceIds)) {
            return ListUtil.empty();
        }

        SoftPluginListener.Plugin none = new SoftPluginListener.Plugin();
        List<SyncResultVO> result = list(new LambdaQueryWrapper<Device>()
                .in(Device::getDeviceId, deviceIds))
                .stream()
                .collect(Collectors.groupingBy(device -> {
                    SoftPluginListener.Plugin plugin = pluginListener.getByBrandModeId(device.getBrandModelId());
                    return Optional.ofNullable(plugin).orElse(none);
                }, Collectors.toList()))
                .entrySet()
                .stream()
                .flatMap(en -> {
                    SoftPluginListener.Plugin plugin = en.getKey();
                    List<Device> devices = en.getValue();

                    String code;
                    if (Objects.equals(none, plugin)) {
                        code = SyncCodeEnum.BRAND_MODEL_NOT_EXIST.getCode();
                    } else {
                        SyncCodeEnum codeEnum = SyncCodeEnum.SUCCESS;
                        List<String> ids = devices.stream()
                                .map(Device::getId)
                                .collect(Collectors.toList());

                        long count = count(new LambdaQueryWrapper<Device>()
                                .in(Device::getParentId, ids)
                                .notIn(Device::getId, ids));
                        if (count > 0) {
                            // 存在子级
                            codeEnum = SyncCodeEnum.ERROR;
                        }

                        IPluginHandler handler = plugin.getHandler();
                        String pluginId = plugin.getPluginId() + ":" + handler.name();

                        List<String> serials = devices.stream()
                                .map(Device::getSerial)
                                .collect(Collectors.toList());

                        boolean remove;
                        try {
                            remove = handler.delete(serials);
                        } catch (Exception e) {
                            if (log.isErrorEnabled()) {
                                log.error("驱动[{}]删除设备错误: {}", pluginId, e.getMessage(), e);
                            }

                            remove = false;
                        }

                        if (remove) {
                            remove = removeByIds(ids);
                        }

                        code = remove ? codeEnum.getCode() : SyncCodeEnum.ERROR.getCode();
                    }

                    return devices.stream()
                            .map(device -> new SyncResultVO()
                                    .setId(device.getDeviceId())
                                    .setCode(code));
                })
                .collect(Collectors.toList());

        String code = SyncCodeEnum.SUCCESS.getCode();
        List<String> dids = result.stream()
                .map(SyncResultVO::getId)
                .collect(Collectors.toList());
        List<SyncResultVO> sr = deviceIds.stream()
                .filter(deviceId -> !dids.contains(deviceId))
                .map(deviceId -> new SyncResultVO()
                        .setId(deviceId)
                        .setCode(code))
                .collect(Collectors.toList());
        return CollectionUtil.addAllIfNotContains(result, sr);
    }

    private List<SyncResultVO> batchUpdateDevice(List<PlatformDeviceVO> vos) {
        return Optional.ofNullable(vos)
                .orElseGet(ListUtil::empty)
                .stream()
                .map(this::updateDeviceByDeviceId)
                .collect(Collectors.toList());
    }

    private SyncResultVO updateDeviceByDeviceId(PlatformDeviceVO vo) {
        String deviceId = vo.getId();
        SyncResultVO result = new SyncResultVO()
                .setId(deviceId)
                .setCode(SyncCodeEnum.SUCCESS.getCode());

        Gateway info = softService.getInfo();
        if (Objects.nonNull(info) && StrUtil.equals(info.getId(), deviceId)) {
            return result;
        }

        Device device = getOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceId, deviceId));
        if (Objects.isNull(device)) {
            result.setCode(SyncCodeEnum.INVALID_DEVICE_ERROR.getCode());
            return result;
        }
        String id = device.getId();

        String serial = vo.getSn();
        long count = StrUtil.equals(device.getSerial(), serial) ? 0 : count(new LambdaQueryWrapper<Device>()
                .eq(Device::getSerial, serial));
        if (count > 0) {
            result.setCode(SyncCodeEnum.DUPLICATE_SERIAL_ERROR.getCode());
            return result;
        }

        String parentSerial = null;
        String parentId = vo.getParentId();
        if (StrUtil.isNotBlank(parentId)) {
            device = getOne(new LambdaQueryWrapper<Device>()
                    .eq(Device::getDeviceId, parentId));
            if (Objects.isNull(device)) {
                result.setCode(SyncCodeEnum.DEVICE_EXCEPTION.getCode());
                return result;
            }
            parentSerial = device.getSerial();
            parentId = device.getId();
        }

        SoftPluginListener.Plugin plugin = pluginListener.getByBrandModeId(vo.getBrandModelId());
        if (Objects.isNull(plugin)) {
            result.setCode(SyncCodeEnum.BRAND_MODEL_NOT_EXIST.getCode());
            return result;
        }
        IPluginHandler handler = plugin.getHandler();
        String pluginId = plugin.getPluginId() + ":" + handler.name();

        boolean update;
        try {
            Map<String, String> attr = Optional.ofNullable(vo.getLinkAttr())
                    .orElseGet(ListUtil::empty)
                    .stream()
                    .collect(Collectors.toMap(obj -> obj.get("code"), obj -> obj.get("value"), (v1, v2) -> v2));
            SyncDeviceVO svo = new SyncDeviceVO()
                    .setSerial(serial)
                    .setParentSerial(parentSerial)
                    .setName(vo.getDeviceName())
                    .setBrandModelId(vo.getBrandModelId())
                    .setAttr(attr);
            update = handler.update(svo);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("驱动[{}]更新设备错误: {}", pluginId, e.getMessage(), e);
            }

            update = false;
        }

        if (update) {
            device = new Device();
            device.setId(id);
            device.setName(vo.getDeviceName());
            device.setSerial(serial);
            device.setParentId(parentId);
            device.setBrandModelId(vo.getBrandModelId());
            device.setLinkAttr(vo.getLinkAttr());
            device.setSyncResult(result.getCode());
            update = updateById(device);
        }

        return update ? result : result.setCode(SyncCodeEnum.ERROR.getCode());
    }

    private List<SyncResultVO> batchSaveDevice(List<PlatformDeviceVO> vos) {
        if (CollectionUtil.isEmpty(vos)) {
            return ListUtil.empty();
        }

        // 所有新增设备的ids
        List<String> dids = vos.stream()
                .map(PlatformDeviceVO::getId)
                .collect(Collectors.toList());

        // 先保存父级已经存在的
        List<SyncResultVO> sr = vos.stream()
                .filter(dvo -> !dids.contains(dvo.getParentId()))
                .map(this::saveDevice)
                .collect(Collectors.toList());

        // 保存父级不存在的
        sr.addAll(batchSaveDevice(vos.stream()
                .filter(dvo -> dids.contains(dvo.getParentId()))
                .collect(Collectors.toList())));

        return sr;
    }

    private SyncResultVO saveDevice(PlatformDeviceVO vo) {
        String deviceId = vo.getId();
        SyncResultVO result = new SyncResultVO()
                .setId(deviceId)
                .setCode(SyncCodeEnum.SUCCESS.getCode());

        String brandModelId = vo.getBrandModelId();
        SoftPluginListener.Plugin plugin = pluginListener.getByBrandModeId(brandModelId);
        if (Objects.isNull(plugin)) {
            result.setCode(SyncCodeEnum.BRAND_MODEL_NOT_EXIST.getCode());
            return result;
        }
        IPluginHandler handler = plugin.getHandler();
        String pluginId = plugin.getPluginId() + ":" + handler.name();

        long count = count(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceId, deviceId));
        if (count > 0) {
            result.setCode(SyncCodeEnum.DUPLICATE_DEVICE_ID_ERROR.getCode());
            return result;
        }

        String serial = vo.getSn();
        count = count(new LambdaQueryWrapper<Device>()
                .eq(Device::getSerial, serial));
        if (count > 0) {
            result.setCode(SyncCodeEnum.DUPLICATE_SERIAL_ERROR.getCode());
            return result;
        }

        String parentSerial = null;
        String parentId = vo.getParentId();
        if (StrUtil.isNotBlank(parentId)) {
            Device device = getOne(new LambdaQueryWrapper<Device>()
                    .eq(Device::getDeviceId, parentId));
            if (Objects.isNull(device)) {
                result.setCode(SyncCodeEnum.DEVICE_EXCEPTION.getCode());
                return result;
            }
            parentSerial = device.getSerial();
            parentId = device.getId();
        }

        boolean save;
        try {
            Map<String, String> attr = Optional.ofNullable(vo.getLinkAttr())
                    .orElseGet(ListUtil::empty)
                    .stream()
                    .collect(Collectors.toMap(obj -> obj.get("code"), obj -> obj.get("value"), (v1, v2) -> v2));
            SyncDeviceVO svo = new SyncDeviceVO()
                    .setSerial(serial)
                    .setParentSerial(parentSerial)
                    .setName(vo.getDeviceName())
                    .setBrandModelId(brandModelId)
                    .setAttr(attr);
            save = handler.add(svo);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("驱动[{}]添加设备错误: {}", pluginId, e.getMessage(), e);
            }

            save = false;
        }

        if (save) {
            Device device = new Device();
            device.setDeviceId(deviceId);
            device.setName(vo.getDeviceName());
            device.setSerial(serial);
            device.setParentId(parentId);
            device.setBrandModelId(brandModelId);
            device.setLinkAttr(vo.getLinkAttr());
            device.setSource("platform");
            device.setPluginId(pluginId);
            device.setSyncResult(result.getCode());
            save = save(device);
        }

        return save ? result : result.setCode(SyncCodeEnum.ERROR.getCode());
    }

    private List<PlatformDeviceVO> handleParentId(String parentId, List<PlatformDeviceVO> vos) {
        return Optional.ofNullable(vos)
                .orElseGet(ListUtil::empty)
                .stream()
                .peek(a -> {
                    if (StrUtil.equals(parentId, a.getParentId())) {
                        a.setParentId(null);
                    }
                })
                .collect(Collectors.toList());
    }

    private List<DeviceVO> saveOrUpdateBatchDevice(List<SubDeviceVO> vos, Map<String, Device> map, String parentId, String pluginId) {
        if (CollectionUtil.isEmpty(vos)) {
            return ListUtil.empty();
        }

        return vos.stream()
                .flatMap(dvo -> {
                    DeviceVO vo = new DeviceVO(dvo);
                    vo.setParentId(parentId);
                    vo.setPluginId(pluginId);

                    List<DeviceVO> devices = ListUtil.toList();
                    Device device = map.remove(vo.getSerial());
                    if (Objects.isNull(device)) {
                        devices.add(vo);
                        vo.setOperate("add");
                        vo.setSource("plugin");
                        saveDevice(vo);
                    } else if (isUpdate(device, vo)) {
                        devices.add(vo);
                        vo.setOperate("update");
                        vo.setId(device.getId());
                        vo.setDeviceId(device.getDeviceId());
                        vo.setParentDeviceId(StrUtil.isBlank(parentId) ? null : Optional.ofNullable(getById(parentId))
                                .map(Device::getDeviceId)
                                .orElse(null));
                    }

                    devices.addAll(saveOrUpdateBatchDevice(vo.getChildren(), map, vo.getId(), pluginId));
                    return devices.stream();
                })
                .collect(Collectors.toList());
    }

    private PlatformDeviceVO toPlatform(DeviceVO vo, String parentId) {
        String deviceParentId = vo.getParentId();
        if (StrUtil.isNotBlank(deviceParentId)) {
            parentId = vo.getParentDeviceId();
            if (StrUtil.isBlank(parentId)) {
                parentId = deviceParentId;
            }
        }

        PlatformDeviceVO device = new PlatformDeviceVO();
        device.setDeviceId(vo.getId());
        device.setId(vo.getDeviceId());
        device.setParentId(parentId);
        device.setDeviceParentId(deviceParentId);
        device.setSn(vo.getSerial());
        device.setDeviceName(vo.getName());
        device.setBrandModelId(vo.getBrandModelId());
        device.setLinkAttr(ListUtil.toList(vo.getLinkAttr()));
        return device;
    }

    private boolean isUpdate(Device device, DeviceVO vo) {
        // 序列号比较
        if (!StrUtil.equals(device.getSerial(), vo.getSerial())) {
            return true;
        }

        // 名称
        if (!StrUtil.equals(device.getName(), vo.getName())) {
            return true;
        }

        // 上级设备
        if (!StrUtil.equals(device.getParentId(), vo.getParentId())) {
            return true;
        }

        // 品牌
        if (!StrUtil.equals(device.getBrandModelId(), vo.getBrandModelId())) {
            return true;
        }

        String olds = JSON.toJSONString(device.getLinkAttr());
        String news = JSON.toJSONString(vo.getLinkAttr());
        return !StrUtil.equals(olds, news);
    }

    /**
     * 内部调用,事务传递设置不生效,需要获取bean调用
     */
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
}
