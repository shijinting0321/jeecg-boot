/* --------------- 创建表 --------------- */
DROP TABLE IF EXISTS tb_device;
CREATE TABLE tb_device
(
    `id`                VARCHAR(64)  NOT NULL COMMENT 'ID',
    `tenant_id`         VARCHAR(64) COMMENT '租户号',
    `version`           VARCHAR(64) COMMENT '乐观锁',
    `create_by`         VARCHAR(64) COMMENT '创建人',
    `create_dept`       VARCHAR(64) COMMENT '创建部门',
    `create_time`       DATETIME COMMENT '创建时间',
    `update_by`         VARCHAR(64) COMMENT '更新人',
    `update_time`       DATETIME COMMENT '更新时间',
    `update_ip`         VARCHAR(64) COMMENT '更新IP',
    `del_flag`          INT(1) COMMENT '逻辑删除标记;0-未删除 1-已删除',
    `sort`              INT COMMENT '排序',
    `remark`            VARCHAR(255) COMMENT '备注',
    `linked_gateway_id` VARCHAR(64) COMMENT '关联网关id',
    `device_code`       VARCHAR(64)  NOT NULL COMMENT '设备编码',
    `device_name`       VARCHAR(255) NOT NULL COMMENT '设备名称',
    `brand`             VARCHAR(255) COMMENT '品牌',
    `model`             VARCHAR(255) COMMENT '型号',
    `status`            VARCHAR(2) COMMENT '状态',
    PRIMARY KEY (id)
) COMMENT = '设备表';
DROP TABLE IF EXISTS tb_device_param;
CREATE TABLE tb_device_param
(
    `id`                VARCHAR(64) NOT NULL COMMENT 'ID',
    `tenant_id`         VARCHAR(64) COMMENT '租户号',
    `version`           VARCHAR(64) COMMENT '乐观锁',
    `create_by`         VARCHAR(64) COMMENT '创建人',
    `create_dept`       VARCHAR(64) COMMENT '创建部门',
    `create_time`       DATETIME COMMENT '创建时间',
    `update_by`         VARCHAR(64) COMMENT '更新人',
    `update_time`       DATETIME COMMENT '更新时间',
    `update_ip`         VARCHAR(64) COMMENT '更新IP',
    `del_flag`          INT(1) COMMENT '逻辑删除标记;0-未删除 1-已删除',
    `sort`              INT COMMENT '排序',
    `remark`            VARCHAR(255) COMMENT '备注',
    `linked_gateway_id` VARCHAR(64) COMMENT '关联网关id',
    `device_id`         VARCHAR(64) COMMENT '设备id',
    `device_code`       VARCHAR(64) COMMENT '设备编码',
    `param_code`        VARCHAR(255) COMMENT '参数编码',
    `param_value`       VARCHAR(255) COMMENT '参数值',
    PRIMARY KEY (id)
) COMMENT = '设备参数表';

DROP TABLE IF EXISTS tb_protocol_plugins;
CREATE TABLE tb_protocol_plugins
(
    `id`          VARCHAR(64) NOT NULL COMMENT 'ID',
    `tenant_id`   VARCHAR(64) COMMENT '租户号',
    `version`     VARCHAR(64) COMMENT '乐观锁',
    `create_by`   VARCHAR(64) COMMENT '创建人',
    `create_dept` VARCHAR(64) COMMENT '创建部门',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by`   VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME COMMENT '更新时间',
    `update_ip`   VARCHAR(64) COMMENT '更新IP',
    `del_flag`    INT(1) COMMENT '逻辑删除标记;0-未删除 1-已删除',
    `sort`        INT COMMENT '排序',
    `remark`      VARCHAR(255) COMMENT '备注',
    `plugin_code` VARCHAR(64) COMMENT '协议插件编码',
    `name`        VARCHAR(255) COMMENT '名称',
    `description` VARCHAR(900) COMMENT '描述',
    `file`        VARCHAR(255) COMMENT '协议文件',
    `status`      VARCHAR(2) COMMENT '状态',
    PRIMARY KEY (id)
) COMMENT = '协议管理表';
