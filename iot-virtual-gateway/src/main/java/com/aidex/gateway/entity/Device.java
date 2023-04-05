package com.aidex.gateway.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
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
@TableName(value = "cec_soft_device", autoResultMap = true)
public class Device {

    @TableId(type = IdType.AUTO)
    protected String id;
    @TableLogic
    protected Boolean deleted;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String parentId;
    private String deviceId;
    private String serial;
    private String brandModelId;
    private String name;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<Map<String, String>> linkAttr;

    private String syncResult;
    private String source;
    private String pluginId;
}
