package org.jeecg.virtualgateway.vo;

import org.jeecg.virtualgateway.entity.DeviceData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class MqttDataVO {
    /**
     * 上位机设备id
     */
    private String deviceId;
    /**
     * 采集数据
     */
    private List<DeviceData> data;
}
