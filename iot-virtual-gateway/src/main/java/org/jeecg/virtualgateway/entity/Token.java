package org.jeecg.virtualgateway.entity;

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
public class Token {
    private String accessToken;
    private Integer expire;
}
