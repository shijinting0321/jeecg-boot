package org.jeecg.virtualgateway.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
public class PlatformDeviceVO {
    private String deviceId;
    private String id;
    private String parentId;

    private String sn;
    private String brandModelId;
    private String deviceName;
    private List<Map<String, String>> linkAttr;

    private String deviceParentId;
}
