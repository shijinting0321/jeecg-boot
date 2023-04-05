package org.jeecg.virtualgateway.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import org.jeecg.virtualgateway.constant.RedisConstant;
import org.jeecg.virtualgateway.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Service
public class RedisServiceImpl implements IRedisService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveInfo(String serial, String version, String info) {
        String key = RedisConstant.INIT_INFO_CACHE_PRE + serial + "_" + version;
        stringRedisTemplate.opsForValue().set(key, info);
    }

    @Override
    public String getInfo(String serial, String version) {
        String key = RedisConstant.INIT_INFO_CACHE_PRE + serial + "_" + version;
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void saveToken(String deviceKey, String accessToken, int expire) {
        String key = RedisConstant.INIT_TOKEN_CACHE_PRE + deviceKey;
        stringRedisTemplate.opsForValue().set(key, accessToken, expire, TimeUnit.SECONDS);
    }

    @Override
    public String getToken(String deviceKey) {
        String key = RedisConstant.INIT_TOKEN_CACHE_PRE + deviceKey;
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void saveDeviceStatus(String deviceKey, Map<String, String> map) {
        String key = RedisConstant.DEVICE_STATUS_CACHE_PRE + deviceKey;
        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public Map<Object, Object> getDeviceStatus(String deviceKey) {
        String key = RedisConstant.DEVICE_STATUS_CACHE_PRE + deviceKey;
        return stringRedisTemplate.opsForHash().entries(key);
    }

    @Override
    public void removeDeviceStatus(String deviceKey) {
        String key = RedisConstant.DEVICE_STATUS_CACHE_PRE + deviceKey;
        stringRedisTemplate.delete(key);
    }

    @Override
    public void removeDeviceStatus(String deviceKey, List<String> deviceIds) {
        if (CollectionUtil.isEmpty(deviceIds)) {
            return;
        }

        String key = RedisConstant.DEVICE_STATUS_CACHE_PRE + deviceKey;
        stringRedisTemplate.opsForHash().delete(key, deviceIds.toArray());
    }

    @Override
    public void saveDeviceData(String deviceKey, Map<String, String> map) {
        String key = RedisConstant.DEVICE_DATA_CACHE_PRE + deviceKey;
        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public Map<Object, Object> getDeviceData(String deviceKey) {
        String key = RedisConstant.DEVICE_DATA_CACHE_PRE + deviceKey;
        return stringRedisTemplate.opsForHash().entries(key);
    }

    @Override
    public void removeDeviceData(String deviceKey) {
        String key = RedisConstant.DEVICE_DATA_CACHE_PRE + deviceKey;
        stringRedisTemplate.delete(key);
    }

    @Override
    public void removeDeviceData(String deviceKey, List<String> deviceIds) {
        if (CollectionUtil.isEmpty(deviceIds)) {
            return;
        }
        HashOperations<String, Object, Object> opt = stringRedisTemplate.opsForHash();
        String key = RedisConstant.DEVICE_DATA_CACHE_PRE + deviceKey;

        Object[] hashKeys = deviceIds.stream()
                .flatMap(deviceId -> {
                    Cursor<Map.Entry<Object, Object>> cursor = opt.scan(key, ScanOptions.scanOptions()
                            .match(deviceId + ":*")
                            .build());
                    return cursor.stream()
                            .map(Map.Entry::getKey);
                })
                .toArray();

        if (hashKeys.length > 0) {
            opt.delete(key, hashKeys);
        }
    }
}
