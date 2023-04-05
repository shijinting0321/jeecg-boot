package com.aidex.gateway.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jtcl
 * @date 2022/6/8
 */
@Setter
@Getter
@ToString
public class Gateway {
    private String id;
    private String brandModelId;
    private String latestVersion;
    private String deviceKey;
    private String deviceSecret;
}
