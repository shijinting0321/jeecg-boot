package org.jeecg.modules.iot.gateway.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 网关管理
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Data
@TableName("iot_gateway")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="iot_gateway对象", description="网关管理")
public class IotGateway implements Serializable {
    private static final long serialVersionUID = 1L;

	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "ID")
    private java.lang.String id;
	/**租户号*/
	@Excel(name = "租户号", width = 15)
    @ApiModelProperty(value = "租户号")
    private java.lang.String tenantId;
	/**乐观锁*/
	@Excel(name = "乐观锁", width = 15)
    @ApiModelProperty(value = "乐观锁")
    private java.lang.String version;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
	/**创建部门*/
	@Excel(name = "创建部门", width = 15)
    @ApiModelProperty(value = "创建部门")
    private java.lang.String createDept;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "更新时间")
    private java.util.Date updateTime;
	/**更新IP*/
	@Excel(name = "更新IP", width = 15)
    @ApiModelProperty(value = "更新IP")
    private java.lang.String updateIp;
	/**逻辑删除标记;0-未删除 1-已删除*/
	@Excel(name = "逻辑删除标记;0-未删除 1-已删除", width = 15)
    @ApiModelProperty(value = "逻辑删除标记;0-未删除 1-已删除")
    @TableLogic
    private java.lang.Integer delFlag;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private java.lang.Integer sort;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
	/**编码*/
	@Excel(name = "编码", width = 15)
    @ApiModelProperty(value = "编码")
    private java.lang.String code;
	/**网关名称*/
	@Excel(name = "网关名称", width = 15)
    @ApiModelProperty(value = "网关名称")
    private java.lang.String name;
	/**网关类型;0-虚拟网关 1-硬件网关*/
	@Excel(name = "网关类型;0-虚拟网关 1-硬件网关", width = 15)
    @ApiModelProperty(value = "网关类型;0-虚拟网关 1-硬件网关")
    private java.lang.Integer type;
	/**令牌;用于连接mqtt*/
	@Excel(name = "令牌;用于连接mqtt", width = 15)
    @ApiModelProperty(value = "令牌;用于连接mqtt")
    private java.lang.String token;
	/**协议*/
	@Excel(name = "协议", width = 15)
    @ApiModelProperty(value = "协议")
    private java.lang.String protocol;
}