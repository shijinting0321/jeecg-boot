package com.aidex.gateway.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
public class DeviceVO extends SubDeviceVO {
    private String id;
    private String parentId;

    private String deviceId;
    private String parentDeviceId;

    private List<Map<String, String>> linkAttr;

    private String operate;
    private String source;
    private String pluginId;

    public DeviceVO(SubDeviceVO vo) {
        BeanUtil.copyProperties(vo, this);

        this.linkAttr = Optional.ofNullable(vo.getAttr())
                .orElseGet(MapUtil::empty)
                .entrySet()
                .stream()
                .map(en -> {
                    Map<String, String> map = MapUtil.of("code", en.getKey());
                    map.put("value", en.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
