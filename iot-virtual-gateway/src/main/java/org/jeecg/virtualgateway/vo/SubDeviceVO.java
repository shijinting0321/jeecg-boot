package org.jeecg.virtualgateway.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class SubDeviceVO {
    /**
     * 设备唯一标识
     */
    private String serial;
    /*
     * 设备品牌型号
     */
    private String brandModelId;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 连接属性
     */
    private Map<String, String> attr;
    /**
     * 下级设备
     */
    private List<SubDeviceVO> children;

    /**
     * 上级设备唯一标识
     */
    private String parentSerial;
}
