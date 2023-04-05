package org.jeecg.virtualgateway.service.impl;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.jeecg.virtualgateway.config.SoftGatewayProperties;
import org.jeecg.virtualgateway.entity.Gateway;
import org.jeecg.virtualgateway.entity.Token;
import org.jeecg.virtualgateway.service.IRedisService;
import org.jeecg.virtualgateway.service.ISoftService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Slf4j
@Service
public class SoftServiceImpl implements ISoftService {
    @Autowired
    private SoftGatewayProperties properties;
    @Autowired
    private IRedisService redisService;

    @Override
    public String initInfo() {
        String serial = properties.getCode();
        String version = properties.getVersion();

        Map<String, Object> data = MapUtil.of("sn", serial);
        data.put("version", version);

        String url = properties.getDomain() + "/cec-saas-ac-platform/device/linkDeviceApi/getAuthenticationInfo";
        HttpResponse response = HttpUtil.createPost(url)
                .contentType(ContentType.JSON.getValue())
                .body(JSON.toJSONString(MapUtil.of("data", data)))
                .execute();
        Assert.isTrue(response.isOk(), "设备初始化失败: {}", response.getStatus());

        Result<?> result = JSON.parseObject(response.body(), Result.class);
        if (result.getCode() != Result.OK().getCode()) {
            throw new JeecgBootException(result.getMessage());
        }

        String json = JSON.toJSONString(result.getResult());
        if (log.isDebugEnabled()) {
            log.debug("设备信息: {}", json);
        }

        redisService.saveInfo(serial, version, json);
        return json;
    }

    @Override
    public String initToken() {
        Gateway info = getInfo();
        Assert.notNull(info, "未找到设备信息");

        String deviceKey = info.getDeviceKey();
        String deviceSecret = info.getDeviceSecret();

        Map<String, Object> data = MapUtil.of("deviceKey", deviceKey);
        data.put("deviceSecret", deviceSecret);

        String url = properties.getDomain() + "/cec-saas-basic-account/device/access_token";
        HttpResponse response = HttpUtil.createPost(url)
                .contentType(ContentType.JSON.getValue())
                .body(JSON.toJSONString(MapUtil.of("data", data)))
                .execute();
        Assert.isTrue(response.isOk(), "设备授权失败: {}", response.getStatus());

        Result<?> result = JSON.parseObject(response.body(), Result.class);
        if (result.getCode() != Result.OK().getCode()) {
            throw new JeecgBootException(result.getMessage());
        }

        String json = JSON.toJSONString(result.getResult());
        if (log.isDebugEnabled()) {
            log.debug("授权token: {}", json);
        }

        Token token = JSON.parseObject(json, Token.class);
        Assert.notNull(token, "授权服务错误");

        int expire = token.getExpire() - 60;
        String accessToken = token.getAccessToken();
        redisService.saveToken(deviceKey, accessToken, expire > 0 ? expire : token.getExpire());
        return accessToken;
    }

    @Override
    public Gateway getInfo() {
        String serial = properties.getCode();
        String version = properties.getVersion();

        String info = redisService.getInfo(serial, version);
        if (StrUtil.isBlank(info)) {
            synchronized (this) {
                info = redisService.getInfo(serial, version);
                if (StrUtil.isBlank(info)) {
                    if (log.isInfoEnabled()) {
                        log.info("设备初始化: {}, v{}", properties.getCode(), properties.getVersion());
                    }

                    info = initInfo();
                }
            }
        }

        return JSON.parseObject(info, Gateway.class);
    }

    @Override
    public String getToken() {
        Gateway info = getInfo();
        Assert.notNull(info, "未找到设备信息");

        String token = redisService.getToken(info.getDeviceKey());
        if (StrUtil.isBlank(token)) {
            synchronized (this) {
                token = redisService.getToken(info.getDeviceKey());
                if (StrUtil.isBlank(token)) {
                    if (log.isInfoEnabled()) {
                        log.info("设备授权");
                    }

                    token = initToken();
                }
            }
        }

        return token;
    }
}
