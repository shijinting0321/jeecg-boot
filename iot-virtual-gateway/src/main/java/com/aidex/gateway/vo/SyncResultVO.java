package com.aidex.gateway.vo;

import cn.hutool.core.util.StrUtil;
import com.aidex.gateway.util.SyncCodeEnum;
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
public class SyncResultVO {
    private String id;
    private String deviceId;
    private String code;

    public boolean isOk() {
        return StrUtil.equals(SyncCodeEnum.SUCCESS.getCode(), code);
    }
}
