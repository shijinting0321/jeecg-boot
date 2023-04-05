package com.aidex.gateway.util;

import com.alibaba.fastjson.annotation.JSONType;

import java.io.Serializable;

@JSONType(mappingTo = DefaultCodeMessage.class)
public interface CodeMessage extends Serializable {
    /**
     * 错误码
     */
    String getCode();

    /**
     * 提示信息
     */
    String getMessage();
}
