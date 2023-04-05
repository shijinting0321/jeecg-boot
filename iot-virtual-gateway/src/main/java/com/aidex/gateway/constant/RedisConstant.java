package com.aidex.gateway.constant;

/**
 * @author jtcl
 * @date 2022/6/8
 */
public interface RedisConstant {
    String BASE_PRE = "cec:ac:soft:gateway:";

    String INIT_INFO_CACHE_PRE = BASE_PRE + "init_info_cache:";

    String INIT_TOKEN_CACHE_PRE = BASE_PRE + "init_token_cache:";

    String DEVICE_STATUS_CACHE_PRE = BASE_PRE + "device_status_cache:";

    String DEVICE_DATA_CACHE_PRE = BASE_PRE + "device_data_cache:";
}
