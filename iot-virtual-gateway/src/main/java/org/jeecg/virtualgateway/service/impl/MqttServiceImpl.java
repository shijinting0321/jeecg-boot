package org.jeecg.virtualgateway.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.jeecg.virtualgateway.config.SoftGatewayProperties;
import org.jeecg.virtualgateway.entity.DeviceData;
import org.jeecg.virtualgateway.entity.Gateway;
import org.jeecg.virtualgateway.service.IAsyncService;
import org.jeecg.virtualgateway.service.IMqttService;
import org.jeecg.virtualgateway.service.ISoftService;
import org.jeecg.virtualgateway.util.DeviceStatusEnum;
import org.jeecg.virtualgateway.util.MqttCodeEnum;
import org.jeecg.virtualgateway.vo.MqttDataVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.reactor.Mqtt5ReactorClient;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Slf4j
@Service
public class MqttServiceImpl implements IMqttService {
    /**
     * Mqtt5客户端
     */
    private Mqtt5ReactorClient client;

    /**
     * 初始化标志
     */
    private boolean init = false;

    @Autowired
    private SoftGatewayProperties properties;
    @Autowired
    private ISoftService softService;
    @Autowired
    private IAsyncService asyncService;

    @Override
    public void initClient() {
        Assert.isFalse(init, "请勿重复初始化Mqtt客户端");
        init = true;

        Mqtt5Client client = Mqtt5Client.builder()
                .serverHost(properties.getMqttHost())
                .serverPort(properties.getMqttPort())
                .identifier(properties.getClientId())

                .simpleAuth()
                .password(properties.getClientPwd().getBytes())
                .applySimpleAuth()

                .automaticReconnect()
                .initialDelay(3, TimeUnit.SECONDS)
                .maxDelay(3, TimeUnit.SECONDS)
                .applyAutomaticReconnect()

                .willPublish(publishWillStatus(properties.getClientId(), DeviceStatusEnum.OFFLINE))

                .addConnectedListener(context -> connectSuccess(context, properties.getClientId()))
                .addDisconnectedListener(this::connectError)

                .build();
        this.client = Mqtt5ReactorClient.from(client);

        Mono<?> mono = this.client.connectWith()
                .keepAlive(30)
                .applyConnect();
        List<SoftGatewayProperties.Topic> topics = properties.getTopics();
        if (CollectionUtil.isNotEmpty(topics)) {
            // 订阅主题
            List<Mqtt5Subscription> subscriptions = topics
                    .stream()
                    .map(topic -> {
                        String name = StrUtil.format(topic.getName(), BeanUtil.beanToMap(properties, false, true));
                        MqttQos qos = topic.getQos();

                        return Mqtt5Subscription.builder()
                                .topicFilter(name)
                                .qos(qos)
                                .build();
                    })
                    .collect(Collectors.toList());
            Mono<Mqtt5SubAck> subscribe = this.client.subscribeWith()
                    .addSubscriptions(subscriptions)
                    .applySubscribe()
                    .doOnSuccess(this::subscribeSuccess)
                    .doOnError(this::subscribeError);
            this.client.publishes(MqttGlobalPublishFilter.ALL)
                    .doOnNext(this::scribe)
                    .subscribe();
            mono = mono.then(subscribe);
        }

        mono.subscribe();
    }

    @Override
    public void publishStatus(String deviceId, DeviceStatusEnum status) {
        Map<String, String> data = MapUtil.of("status", status.getCode());

        publish("device/{id}&&" + deviceId + "/status", MqttQos.EXACTLY_ONCE, true, data);
    }

    @Override
    public void publishStatus(Map<String, DeviceStatusEnum> status) {
        Stream<Mqtt5Publish> stream = Optional.ofNullable(status)
                .orElseGet(MapUtil::empty)
                .entrySet()
                .stream()
                .map(en -> {
                    Map<String, String> data = MapUtil.of("status", en.getValue().getCode());
                    return publish0("device/{id}&&" + en.getKey() + "/status", MqttQos.EXACTLY_ONCE, true, data);
                });
        this.client.publish(Flux.fromStream(stream))
                .doOnNext(this::publishResult)
                .subscribe();
    }

    @Override
    public void publishData(List<MqttDataVO> data) {
        if (CollectionUtil.isEmpty(data)) {
            return;
        }

        Function<MqttDataVO, Object> valueMapper = vo -> vo.getData()
                .stream()
                .collect(Collectors.groupingBy(DeviceData::getCode,
                        Collectors.mapping(v -> v.getValue() + "&&" + v.getTimestamp(), Collectors.toList())));

        List<Object> maps = new ArrayList<>(data.size() / 10 + 1);
        long millis = System.currentTimeMillis();
        for (int i = 0; i < data.size(); i += 10) {
            Map<String, Object> map = ListUtil.sub(data, i, i + 10)
                    .stream()
                    .collect(Collectors.toMap(MqttDataVO::getDeviceId, valueMapper, (d, d2) -> d2));
            map.put("timestamp", millis);
            maps.add(map);
        }

        publish("device/{id}/data", MqttQos.AT_MOST_ONCE, false, maps);
    }

    @Override
    public void publishBaseControlResult(String messageId, MqttCodeEnum code) {
        Map<String, Object> map = MapUtil.of("messageId", messageId);
        map.put("code", code.getCode());

        publish("device/{id}/baseControlReply", MqttQos.EXACTLY_ONCE, false, map);
    }

    @Override
    public void publishControlResult(String messageId, MqttCodeEnum code) {
        Map<String, Object> map = MapUtil.of("messageId", messageId);
        map.put("code", code.getCode());

        publish("device/{id}/controlReply", MqttQos.EXACTLY_ONCE, false, map);
    }

    @Override
    public void publish(String topic, MqttQos qos, boolean retain, Object data) {
        Publisher<Mqtt5Publish> publisher;
        if (data instanceof List) {
            publisher = Flux.fromStream(((List<?>) data).stream()
                    .map(d -> publish0(topic, qos, retain, d)));
        } else {
            publisher = Mono.just(publish0(topic, qos, retain, data));
        }

        this.client.publish(publisher)
                .doOnNext(this::publishResult)
                .subscribe();
    }

    private Mqtt5Publish publishWillStatus(String deviceId, DeviceStatusEnum status) {
        Map<String, String> data = MapUtil.of("status", status.getCode());

        return publish0("device/{id}&&" + deviceId + "/status", MqttQos.EXACTLY_ONCE, true, data)
                .asWill();
    }

    private Mqtt5Publish publish0(String topic, MqttQos qos, boolean retain, Object data) {
        Gateway info = softService.getInfo();
        Assert.notNull(info, "未找到设备信息");

        Map<String, Object> param = BeanUtil.beanToMap(info, false, true);
        String name = StrUtil.format(topic, param);
        byte[] payload = StrUtil.bytes(JSON.toJSONString(data), StandardCharsets.UTF_8);

        return Mqtt5Publish.builder()
                .topic(name)
                .qos(qos)
                .retain(retain)
                .payload(payload)
                .build();
    }

    private void scribe(Mqtt5Publish publish) {
        MqttTopic topic = publish.getTopic();
        String payload = StrUtil.str(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);

        JSONObject obj = JSON.parseObject(payload);
        String messageId = obj.getString("messageId");
        if (MqttTopicFilter.of("device/+/baseControl").matches(topic)) {
            if (log.isInfoEnabled()) {
                log.info("基础控制消息: {}", payload);
            }

            String code = obj.getString("code");
            switch (code) {
                case "2":
                    asyncService.reboot(messageId, obj.getObject("ids", new TypeReference<List<String>>() {
                    }));
                    break;
                case "3":
                    asyncService.syncDevice(messageId);
                    break;
                default:
                    if (log.isWarnEnabled()) {
                        log.warn("无效的基础控制指令: {}", code);
                    }

                    publishBaseControlResult(messageId, MqttCodeEnum.SUCCESS);
            }
        } else if (MqttTopicFilter.of("device/+/attribute").matches(topic)) {
            if (log.isInfoEnabled()) {
                log.info("运行属性配置消息: {}", payload);
            }
        } else if (MqttTopicFilter.of("device/+/attributeReportReply").matches(topic)) {
            if (log.isInfoEnabled()) {
                log.info("运行属性配置上报响应消息: {}", payload);
            }
        } else if (MqttTopicFilter.of("device/+/control").matches(topic)) {
            if (log.isInfoEnabled()) {
                log.info("控制指令消息: {}", payload);
            }

            long timestamp = obj.getLongValue("timestamp");

            obj.keySet()
                    .stream()
                    .filter(key -> !StrUtil.equals("messageId", key))
                    .filter(key -> !StrUtil.equals("timestamp", key))
                    .findFirst()
                    .ifPresent(deviceId -> {
                        JSONObject attr = obj.getJSONObject(deviceId);
                        attr.keySet()
                                .stream()
                                .findFirst()
                                .ifPresent(code -> {
                                    JSONObject value = attr.getJSONObject(code);
                                    asyncService.operate(messageId, deviceId, code, value.getString("v"), timestamp);
                                });
                    });
        } else if (MqttTopicFilter.of("device/+/releaseControlLock").

                matches(topic)) {
            if (log.isInfoEnabled()) {
                log.info("解除控制锁定消息: {}", payload);
            }
        } else if (MqttTopicFilter.of("device/+/timeSyncReply").

                matches(topic)) {
            if (log.isInfoEnabled()) {
                log.info("时间同步消息: {}", payload);
            }
        } else if (MqttTopicFilter.of("device/+/tcaRuleIssue").

                matches(topic)) {
            if (log.isInfoEnabled()) {
                log.info("规则下发消息: {}", payload);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("其他消息: {}", payload);
            }
        }

    }

    private void publishResult(Mqtt5PublishResult result) {
        result.getError()
                .ifPresent(throwable -> {
                    throw new JeecgBootException("发送失败", throwable);
                });

        Mqtt5Publish publish = result.getPublish();
        String message = publish.getPayload()
                .map(buffer -> StrUtil.str(buffer, StandardCharsets.UTF_8))
                .orElse("none");
        if (log.isInfoEnabled()) {
            log.info("发送消息 - Topic[{}]: {} {}", publish.getTopic(), System.lineSeparator(), message);
        }
    }

    private void subscribeError(Throwable throwable) {
        if (log.isErrorEnabled()) {
            log.error("订阅失败", throwable);
        }
    }

    private void subscribeSuccess(Mqtt5SubAck ack) {
        if (log.isInfoEnabled()) {
            log.info("订阅成功: {}", ack.getReasonCodes());
        }
    }

    private void connectError(MqttClientDisconnectedContext context) {
        if (log.isWarnEnabled()) {
            log.warn("Mqtt客户端连接失败", context.getCause());
        }
    }

    private void connectSuccess(MqttClientConnectedContext context, String id) {
        if (log.isInfoEnabled()) {
            log.info("Mqtt客户端连接成功: {}", context.getClientConfig().getClientIdentifier().orElse(null));
        }

        publishStatus(id, DeviceStatusEnum.ONLINE);

        asyncService.syncDevice();
        asyncService.syncDeviceStatus();
        asyncService.listDeviceData();
    }

    private byte[] signature(Gateway info, long timestamp) {
        String s_timestamp = "timestamp" + timestamp;
        String deviceKey = info.getDeviceKey();
        String secret = info.getDeviceSecret();

        Map<String, String> params = new HashMap<>(3);
        params.put("clientId", info.getId());
        params.put("timestamp", s_timestamp);
        params.put("deviceKey", deviceKey);

        return SecureUtil.hmacSha1(Base64Decoder.decode(secret))
                .digestHex(MapUtil.sortJoin(params, StrUtil.EMPTY, StrUtil.EMPTY, true))
                .getBytes(StandardCharsets.UTF_8);
    }
}
