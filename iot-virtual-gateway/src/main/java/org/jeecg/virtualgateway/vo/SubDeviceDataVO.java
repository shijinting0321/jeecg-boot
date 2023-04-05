package org.jeecg.virtualgateway.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class SubDeviceDataVO {
    /**
     * 设备唯一标识
     */
    private String serial;
    /**
     * 采集数据
     * key-value: 属性-数据
     */
    private Map<String, AttrValue> data;

    @Setter
    @Getter
    @ToString
    @Accessors(chain = true)
    public static class AttrValue {
        /**
         * 数据值
         */
        private String value;
        /**
         * 采集时间戳
         */
        private Long timestamp;
    }
}
