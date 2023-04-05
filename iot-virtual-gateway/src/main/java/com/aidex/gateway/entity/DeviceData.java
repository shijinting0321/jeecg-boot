package com.aidex.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
@TableName("cec_soft_device_data")
public class DeviceData {
    @TableId(type = IdType.AUTO)
    protected String id;
    private String deviceId;
    private String code;
    private String value;
    private Long timestamp;
}
